package de.caritas.cob.userservice.api.service.sessionlist;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.facade.getsessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.SessionFilter;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.collections.CollectionUtils;

@Service
public class ConsultantSessionListService {

  private final SessionService sessionService;
  private final ChatService chatService;
  private final RocketChatRoomInformationProvider rocketChatRoomInformationProvider;
  private final ConsultingTypeManager consultingTypeManager;
  private final SessionListHelper sessionListHelper;

  @Autowired
  public ConsultantSessionListService(
      SessionService sessionService, ChatService chatService,
      RocketChatRoomInformationProvider rocketChatRoomInformationProvider,
      ConsultingTypeManager consultingTypeManager,
      SessionListHelper sessionListHelper) {
    this.sessionService = sessionService;
    this.chatService = chatService;
    this.rocketChatRoomInformationProvider = rocketChatRoomInformationProvider;
    this.consultingTypeManager = consultingTypeManager;
    this.sessionListHelper = sessionListHelper;
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the specified consultant id and
   * status.
   *
   * @param consultant                {@link Consultant}
   * @param rcAuthToken               Rocket.Chat Token
   * @param sessionListQueryParameter session list query parameters as {@link SessionListQueryParameter}
   * @return the response dto
   */
  public List<ConsultantSessionResponseDTO> getSessionsForAuthenticatedConsultant(
      Consultant consultant, String rcAuthToken,
      SessionListQueryParameter sessionListQueryParameter) {

    List<ConsultantSessionResponseDTO> sessions = sessionService
        .getSessionsForConsultant(consultant, sessionListQueryParameter.getSessionStatus());
    List<ConsultantSessionResponseDTO> chats = new ArrayList<>();

    if (SessionStatus.isStatusValueInProgress(sessionListQueryParameter.getSessionStatus())) {
      chats = chatService.getChatsForConsultant(consultant);
    }

    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .RocketChatUserId(consultant.getRocketChatId()).RocketChatToken(rcAuthToken).build();

    return retrieveMergedListOfConsultantSessionsAndChats(
        consultant, sessions, chats, rocketChatCredentials);
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the specified consultant id.
   *
   * @param consultant                the {@link Consultant}
   * @param rcAuthToken               the Rocket.Chat auth token
   * @param sessionListQueryParameter session list query parameters as {@link SessionListQueryParameter}
   * @return a {@link ConsultantSessionListResponseDTO} with a {@link List} of {@link
   * ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO> getTeamSessionsForAuthenticatedConsultant(Consultant
      consultant, String rcAuthToken, SessionListQueryParameter sessionListQueryParameter) {

    List<ConsultantSessionResponseDTO> teamSessions =
        sessionService.getTeamSessionsForConsultant(consultant);

    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .RocketChatUserId(consultant.getRocketChatId()).RocketChatToken(rcAuthToken).build();
    RocketChatRoomInformation rocketChatRoomInformation = rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(rocketChatCredentials);
    setConsultantSessionValues(teamSessions, rocketChatRoomInformation,
        consultant.getRocketChatId());

    sortSessionsByLastMessageDateDesc(teamSessions);

    if (sessionListQueryParameter.getSessionFilter().equals(SessionFilter.FEEDBACK)) {
      removeAllChatsAndSessionsWithoutUnreadFeedback(teamSessions);
    }

    return teamSessions;
  }

  private List<ConsultantSessionResponseDTO> retrieveMergedListOfConsultantSessionsAndChats(
      Consultant consultant, List<ConsultantSessionResponseDTO> sessions,
      List<ConsultantSessionResponseDTO> chats, RocketChatCredentials rocketChatCredentials) {
    List<ConsultantSessionResponseDTO> allSessions = new ArrayList<>();

    RocketChatRoomInformation rocketChatRoomInformation = rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(rocketChatCredentials);

    if (CollectionUtils.isNotEmpty(sessions)) {
      allSessions.addAll(setConsultantSessionValues(sessions, rocketChatRoomInformation,
          consultant.getRocketChatId()));
    }

    if (CollectionUtils.isNotEmpty(chats)) {
      allSessions.addAll(
          setConsultantChatValues(chats, rocketChatRoomInformation, consultant.getRocketChatId()));
    }
    return allSessions;
  }

  private void sortSessionsByLastMessageDateDesc(List<ConsultantSessionResponseDTO> sessions) {
    sessions.sort(Comparator.comparing(ConsultantSessionResponseDTO::getLatestMessage).reversed());
  }

  private void removeAllChatsAndSessionsWithoutUnreadFeedback(
      List<ConsultantSessionResponseDTO> sessions) {

    sessions.removeIf(
        consultantSessionResponseDTO -> nonNull(consultantSessionResponseDTO.getChat())
            || consultantSessionResponseDTO.getSession().isFeedbackRead());
  }

  private List<ConsultantSessionResponseDTO> setConsultantSessionValues(
      List<ConsultantSessionResponseDTO> sessions,
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId) {

    for (ConsultantSessionResponseDTO session : sessions) {

      String groupId = session.getSession().getGroupId();

      session.getSession().setMonitoring(getMonitoringProperty(session.getSession()));

      // Fallback: If map doesn't contain feedback group id messagesRead is false to ensure that
      // nothing will be missed
      session.getSession()
          .setMessagesRead(rocketChatRoomInformation
              .getMessagesReadMap().getOrDefault(groupId, false));

      if (sessionListHelper.isLastMessageForRocketChatGroupIdAvailable(
          rocketChatRoomInformation.getRoomLastMessageMap(),
          groupId)) {
        RoomsLastMessageDTO roomsLastMessage =
            rocketChatRoomInformation.getRoomLastMessageMap()
                .get(groupId);
        session.getSession().setLastMessage(
            !StringUtils.isEmpty(roomsLastMessage.getMessage()) ? sessionListHelper
                .prepareMessageForSessionList(
                    roomsLastMessage.getMessage(), groupId) : null);
        session.getSession().setMessageDate(Helper.getUnixTimestampFromDate(
            rocketChatRoomInformation.getRoomLastMessageMap().get(groupId)
                .getTimestamp()));
        session.setLatestMessage(roomsLastMessage.getTimestamp());
        session.getSession()
            .setAttachment(
                sessionListHelper
                    .getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
      } else {
        // Fallback: If map doesn't contain group id messagedate is set to 1970-01-01
        session.getSession().setMessageDate(Helper.UNIXTIME_0.getTime());
        session.setLatestMessage(Helper.UNIXTIME_0);
      }

      // Due to a Rocket.Chat bug the read state is is only set, when a message was posted
      if (isFeedbackFlagAvailable(rocketChatRoomInformation, session)) {
        session.getSession()
            .setFeedbackRead(
                rocketChatRoomInformation.getMessagesReadMap()
                    .get(session.getSession().getFeedbackGroupId()));
      } else {
        // Fallback: If map doesn't contain feedback group id feedbackRead is false to ensure that
        // nothing will be missed
        session.getSession().setFeedbackRead(
            !rocketChatRoomInformation.getRoomLastMessageMap()
                .containsKey(session.getSession().getFeedbackGroupId()));
      }

    }

    return sessions;
  }

  private List<ConsultantSessionResponseDTO> setConsultantChatValues(
      List<ConsultantSessionResponseDTO> chats, RocketChatRoomInformation rocketChatRoomInformation,
      String rcUserId) {

    for (ConsultantSessionResponseDTO chat : chats) {

      String groupId = chat.getChat().getGroupId();

      chat.getChat().setSubscribed(
          isRoomSubscribedByConsultant(rocketChatRoomInformation.getUserRoomList(), chat));
      chat.getChat()
          .setMessagesRead(rocketChatRoomInformation
              .getMessagesReadMap().getOrDefault(chat.getChat().getGroupId(), true));

      if (sessionListHelper.isLastMessageForRocketChatGroupIdAvailable(
          rocketChatRoomInformation.getRoomLastMessageMap(), groupId)) {
        RoomsLastMessageDTO roomsLastMessage = rocketChatRoomInformation
            .getRoomLastMessageMap().get(groupId);
        chat.getChat().setLastMessage(!StringUtils.isEmpty(roomsLastMessage.getMessage())
            ? sessionListHelper
            .prepareMessageForSessionList(roomsLastMessage.getMessage(),
                groupId)
            : null);
        chat.getChat().setMessageDate(Helper.getUnixTimestampFromDate(
            rocketChatRoomInformation.getRoomLastMessageMap().get(groupId)
                .getTimestamp()));
        chat.setLatestMessage(roomsLastMessage.getTimestamp());
        chat.getChat()
            .setAttachment(
                sessionListHelper
                    .getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
      } else {
        chat.setLatestMessage(Timestamp.valueOf(chat.getChat().getStartDateWithTime()));
      }
    }

    return chats;
  }

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

  private boolean isFeedbackFlagAvailable(RocketChatRoomInformation rocketChatRoomInformation,
      ConsultantSessionResponseDTO session) {
    return rocketChatRoomInformation.getRoomLastMessageMap()
        .containsKey(session.getSession().getFeedbackGroupId())
        && rocketChatRoomInformation.getMessagesReadMap()
        .containsKey(session.getSession().getFeedbackGroupId());
  }

  private boolean isRoomSubscribedByConsultant(List<String> userRoomsList,
      ConsultantSessionResponseDTO chat) {
    return nonNull(userRoomsList) && userRoomsList.contains(chat.getChat().getGroupId());
  }
}
