package de.caritas.cob.userservice.api.facade;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.CustomCryptoException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatGetRoomsException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatGetSubscriptionsException;
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
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.model.rocketChat.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.SessionFilter;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.DecryptionService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;

/**
 * Facade to encapsulate the steps to get the session list for a user or consultant (read sessions
 * from database and get unread messages status from Rocket.Chat)
 */

@Service
public class GetSessionListFacade {

  private final int truncateStart = 0;
  private final int truncateEnd = 100;

  private final SessionService sessionService;
  private final RocketChatService rocketChatService;
  private final DecryptionService decryptionService;
  private final ConsultingTypeManager consultingTypeManager;
  private final ChatService chatService;

  @Autowired
  public GetSessionListFacade(SessionService sessionService, RocketChatService rocketChatService,
      DecryptionService decryptionService, ConsultingTypeManager consultingTypeManager,
      ChatService chatService) {
    this.sessionService = sessionService;
    this.rocketChatService = rocketChatService;
    this.decryptionService = decryptionService;
    this.consultingTypeManager = consultingTypeManager;
    this.chatService = chatService;
  }

  /**
   * Returns a list of {@link UserSessionResponseDTO} for the specified user ID
   *
   * @param userId Keycloak/MariaDB user ID
   * @param rcUserId Rocket.Chat user ID
   * @param rcAuthToken Rocket.Chat token
   * @return {@link UserSessionResponseDTO}
   */
  public UserSessionListResponseDTO getSessionsForAuthenticatedUser(String userId,
      RocketChatCredentials rocketChatCredentials) {

    List<UserSessionResponseDTO> sessions = sessionService.getSessionsForUserId(userId);
    List<UserSessionResponseDTO> chats = chatService.getChatsForUserId(userId);

    if ((sessions == null || sessions.size() == 0) && (chats == null || chats.size() == 0)) {
      return new UserSessionListResponseDTO();
    }

    Map<String, Boolean> messagesReadMap = Collections.emptyMap();
    List<RoomsUpdateDTO> roomsUpdateList = Collections.emptyList();

    if (rocketChatCredentials.getRocketChatUserId() != null) {
      messagesReadMap = getMessagesReadMap(rocketChatCredentials);
      roomsUpdateList = getRcRoomsUpdateList(rocketChatCredentials);
    }

    List<String> userRoomList =
        roomsUpdateList.stream().map(x -> x.getId()).collect(Collectors.toList());
    Map<String, RoomsLastMessageDTO> roomMessageMap = getRcRoomMessageMap(roomsUpdateList);
    List<UserSessionResponseDTO> allSessions = new ArrayList<UserSessionResponseDTO>();

    if (sessions != null && sessions.size() > 0) {
      allSessions.addAll(setUserSessionValues(sessions, messagesReadMap, roomMessageMap,
          rocketChatCredentials.getRocketChatUserId()));
    }

    if (chats != null && chats.size() > 0) {
      allSessions.addAll(setUserChatValues(chats, messagesReadMap, roomMessageMap, userRoomList,
          rocketChatCredentials.getRocketChatUserId()));
    }

    // Sort the session list so the latest answers are on top
    allSessions.sort(Comparator.comparing(UserSessionResponseDTO::getLatestMessage).reversed());

    return new UserSessionListResponseDTO(allSessions);

  }

