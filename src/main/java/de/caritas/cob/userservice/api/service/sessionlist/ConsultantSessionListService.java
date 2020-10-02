package de.caritas.cob.userservice.api.service.sessionlist;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.chat.UserChatDTO;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    this.sessionService = requireNonNull(sessionService);
    this.chatService = requireNonNull(chatService);
    this.rocketChatRoomInformationProvider = requireNonNull(rocketChatRoomInformationProvider);
    this.consultingTypeManager = requireNonNull(consultingTypeManager);
    this.sessionListHelper = requireNonNull(sessionListHelper);
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
  public List<ConsultantSessionResponseDTO> retrieveSessionsForAuthenticatedConsultant(
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
  public List<ConsultantSessionResponseDTO> retrieveTeamSessionsForAuthenticatedConsultant(Consultant
      consultant, String rcAuthToken, SessionListQueryParameter sessionListQueryParameter) {

    List<ConsultantSessionResponseDTO> teamSessions =
        sessionService.getTeamSessionsForConsultant(consultant);

    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .RocketChatUserId(consultant.getRocketChatId()).RocketChatToken(rcAuthToken).build();
    RocketChatRoomInformation rocketChatRoomInformation = rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(rocketChatCredentials);
    updateConsultantSessionValues(teamSessions, rocketChatRoomInformation,
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
      allSessions.addAll(updateConsultantSessionValues(sessions, rocketChatRoomInformation,
          consultant.getRocketChatId()));
    }

    if (CollectionUtils.isNotEmpty(chats)) {
      allSessions.addAll(
          updateConsultantChatValues(chats, rocketChatRoomInformation, consultant.getRocketChatId()));
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

  private List<ConsultantSessionResponseDTO> updateConsultantSessionValues(
      List<ConsultantSessionResponseDTO> sessions,
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId) {

    for (ConsultantSessionResponseDTO dto : sessions) {

      SessionDTO session = dto.getSession();
      String groupId = session.getGroupId();

      session.setMonitoring(getMonitoringProperty(session));

      session.setMessagesRead(
          sessionListHelper.isMessagesForRocketChatGroupReadByUser(
              rocketChatRoomInformation.getMessagesReadMap(),
              groupId));

      if (sessionListHelper.isLastMessageForRocketChatGroupIdAvailable(
          rocketChatRoomInformation.getRoomLastMessageMap(),
          groupId)) {
        RoomsLastMessageDTO roomsLastMessage =
            rocketChatRoomInformation.getRoomLastMessageMap()
                .get(groupId);
        session.setLastMessage(
            !StringUtils.isEmpty(roomsLastMessage.getMessage()) ? sessionListHelper
                .prepareMessageForSessionList(
                    roomsLastMessage.getMessage(), groupId) : null);
        session.setMessageDate(Helper.getUnixTimestampFromDate(
            rocketChatRoomInformation.getRoomLastMessageMap().get(groupId)
                .getTimestamp()));
        dto.setLatestMessage(roomsLastMessage.getTimestamp());
        session
            .setAttachment(
                sessionListHelper
                    .getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
      } else {
        // Fallback: If map doesn't contain group id messagedate is set to 1970-01-01
        session.setMessageDate(Helper.UNIXTIME_0.getTime());
        dto.setLatestMessage(Helper.UNIXTIME_0);
      }

      // Due to a Rocket.Chat bug the read state is is only set, when a message was posted
      if (isFeedbackFlagAvailable(rocketChatRoomInformation, dto)) {
        session
            .setFeedbackRead(
                rocketChatRoomInformation.getMessagesReadMap()
                    .get(session.getFeedbackGroupId()));
      } else {
        // Fallback: If map doesn't contain feedback group id set to true -> no feedback label in frontend application
        session.setFeedbackRead(!rocketChatRoomInformation.getRoomLastMessageMap()
            .containsKey(session.getFeedbackGroupId()));
      }
    }

    return sessions;
  }

  private List<ConsultantSessionResponseDTO> updateConsultantChatValues(
      List<ConsultantSessionResponseDTO> chats, RocketChatRoomInformation rocketChatRoomInformation,
      String rcUserId) {

    for (ConsultantSessionResponseDTO dto : chats) {

      UserChatDTO chat = dto.getChat();
      String groupId = dto.getChat().getGroupId();

      chat.setSubscribed(
          isRoomSubscribedByConsultant(rocketChatRoomInformation.getUserRoomList(), dto));
      chat.setMessagesRead(rocketChatRoomInformation
          .getMessagesReadMap().getOrDefault(chat.getGroupId(), true));

      if (sessionListHelper.isLastMessageForRocketChatGroupIdAvailable(
          rocketChatRoomInformation.getRoomLastMessageMap(), groupId)) {
        RoomsLastMessageDTO roomsLastMessage = rocketChatRoomInformation
            .getRoomLastMessageMap().get(groupId);
        chat.setLastMessage(!StringUtils.isEmpty(roomsLastMessage.getMessage())
            ? sessionListHelper
            .prepareMessageForSessionList(roomsLastMessage.getMessage(), groupId)
            : null);
        chat.setMessageDate(Helper
            .getUnixTimestampFromDate(rocketChatRoomInformation.getRoomLastMessageMap().get(groupId)
                .getTimestamp()));
        dto.setLatestMessage(roomsLastMessage.getTimestamp());
        chat
            .setAttachment(
                sessionListHelper
                    .getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
      } else {
        dto.setLatestMessage(Timestamp.valueOf(chat.getStartDateWithTime()));
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
