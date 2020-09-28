package de.caritas.cob.userservice.api.facade.getsessionlist;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.exception.CustomCryptoException;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionAttachmentDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.model.chat.UserChatDTO;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.SessionFilter;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.DecryptionService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.SessionService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Facade to encapsulate the steps to get the session list for a user or consultant (read sessions
 * from database and get unread messages status from Rocket.Chat)
 */

@Service
public class GetSessionListFacade {

  private static final int TRUNCATE_START = 0;
  private static final int TRUNCATE_END = 100;

  private final SessionService sessionService;
  private final DecryptionService decryptionService;
  private final ConsultingTypeManager consultingTypeManager;
  private final ChatService chatService;
  private final RocketChatRoomInformationProvider rocketChatRoomInformationProvider;

  @Autowired
  public GetSessionListFacade(SessionService sessionService,
      DecryptionService decryptionService, ConsultingTypeManager consultingTypeManager,
      ChatService chatService, RocketChatRoomInformationProvider rocketChatRoomInformationProvider) {
    this.sessionService = sessionService;
    this.decryptionService = decryptionService;
    this.consultingTypeManager = consultingTypeManager;
    this.chatService = chatService;
    this.rocketChatRoomInformationProvider = rocketChatRoomInformationProvider;
  }

  /**
   * Returns a list of {@link UserSessionResponseDTO} for the specified user ID
   *
   * @param userId  Keycloak/MariaDB user ID
   * @param rocketChatCredentials the rocket chat credentials
   * @return {@link UserSessionResponseDTO}
   */
  public UserSessionListResponseDTO getSessionsForAuthenticatedUser(String userId,
      RocketChatCredentials rocketChatCredentials) {

    List<UserSessionResponseDTO> sessions = sessionService.getSessionsForUserId(userId);
    List<UserSessionResponseDTO> chats = chatService.getChatsForUserId(userId);

    if (!isUserSessionsOrChatsAvailable(sessions, chats)) {
      return new UserSessionListResponseDTO();
    }

    List<UserSessionResponseDTO> allSessions = retrieveMergedListOfUserSessionsAndChats(
        sessions, chats, rocketChatCredentials);

    // Sort the session list so the latest answers are on top
    allSessions.sort(Comparator.comparing(UserSessionResponseDTO::getLatestMessage).reversed());

    return new UserSessionListResponseDTO(allSessions);

  }

  private boolean isUserSessionsOrChatsAvailable(List<UserSessionResponseDTO> sessions,
      List<UserSessionResponseDTO> chats) {
    return !CollectionUtils.isEmpty(sessions) || !CollectionUtils.isEmpty(chats);
  }

  private List<UserSessionResponseDTO> retrieveMergedListOfUserSessionsAndChats(
      List<UserSessionResponseDTO> sessions,
      List<UserSessionResponseDTO> chats, RocketChatCredentials rocketChatCredentials) {

    RocketChatRoomInformation rocketChatRoomInformation = rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(rocketChatCredentials);

    List<UserSessionResponseDTO> allSessions = new ArrayList<>();
    allSessions.addAll(setUserSessionValues(sessions, rocketChatRoomInformation,
        rocketChatCredentials.getRocketChatUserId()));
    allSessions.addAll(setUserChatValues(chats, rocketChatRoomInformation,
        rocketChatCredentials.getRocketChatUserId()));

    return allSessions;
  }

  /**
   * Sets the messagesRead and latestMessage value for the specified list of {@link
   * UserSessionResponseDTO} with {@link SessionDTO}.
   *
   * @param sessions a {@link List} of {@link UserSessionResponseDTO}
   * @param rocketChatRoomInformation {@link RocketChatRoomInformation}
   * @param rcUserId the Rocket.Chat id of the user
   * @return the list of {@link UserSessionResponseDTO}
   */
  private List<UserSessionResponseDTO> setUserSessionValues(List<UserSessionResponseDTO> sessions,
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId) {

    for (UserSessionResponseDTO session : sessions) {

      session.getSession().setMessagesRead(
          isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
              session.getSession().getGroupId()));
      if (isLastMessageForRocketChatGroupIdAvailable(rocketChatRoomInformation.getRoomLastMessageMap(),
          session.getSession().getGroupId())) {
        RoomsLastMessageDTO roomsLastMessage =
            rocketChatRoomInformation.getRoomLastMessageMap().get(session.getSession().getGroupId());
        session.getSession().setLastMessage(
            !StringUtils.isEmpty(roomsLastMessage.getMessage()) ? decryptAndTruncateMessage(
                roomsLastMessage.getMessage(), session.getSession().getGroupId()) : null);
        session.getSession()
            .setMessageDate(Helper.getUnixTimestampFromDate(roomsLastMessage.getTimestamp()));
        session.setLatestMessage(roomsLastMessage.getTimestamp());
        session.getSession()
            .setAttachment(
                getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
      } else {
        session.setLatestMessage(Helper.UNIXTIME_0);
      }

    }

    return sessions;
  }