  /**
   * Sets the messagesRead and latestMessage value for the specified list of
   * {@link UserSessionResponseDTO} with {@link UserChatDTO}
   *
   * @param sessions
   * @param messagesReadMap
   * @param roomMessageMap
   * @param rcUserId
   * @return
   */
  private List<UserSessionResponseDTO> setUserChatValues(List<UserSessionResponseDTO> chats,
      Map<String, Boolean> messagesReadMap, Map<String, RoomsLastMessageDTO> roomMessageMap,
      List<String> userRoomsList, String rcUserId) {

    for (UserSessionResponseDTO chat : chats) {

      if (userRoomsList != null && userRoomsList.contains(chat.getChat().getGroupId())) {
        chat.getChat().setSubscribed(true);
      } else {
        chat.getChat().setSubscribed(false);
      }

      if (messagesReadMap.containsKey(chat.getChat().getGroupId())) {
        chat.getChat().setMessagesRead(messagesReadMap.get(chat.getChat().getGroupId()));
      } else {
        chat.getChat().setMessagesRead(true);
      }

      if (roomMessageMap.containsKey(chat.getChat().getGroupId())) {
        RoomsLastMessageDTO roomsLastMessage = roomMessageMap.get(chat.getChat().getGroupId());
        chat.getChat().setLastMessage(!StringUtils.isEmpty(roomsLastMessage.getMessage())
            ? decryptAndTruncateMessage(roomsLastMessage.getMessage(), chat.getChat().getGroupId())
            : null);
        chat.getChat()
            .setMessageDate(Helper.getUnixTimestampFromDate(roomsLastMessage.getTimestamp()));
        chat.setLatestMessage(roomsLastMessage.getTimestamp());
        // Attachment
        if (roomsLastMessage.getFile() != null) {
          chat.getChat()
              .setAttachment(new SessionAttachmentDTO(roomsLastMessage.getFile().getType(),
                  roomsLastMessage.getAttachements()[0].getImagePreview(),
                  !rcUserId.equals(roomsLastMessage.getUser().getId())));
        }
      } else {
        chat.setLatestMessage(Timestamp.valueOf(chat.getChat().getStartDateWithTime()));
      }

    }

    return chats;

  }

  /**
   * Sets the messagesRead and latestMessage value for the specified list of
   * {@link UserSessionResponseDTO} with {@link SessionDTO}
   *
   * @param sessions
   * @param messagesReadMap
   * @param roomMessageMap
   * @param rcUserId
   * @return
   */
  private List<UserSessionResponseDTO> setUserSessionValues(List<UserSessionResponseDTO> sessions,
      Map<String, Boolean> messagesReadMap, Map<String, RoomsLastMessageDTO> roomMessageMap,
      String rcUserId) {

    for (UserSessionResponseDTO session : sessions) {

      if (messagesReadMap.containsKey(session.getSession().getGroupId())) {
        session.getSession()
            .setMessagesRead(messagesReadMap.get(session.getSession().getGroupId()));
      } else {
        // Fallback: If map doesn't contain feedback group id messagesRead is false to ensure that
        // nothing will be missed
        session.getSession().setMessagesRead(false);
      }

      if (roomMessageMap.containsKey(session.getSession().getGroupId())) {
        RoomsLastMessageDTO roomsLastMessage =
            roomMessageMap.get(session.getSession().getGroupId());
        session.getSession().setLastMessage(
            !StringUtils.isEmpty(roomsLastMessage.getMessage()) ? decryptAndTruncateMessage(
                roomsLastMessage.getMessage(), session.getSession().getGroupId()) : null);

        session.getSession()
            .setMessageDate(Helper.getUnixTimestampFromDate(roomsLastMessage.getTimestamp()));
        session.setLatestMessage(roomsLastMessage.getTimestamp());
        // Attachment
        if (roomsLastMessage.getFile() != null) {
          session.getSession()
              .setAttachment(new SessionAttachmentDTO(roomsLastMessage.getFile().getType(),
                  roomsLastMessage.getAttachements()[0].getImagePreview(),
                  !rcUserId.equals(roomsLastMessage.getUser().getId())));
        }
      } else {
        session.setLatestMessage(Helper.UNIXTIME_0);
      }

    }

    return sessions;

  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the specified consultant id and
   * status
   *
   * @param consultant {@link Consultant}
   * @param status integer (1=enquiries, 2=sessions)
   * @param rcAuthToken Rocket.Chat Token
   * @param offset Offset
   * @param count Count
   * @param sessionFilter Filter
   * @return
   */
  public ConsultantSessionListResponseDTO getSessionsForAuthenticatedConsultant(
      Consultant consultant, int status, String rcAuthToken, int offset, int count,
      SessionFilter sessionFilter) {

    List<ConsultantSessionResponseDTO> sessions =
        sessionService.getSessionsForConsultant(consultant, status);
    List<ConsultantSessionResponseDTO> chats = null;
    if (status == SessionStatus.IN_PROGRESS.getValue()) {
      chats = chatService.getChatsForConsultant(consultant);
    }

    if ((sessions == null || offset >= sessions.size()) && (chats == null || chats.size() == 0)) {
      return new ConsultantSessionListResponseDTO();
    }

    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .RocketChatUserId(consultant.getRocketChatId()).RocketChatToken(rcAuthToken).build();
    Map<String, Boolean> messagesReadMap = getMessagesReadMap(rocketChatCredentials);
    List<RoomsUpdateDTO> roomsUpdateList = getRcRoomsUpdateList(rocketChatCredentials);
    List<String> userRoomList =
        roomsUpdateList.stream().map(x -> x.getId()).collect(Collectors.toList());
    Map<String, RoomsLastMessageDTO> roomMessageMap = getRcRoomMessageMap(roomsUpdateList);
    List<ConsultantSessionResponseDTO> allSessions = new ArrayList<ConsultantSessionResponseDTO>();

    if (sessions != null && sessions.size() > 0) {
      allSessions.addAll(setConsultantSessionValues(sessions, messagesReadMap, roomMessageMap,
          consultant.getRocketChatId()));
    }

    if (chats != null && chats.size() > 0) {
      allSessions.addAll(setConsultantChatValues(chats, messagesReadMap, roomMessageMap,
          userRoomList, consultant.getRocketChatId()));
    }

    Optional<SessionStatus> sessionStatus = SessionStatus.valueOf(status);

    /**
     * Sort the session list by latest Rocket.Chat message if session is in progress (no enquiry).
     * The latest answer is on top.
     *
     * Please note: Enquiry message sessions are being sorted by the repository (via
     * SessionService). Here the latest enquiry message is on the bottom.
     */
    if (sessionStatus.isPresent() && sessionStatus.get().equals(SessionStatus.IN_PROGRESS)) {
      allSessions
          .sort(Comparator.comparing(ConsultantSessionResponseDTO::getLatestMessage).reversed());
    }

    if (sessionFilter.equals(SessionFilter.FEEDBACK)) {
      allSessions = removeAllChatsAndSessionsWithoutUnreadFeedback(allSessions);
    }

    List<ConsultantSessionResponseDTO> sessionSublist =
        getSessionSublistByOffsetAndCount(allSessions, offset, count);

    return new ConsultantSessionListResponseDTO(sessionSublist, offset, sessionSublist.size(),
        allSessions.size());

  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the specified consultant id
   *
   * @param consultant
   * @param rcAuthToken
   * @param offset
   * @param count
   * @param sessionFilter
   * @return
   */
  public ConsultantSessionListResponseDTO getTeamSessionsForAuthenticatedConsultant(
      Consultant consultant, String rcAuthToken, int offset, int count,
      SessionFilter sessionFilter) {

    List<ConsultantSessionResponseDTO> sessions =
        sessionService.getTeamSessionsForConsultant(consultant);

    if (sessions == null || offset >= sessions.size()) {
      return null;
    }

    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .RocketChatUserId(consultant.getRocketChatId()).RocketChatToken(rcAuthToken).build();
    Map<String, Boolean> messagesReadMap = getMessagesReadMap(rocketChatCredentials);
    List<RoomsUpdateDTO> roomsUpdateList = getRcRoomsUpdateList(rocketChatCredentials);
    Map<String, RoomsLastMessageDTO> roomMessageMap = getRcRoomMessageMap(roomsUpdateList);
    sessions = setConsultantSessionValues(sessions, messagesReadMap, roomMessageMap,
        consultant.getRocketChatId());
    /**
     * Sort the session list by latest Rocket.Chat message if session is in progress (no enquiry).
     * The latest answer is on top.
     */
    sessions.sort(Comparator.comparing(ConsultantSessionResponseDTO::getLatestMessage).reversed());

    if (sessionFilter.equals(SessionFilter.FEEDBACK)) {
      sessions = removeAllChatsAndSessionsWithoutUnreadFeedback(sessions);
    }

    List<ConsultantSessionResponseDTO> sessionSublist =
        getSessionSublistByOffsetAndCount(sessions, offset, count);

    return new ConsultantSessionListResponseDTO(sessionSublist, offset, sessionSublist.size(),
        sessions.size());
  }

  /**
   * Get a sublist of a session list with offset and count
   *
   * @param sessions
   * @param offset
   * @param count
   * @return a sublist of the given session list
   */
  private List<ConsultantSessionResponseDTO> getSessionSublistByOffsetAndCount(
      List<ConsultantSessionResponseDTO> sessions, int offset, int count) {

    if (sessions == null) {
      return null;
    }

    int indexFrom = offset;
    int indexTo = (offset + count > sessions.size() ? sessions.size() : offset + count);

    return sessions.subList(indexFrom, indexTo);

  }

  /**
   * Remove chats and all sessions which don't have unread feedback chat messages from the given
   * session list
   *
   * @param sessions
   * @return
   */
  private List<ConsultantSessionResponseDTO> removeAllChatsAndSessionsWithoutUnreadFeedback(
      List<ConsultantSessionResponseDTO> sessions) {

    if (sessions == null) {
      return null;
    }

    Iterator<ConsultantSessionResponseDTO> sessionIterator = sessions.iterator();
    while (sessionIterator.hasNext()) {
      ConsultantSessionResponseDTO consultantSessionResponseDTO = sessionIterator.next();
      if (consultantSessionResponseDTO.getChat() != null
          || consultantSessionResponseDTO.getSession().isFeedbackRead()) {
        sessionIterator.remove();
      }
    }

    return sessions;

  }

  /**
   * Sets the values for messagesRead, latestMessage and monitoring for the specified list of {@link
   * ConsultantSessionResponseDTO} and decrypts and truncates the room's last message.
   *
   * @param sessions {@link List} of {@link ConsultantSessionResponseDTO}
   * @param messagesReadMap Map<String, Boolean>
   * @param roomMessageMap Map<String, Boolean>
   * @param rcUserId Rocket.Chat Id
   * @return
   */
  private List<ConsultantSessionResponseDTO> setConsultantSessionValues(
      List<ConsultantSessionResponseDTO> sessions, Map<String, Boolean> messagesReadMap,
      Map<String, RoomsLastMessageDTO> roomMessageMap, String rcUserId) {

    for (ConsultantSessionResponseDTO session : sessions) {

      if (session.getSession() == null) {
        continue;
      }

      session.getSession().setMonitoring(getMonitoringProperty(session.getSession()));

      if (messagesReadMap.containsKey(session.getSession().getGroupId())) {
        session.getSession()
            .setMessagesRead(messagesReadMap.get(session.getSession().getGroupId()));
      } else {
        // Fallback: If map doesn't contain feedback group id messagesRead is false to ensure that
        // nothing will be missed
        session.getSession().setMessagesRead(false);
      }

      if (roomMessageMap.containsKey(session.getSession().getGroupId())) {
        RoomsLastMessageDTO roomsLastMessage =
            roomMessageMap.get(session.getSession().getGroupId());
        session.getSession().setLastMessage(
            !StringUtils.isEmpty(roomsLastMessage.getMessage()) ? decryptAndTruncateMessage(
                roomsLastMessage.getMessage(), session.getSession().getGroupId()) : null);
        session.getSession().setMessageDate(Helper.getUnixTimestampFromDate(
            roomMessageMap.get(session.getSession().getGroupId()).getTimestamp()));
        session.setLatestMessage(roomsLastMessage.getTimestamp());
        // Attachment
        if (roomsLastMessage.getFile() != null) {
          session.getSession()
              .setAttachment(new SessionAttachmentDTO(roomsLastMessage.getFile().getType(),
                  roomsLastMessage.getAttachements()[0].getImagePreview(),
                  !rcUserId.equals(roomsLastMessage.getUser().getId())));
        }
      } else {
        // Fallback: If map doesn't contain group id messagedate is set to 1970-01-01
        session.getSession().setMessageDate(Helper.UNIXTIME_0.getTime());
        session.setLatestMessage(Helper.UNIXTIME_0);
      }

      if (session.getSession().getFeedbackGroupId() == null) {
        session.getSession().setFeedbackRead(true);
      }

      // Due to a Rocket.Chat bug the read state is is only set, when a message was posted
      if (roomMessageMap.containsKey(session.getSession().getFeedbackGroupId())
          && messagesReadMap.containsKey(session.getSession().getFeedbackGroupId())) {
        session.getSession()
            .setFeedbackRead(messagesReadMap.get(session.getSession().getFeedbackGroupId()));
      } else {
        // Fallback: If map doesn't contain feedback group id feedbackRead is false to ensure that
        // nothing will be missed
        if (!roomMessageMap.containsKey(session.getSession().getFeedbackGroupId())) {
          session.getSession().setFeedbackRead(true);
        } else {
          session.getSession().setFeedbackRead(false);
        }
      }

    }

    return sessions;
  }

  /**
   * Sets the values for messagesRead, latestMessage and monitoring for the specified list of {@link
   * ConsultantSessionResponseDTO} and decrypts and truncates the room's last message.
   *
   * @param chats {@link List} of {@link ConsultantSessionResponseDTO}
   * @param messagesReadMap Map<String, Boolean>
   * @param roomMessageMap Map<String, Boolean>
   * @param userRoomsList {@link List} of String
   * @param rcUserId Rocket.Chat user ID
   * @return {@link List} of {@link ConsultantSessionResponseDTO}
   */
  private List<ConsultantSessionResponseDTO> setConsultantChatValues(
      List<ConsultantSessionResponseDTO> chats, Map<String, Boolean> messagesReadMap,
      Map<String, RoomsLastMessageDTO> roomMessageMap, List<String> userRoomsList,
      String rcUserId) {

    for (ConsultantSessionResponseDTO chat : chats) {

      if (chat.getChat() == null) {
        continue;
      }

      if (userRoomsList != null && userRoomsList.contains(chat.getChat().getGroupId())) {
        chat.getChat().setSubscribed(true);
      } else {
        chat.getChat().setSubscribed(false);
      }

      if (messagesReadMap.containsKey(chat.getChat().getGroupId())) {
        chat.getChat().setMessagesRead(messagesReadMap.get(chat.getChat().getGroupId()));
      } else {
        chat.getChat().setMessagesRead(true);
      }

      if (roomMessageMap.containsKey(chat.getChat().getGroupId())) {
        RoomsLastMessageDTO roomsLastMessage = roomMessageMap.get(chat.getChat().getGroupId());
        chat.getChat().setLastMessage(!StringUtils.isEmpty(roomsLastMessage.getMessage())
            ? decryptAndTruncateMessage(roomsLastMessage.getMessage(), chat.getChat().getGroupId())
            : null);
        chat.getChat().setMessageDate(Helper.getUnixTimestampFromDate(
            roomMessageMap.get(chat.getChat().getGroupId()).getTimestamp()));
        chat.setLatestMessage(roomsLastMessage.getTimestamp());
        // Attachment
        if (roomsLastMessage.getFile() != null) {
          chat.getChat()
              .setAttachment(new SessionAttachmentDTO(roomsLastMessage.getFile().getType(),
                  roomsLastMessage.getAttachements()[0].getImagePreview(),
                  !rcUserId.equals(roomsLastMessage.getUser().getId())));
        }
      } else {
        chat.setLatestMessage(Timestamp.valueOf(chat.getChat().getStartDateWithTime()));
      }
    }

    return chats;
  }

  /**
   * Get a map with the read-status for each room id
   *
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return
   */
  private Map<String, Boolean> getMessagesReadMap(RocketChatCredentials rocketChatCredentials) {
    List<SubscriptionsUpdateDTO> subscriptions;
    try {
      subscriptions = rocketChatService.getSubscriptionsOfUser(rocketChatCredentials);
    } catch (RocketChatGetSubscriptionsException rocketChatGetSubscriptionsException) {
      throw new ServiceException(rocketChatGetSubscriptionsException.getMessage(),
          rocketChatGetSubscriptionsException);
    }
    Map<String, Boolean> messagesReadMap = new HashMap<String, Boolean>();
    for (SubscriptionsUpdateDTO subscription : subscriptions) {
      messagesReadMap.put(subscription.getRoomId(),
          (subscription.getUnread() != null && subscription.getUnread() == 0) ? true : false);
    }
    return messagesReadMap;
  }

  /**
   * Get the rooms update list for a user from RocketChat
   *
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return
   */
  private List<RoomsUpdateDTO> getRcRoomsUpdateList(RocketChatCredentials rocketChatCredentials) {
    try {
      return rocketChatService.getRoomsOfUser(rocketChatCredentials);
    } catch (RocketChatGetRoomsException rocketChatGetRoomsException) {
      throw new ServiceException(rocketChatGetRoomsException.getMessage(),
          rocketChatGetRoomsException);
    }
  }

  /**
   * Get a map with the last Rocket.Chat message and its date for each room id
   *
   * @param roomsUpdateList {@link List} of {@link RoomsUpdateDTO}
   * @return
   */
  private Map<String, RoomsLastMessageDTO> getRcRoomMessageMap(
      List<RoomsUpdateDTO> roomsUpdateList) {

    if (roomsUpdateList == null) {
      return new HashMap<String, RoomsLastMessageDTO>();
    }

    Map<String, RoomsLastMessageDTO> messageDateMap = new HashMap<String, RoomsLastMessageDTO>();
    for (RoomsUpdateDTO room : roomsUpdateList) {
      if (room.getLastMessage() != null && room.getLastMessage().getTimestamp() != null) {
        messageDateMap.put(room.getId(), room.getLastMessage());
      }
    }
    return messageDateMap;
  }

  /**
   * Decrypts and returns a Rocket.Chat message and truncates it to a maximum length
   *
   * @param message Encrypted message
   * @param groupId Rocket.Chat group id of the message
   * @return Decrypted message
   */
  private String decryptAndTruncateMessage(String message, String groupId) {

    if (message == null) {
      return null;
    }

    try {
      String decryptedMessage = decryptionService.decrypt(message, groupId);

      return decryptedMessage.substring(truncateStart,
          decryptedMessage.length() < truncateEnd ? decryptedMessage.length() : truncateEnd);

    } catch (CustomCryptoException cryptoEx) {
      LogService.logDecryptionError(
          String.format("Could not decrypt message for group id %s", groupId), cryptoEx);
    } catch (IndexOutOfBoundsException substringEx) {
      LogService.logTruncationError(
          String.format("Could not truncate message for group id %s", groupId), substringEx);
    }

    return null;

  }

  /**
   * Returns the monitoring property (which is set in the {@link ConsultingTypeSettings}) for the
   * given session.
   *
   * @param session
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