  /**
   * Sets the messagesRead and latestMessage value for the specified list of {@link
   * UserSessionResponseDTO} with {@link UserChatDTO}.
   *
   * @param chats a {@link List} of {@link UserSessionResponseDTO}
   * @param rocketChatRoomInformation {@link RocketChatRoomInformation}
   * @param rcUserId the Rocket.Chat id of the user
   * @return the list of {@link UserSessionResponseDTO}
   */
  private List<UserSessionResponseDTO> setUserChatValues(List<UserSessionResponseDTO> chats,
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId) {

    for (UserSessionResponseDTO chat : chats) {

      chat.getChat().setSubscribed(isRocketChatRoomSubscribedByUser(rocketChatRoomInformation.getUserRoomList(), chat));
      chat.getChat().setMessagesRead(
          isMessagesForRocketChatGroupReadByUser(rocketChatRoomInformation.getMessagesReadMap(),
              chat.getChat().getGroupId()));

      if (isLastMessageForRocketChatGroupIdAvailable(rocketChatRoomInformation.getRoomLastMessageMap(),
          chat.getChat().getGroupId())) {
        RoomsLastMessageDTO roomsLastMessage = rocketChatRoomInformation.getRoomLastMessageMap()
            .get(chat.getChat().getGroupId());
        chat.getChat().setLastMessage(!StringUtils.isEmpty(roomsLastMessage.getMessage())
            ? decryptAndTruncateMessage(roomsLastMessage.getMessage(), chat.getChat().getGroupId())
            : null);
        chat.getChat()
            .setMessageDate(Helper.getUnixTimestampFromDate(roomsLastMessage.getTimestamp()));
        chat.setLatestMessage(roomsLastMessage.getTimestamp());
        chat.getChat()
            .setAttachment(
                getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
      } else {
        chat.setLatestMessage(Timestamp.valueOf(chat.getChat().getStartDateWithTime()));
      }

    }

    return chats;
  }

  private boolean isLastMessageForRocketChatGroupIdAvailable(
      Map<String, RoomsLastMessageDTO> roomLastMessageMap, String groupId) {
    return roomLastMessageMap.containsKey(groupId);
  }

  /**
   * Check, if rooms is subscribed by user
   *
   * @param userRoomsList list of subscribed Rocket.Chat rooms by a user
   * @param chat          the chat object
   * @return true, if room is subscribed by user
   */
  private boolean isRocketChatRoomSubscribedByUser(List<String> userRoomsList,
      UserSessionResponseDTO chat) {
    return !Objects.isNull(userRoomsList) && userRoomsList.contains(chat.getChat().getGroupId());
  }

  /**
   * Check, if messages for given session were read by user
   *
   * @param messagesReadMap list with room information from Rocket.Chat
   * @param groupId         the Rocket.Chat group id of the session or chat
   * @return true, if messages were read for given thread or no messages in chat room available
   */
  private boolean isMessagesForRocketChatGroupReadByUser(Map<String, Boolean> messagesReadMap,
      String groupId) {
    return messagesReadMap.getOrDefault(groupId, true);
  }

  private SessionAttachmentDTO getAttachmentFromRocketChatMessageIfAvailable(String rcUserId,
      RoomsLastMessageDTO roomsLastMessageDto) {
    if (Objects.isNull(roomsLastMessageDto.getFile())) {
      return null;
    }
    return new SessionAttachmentDTO(roomsLastMessageDto.getFile().getType(),
        roomsLastMessageDto.getAttachements()[0].getImagePreview(),
        !rcUserId.equals(roomsLastMessageDto.getUser().getId()));
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the specified consultant id and
   * status
   *
   * @param consultant    {@link Consultant}
   * @param status        integer (1=enquiries, 2=sessions)
   * @param rcAuthToken   Rocket.Chat Token
   * @param offset        the offset of the sublist
   * @param count         the count of the sublist
   * @param sessionFilter Filter
   * @return the response dto
   */
  public ConsultantSessionListResponseDTO getSessionsForAuthenticatedConsultant(
      Consultant consultant, int status, String rcAuthToken, int offset, int count,
      SessionFilter sessionFilter) {

    List<ConsultantSessionResponseDTO> sessions = sessionService
        .getSessionsForConsultant(consultant, status);
    List<ConsultantSessionResponseDTO> chats = null;

    if (status == SessionStatus.IN_PROGRESS.getValue()) {
      chats = chatService.getChatsForConsultant(consultant);
    }

    if (!isConsultantSessionsOrChatsAvailable(offset, sessions, chats)) {
      return new ConsultantSessionListResponseDTO();
    }

    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .RocketChatUserId(consultant.getRocketChatId()).RocketChatToken(rcAuthToken).build();
    List<ConsultantSessionResponseDTO> allSessions = retrieveMergedListOfConsultantSessionsAndChats(
        consultant, sessions, chats, rocketChatCredentials);

    /* Sort the session list by latest Rocket.Chat message if session is in progress (no enquiry).
     * The latest answer is on top.
     *
     * Please note: Enquiry message sessions are being sorted by the repository (via
     * SessionService). Here the latest enquiry message is on the bottom.
     */
    Optional<SessionStatus> sessionStatus = SessionStatus.valueOf(status);
    if (sessionStatus.isPresent() && sessionStatus.get().equals(SessionStatus.IN_PROGRESS)) {
      sortSessionsByLastMessageDateDesc(allSessions);
    }

    if (sessionFilter.equals(SessionFilter.FEEDBACK)) {
      removeAllChatsAndSessionsWithoutUnreadFeedback(allSessions);
    }

    List<ConsultantSessionResponseDTO> sessionSublist =
        getSessionSublistByOffsetAndCount(allSessions, offset, count);

    return new ConsultantSessionListResponseDTO(sessionSublist, offset, sessionSublist.size(),
        allSessions.size());

  }

  private List<ConsultantSessionResponseDTO> retrieveMergedListOfConsultantSessionsAndChats(
      Consultant consultant, List<ConsultantSessionResponseDTO> sessions,
      List<ConsultantSessionResponseDTO> chats, RocketChatCredentials rocketChatCredentials) {
    List<ConsultantSessionResponseDTO> allSessions = new ArrayList<>();

    RocketChatRoomInformation rocketChatRoomInformation = rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(rocketChatCredentials);

    if (!CollectionUtils.isEmpty(sessions)) {
      allSessions.addAll(setConsultantSessionValues(sessions, rocketChatRoomInformation,
          consultant.getRocketChatId()));
    }

    if (!CollectionUtils.isEmpty(chats)) {
      allSessions.addAll(setConsultantChatValues(chats, rocketChatRoomInformation, consultant.getRocketChatId()));
    }
    return allSessions;
  }

  private boolean isConsultantSessionsOrChatsAvailable(int offset,
      List<ConsultantSessionResponseDTO> sessions, List<ConsultantSessionResponseDTO> chats) {
    return (!CollectionUtils.isEmpty(sessions) && offset < sessions.size()) || !CollectionUtils
        .isEmpty(chats);
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the specified consultant id.
   *
   * @param consultant the {@link Consultant}
   * @param rcAuthToken the Rocket.Chat auth token
   * @param offset offset
   * @param count count
   * @param sessionFilter the {@link SessionFilter}
   * @return a {@link ConsultantSessionListResponseDTO} with a {@link List} of {@link ConsultantSessionResponseDTO}
   */
  public ConsultantSessionListResponseDTO getTeamSessionsForAuthenticatedConsultant(
      Consultant consultant, String rcAuthToken, int offset, int count,
      SessionFilter sessionFilter) {

    List<ConsultantSessionResponseDTO> sessions =
        sessionService.getTeamSessionsForConsultant(consultant);

    if (!isSessionsAvailable(offset, sessions)) {
      return null;
    }

    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .RocketChatUserId(consultant.getRocketChatId()).RocketChatToken(rcAuthToken).build();
    RocketChatRoomInformation rocketChatRoomInformation = rocketChatRoomInformationProvider.retrieveRocketChatInformation(rocketChatCredentials);
    setConsultantSessionValues(sessions, rocketChatRoomInformation, consultant.getRocketChatId());

    /**
     * Sort the session list by latest Rocket.Chat message if session is in progress (no enquiry).
     * The latest answer is on top.
     */
    sortSessionsByLastMessageDateDesc(sessions);

    if (sessionFilter.equals(SessionFilter.FEEDBACK)) {
      removeAllChatsAndSessionsWithoutUnreadFeedback(sessions);
    }

    List<ConsultantSessionResponseDTO> sessionSublist =
        getSessionSublistByOffsetAndCount(sessions, offset, count);

    return new ConsultantSessionListResponseDTO(
        getSessionSublistByOffsetAndCount(sessions, offset, count), offset, sessionSublist.size(),
        sessions.size());
  }

  private boolean isSessionsAvailable(int offset, List<ConsultantSessionResponseDTO> sessions) {
    return !Objects.isNull(sessions) && offset < sessions.size();
  }

  private void sortSessionsByLastMessageDateDesc(List<ConsultantSessionResponseDTO> sessions) {
    sessions.sort(Comparator.comparing(ConsultantSessionResponseDTO::getLatestMessage).reversed());
  }

  /**
   * Get a sublist of a session list with offset and count
   *
   * @param sessions a {@link List} of {@link ConsultantSessionResponseDTO}
   * @param offset the offset of the sublist
   * @param count the count of the sublist
   * @return a sublist of the given session list
   */
  private List<ConsultantSessionResponseDTO> getSessionSublistByOffsetAndCount(
      List<ConsultantSessionResponseDTO> sessions, int offset, int count) {
    return sessions.subList(offset, Math.min(offset + count, sessions.size()));
  }

  /**
   * Remove chats and all sessions which don't have unread feedback chat messages from the given
   * session list
   *
   * @param sessions a {@link List} of {@link ConsultantSessionResponseDTO}
   * @return the reduced @link List} of {@link ConsultantSessionResponseDTO}
   */
  private void removeAllChatsAndSessionsWithoutUnreadFeedback(
      List<ConsultantSessionResponseDTO> sessions) {

    sessions.removeIf(
        consultantSessionResponseDTO -> !Objects.isNull(consultantSessionResponseDTO.getChat())
            || consultantSessionResponseDTO.getSession().isFeedbackRead());
  }

  /**
   * Sets the values for messagesRead, latestMessage and monitoring for the specified list of {@link
   * ConsultantSessionResponseDTO} and decrypts and truncates the room's last message.
   *
   * @param sessions a {@link List} of {@link ConsultantSessionResponseDTO}
   * @param rocketChatRoomInformation {@link RocketChatRoomInformation}
   * @param rcUserId the Rocket.Chat id of the user
   * @return a {@link List} of {@link ConsultantSessionResponseDTO}
   */
  private List<ConsultantSessionResponseDTO> setConsultantSessionValues(
      List<ConsultantSessionResponseDTO> sessions, RocketChatRoomInformation rocketChatRoomInformation, String rcUserId) {

    for (ConsultantSessionResponseDTO session : sessions) {

      session.getSession().setMonitoring(getMonitoringProperty(session.getSession()));

      // Fallback: If map doesn't contain feedback group id messagesRead is false to ensure that
      // nothing will be missed
      session.getSession()
          .setMessagesRead(rocketChatRoomInformation
              .getMessagesReadMap().getOrDefault(session.getSession().getGroupId(), false));

      if (isLastMessageForRocketChatGroupIdAvailable(rocketChatRoomInformation.getRoomLastMessageMap(),
          session.getSession().getGroupId())) {
        RoomsLastMessageDTO roomsLastMessage =
            rocketChatRoomInformation.getRoomLastMessageMap().get(session.getSession().getGroupId());
        session.getSession().setLastMessage(
            !StringUtils.isEmpty(roomsLastMessage.getMessage()) ? decryptAndTruncateMessage(
                roomsLastMessage.getMessage(), session.getSession().getGroupId()) : null);
        session.getSession().setMessageDate(Helper.getUnixTimestampFromDate(
            rocketChatRoomInformation.getRoomLastMessageMap().get(session.getSession().getGroupId()).getTimestamp()));
        session.setLatestMessage(roomsLastMessage.getTimestamp());
        session.getSession()
            .setAttachment(
                getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
      } else {
        // Fallback: If map doesn't contain group id messagedate is set to 1970-01-01
        session.getSession().setMessageDate(Helper.UNIXTIME_0.getTime());
        session.setLatestMessage(Helper.UNIXTIME_0);
      }

      // Due to a Rocket.Chat bug the read state is is only set, when a message was posted
      if (isFeedbackFlagAvailable(rocketChatRoomInformation, session)) {
        session.getSession()
            .setFeedbackRead(
                rocketChatRoomInformation.getMessagesReadMap().get(session.getSession().getFeedbackGroupId()));
      } else {
        // Fallback: If map doesn't contain feedback group id feedbackRead is false to ensure that
        // nothing will be missed
        session.getSession().setFeedbackRead(
            !rocketChatRoomInformation.getRoomLastMessageMap().containsKey(session.getSession().getFeedbackGroupId()));
      }

    }

    return sessions;
  }

  private boolean isFeedbackFlagAvailable(RocketChatRoomInformation rocketChatRoomInformation, ConsultantSessionResponseDTO session) {
    return rocketChatRoomInformation.getRoomLastMessageMap().containsKey(session.getSession().getFeedbackGroupId())
        && rocketChatRoomInformation.getMessagesReadMap().containsKey(session.getSession().getFeedbackGroupId());
  }

  /**
   * Sets the values for messagesRead, latestMessage and monitoring for the specified list of {@link
   *  ConsultantSessionResponseDTO} and decrypts and truncates the room's last message.
   *
   * @param chats a {@link List} of {@link ConsultantSessionResponseDTO}
   * @param rocketChatRoomInformation {@link RocketChatRoomInformation}
   * @param rcUserId the Rocket.Chat id of the user
   * @return the {@link List} of {@link ConsultantSessionResponseDTO}
   */
  private List<ConsultantSessionResponseDTO> setConsultantChatValues(
      List<ConsultantSessionResponseDTO> chats, RocketChatRoomInformation rocketChatRoomInformation,
      String rcUserId) {

    for (ConsultantSessionResponseDTO chat : chats) {

      chat.getChat().setSubscribed(isRoomSubscribedByConsultant(rocketChatRoomInformation.getUserRoomList(), chat));
      chat.getChat()
          .setMessagesRead(rocketChatRoomInformation
              .getMessagesReadMap().getOrDefault(chat.getChat().getGroupId(), true));

      if (isLastMessageForRocketChatGroupIdAvailable(rocketChatRoomInformation.getRoomLastMessageMap(),
          chat.getChat().getGroupId())) {
        RoomsLastMessageDTO roomsLastMessage = rocketChatRoomInformation
            .getRoomLastMessageMap().get(chat.getChat().getGroupId());
        chat.getChat().setLastMessage(!StringUtils.isEmpty(roomsLastMessage.getMessage())
            ? decryptAndTruncateMessage(roomsLastMessage.getMessage(), chat.getChat().getGroupId())
            : null);
        chat.getChat().setMessageDate(Helper.getUnixTimestampFromDate(
            rocketChatRoomInformation.getRoomLastMessageMap().get(chat.getChat().getGroupId()).getTimestamp()));
        chat.setLatestMessage(roomsLastMessage.getTimestamp());
        chat.getChat()
            .setAttachment(
                getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
      } else {
        chat.setLatestMessage(Timestamp.valueOf(chat.getChat().getStartDateWithTime()));
      }
    }

    return chats;
  }

  private boolean isRoomSubscribedByConsultant(List<String> userRoomsList,
      ConsultantSessionResponseDTO chat) {
    return !Objects.isNull(userRoomsList) && userRoomsList.contains(chat.getChat().getGroupId());
  }

  /**
   * Decrypts and returns a Rocket.Chat message and truncates it to a maximum length
   *
   * @param message Encrypted message
   * @param groupId Rocket.Chat group id of the message
   * @return Decrypted message
   */
  private String decryptAndTruncateMessage(String message, String groupId) {

    String decryptedMessage = null;

    try {
      decryptedMessage = decryptionService.decrypt(message, groupId);
      decryptedMessage = truncateMessageToMaximalLengthForFrontend(decryptedMessage);

    } catch (CustomCryptoException cryptoEx) {
      LogService.logDecryptionError(
          String.format("Could not decrypt message for group id %s", groupId), cryptoEx);
    } catch (IndexOutOfBoundsException substringEx) {
      LogService.logTruncationError(
          String.format("Could not truncate message for group id %s", groupId), substringEx);
    }

    return decryptedMessage;

  }

  private String truncateMessageToMaximalLengthForFrontend(String decryptedMessage) {
    if (Objects.isNull(decryptedMessage)) {
      return null;
    }
    return decryptedMessage.substring(TRUNCATE_START,
        Math.min(decryptedMessage.length(), TRUNCATE_END));
  }

  /**
   * Returns the monitoring property (which is set in the {@link ConsultingTypeSettings}) for the
   * given session.
   *
   * @param session the session
   * @return true if monitoring is active, else false
   */
  private boolean getMonitoringProperty(SessionDTO session) {

    Optional<ConsultingType> consultingType = ConsultingType.valueOf(session.getConsultingType());

    if (!consultingType.isPresent()) {
      throw new ServiceException(String
          .format("Session with id %s does not have a valid consulting type.", session.getId()));
    }

    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultantTypeSettings(consultingType.get());

    return consultingTypeSettings.isMonitoring();
  }

}
