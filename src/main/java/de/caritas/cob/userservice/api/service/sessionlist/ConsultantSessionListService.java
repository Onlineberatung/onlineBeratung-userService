package de.caritas.cob.userservice.api.service.sessionlist;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.chat.UserChatDTO;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsLastMessageDTO;
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
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ConsultantSessionListService {

  private final @NonNull SessionService sessionService;
  private final @NonNull ChatService chatService;
  private final @NonNull RocketChatRoomInformationProvider rocketChatRoomInformationProvider;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull SessionListAnalyser sessionListAnalyser;

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the specified consultant id and
   * status.
   *
   * @param consultant {@link Consultant}
   * @param rcAuthToken Rocket.Chat Token
   * @param sessionListQueryParameter session list query parameters as {@link
   * SessionListQueryParameter}
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
        .rocketChatUserId(consultant.getRocketChatId())
        .rocketChatToken(rcAuthToken)
        .build();

    return mergeConsultantSessionsAndChats(consultant, sessions, chats, rocketChatCredentials);
  }

  /**
   * Returns a list of {@link ConsultantSessionResponseDTO} for the specified consultant id.
   *
   * @param consultant the {@link Consultant}
   * @param rcAuthToken the Rocket.Chat auth token
   * @param sessionListQueryParameter session list query parameters as {@link
   * SessionListQueryParameter}
   * @return a {@link ConsultantSessionListResponseDTO} with a {@link List} of {@link
   * ConsultantSessionResponseDTO}
   */
  public List<ConsultantSessionResponseDTO> retrieveTeamSessionsForAuthenticatedConsultant(
      Consultant consultant, String rcAuthToken,
      SessionListQueryParameter sessionListQueryParameter) {

    List<ConsultantSessionResponseDTO> teamSessions =
        sessionService.getTeamSessionsForConsultant(consultant);

    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .rocketChatUserId(consultant.getRocketChatId())
        .rocketChatToken(rcAuthToken)
        .build();
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

  private List<ConsultantSessionResponseDTO> mergeConsultantSessionsAndChats(
      Consultant consultant, List<ConsultantSessionResponseDTO> sessions,
      List<ConsultantSessionResponseDTO> chats, RocketChatCredentials rocketChatCredentials) {
    List<ConsultantSessionResponseDTO> allSessions = new ArrayList<>();

    RocketChatRoomInformation rocketChatRoomInformation = rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(rocketChatCredentials);

    if (isNotEmpty(sessions)) {
      allSessions.addAll(updateConsultantSessionValues(sessions, rocketChatRoomInformation,
          consultant.getRocketChatId()));
    }

    if (isNotEmpty(chats)) {
      allSessions.addAll(
          updateConsultantChatValues(chats, rocketChatRoomInformation,
              consultant.getRocketChatId()));
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
            || isTrue(consultantSessionResponseDTO.getSession().getFeedbackRead()));
  }

  private List<ConsultantSessionResponseDTO> updateConsultantSessionValues(
      List<ConsultantSessionResponseDTO> sessions,
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId) {

    return sessions.stream()
        .map(dto -> updateRequiredConsultantSessionValues(rocketChatRoomInformation, rcUserId, dto))
        .collect(Collectors.toList());
  }

  private ConsultantSessionResponseDTO updateRequiredConsultantSessionValues(
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId,
      ConsultantSessionResponseDTO consultantSessionResponseDTO) {
    SessionDTO session = consultantSessionResponseDTO.getSession();
    String groupId = session.getGroupId();

    session.setMonitoring(getMonitoringProperty(session));

    session.setMessagesRead(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
        rocketChatRoomInformation.getReadMessages(), groupId));

    if (sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        rocketChatRoomInformation.getLastMessagesRoom(), groupId)) {
      new AvailableLastMessageUpdater(this.sessionListAnalyser)
          .updateSessionWithAvailableLastMessage(rocketChatRoomInformation, rcUserId,
              consultantSessionResponseDTO::setLatestMessage, session, groupId);
    } else {
      setFallbackDate(consultantSessionResponseDTO, session);
    }

    // Due to a Rocket.Chat bug the read state is only set, when a message was posted
    if (isFeedbackFlagAvailable(rocketChatRoomInformation, consultantSessionResponseDTO)) {
      session.setFeedbackRead(
          rocketChatRoomInformation.getReadMessages().get(session.getFeedbackGroupId()));
    } else {
      // Fallback: If map doesn't contain feedback group id set to true -> no feedback label in frontend application
      session.setFeedbackRead(!rocketChatRoomInformation.getLastMessagesRoom()
          .containsKey(session.getFeedbackGroupId()));
    }
    return consultantSessionResponseDTO;
  }

  private void setFallbackDate(ConsultantSessionResponseDTO consultantSessionResponseDTO,
      SessionDTO session) {
    session.setMessageDate(Helper.UNIXTIME_0.getTime());
    consultantSessionResponseDTO.setLatestMessage(Helper.UNIXTIME_0);
  }

  private List<ConsultantSessionResponseDTO> updateConsultantChatValues(
      List<ConsultantSessionResponseDTO> chats, RocketChatRoomInformation rocketChatRoomInformation,
      String rcUserId) {

    return chats.stream()
        .map(chat -> updateRequiredChatValues(rocketChatRoomInformation, rcUserId, chat))
        .collect(Collectors.toList());
  }

  private ConsultantSessionResponseDTO updateRequiredChatValues(
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId,
      ConsultantSessionResponseDTO consultantSessionResponseDTO) {
    UserChatDTO chat = consultantSessionResponseDTO.getChat();
    String groupId = consultantSessionResponseDTO.getChat().getGroupId();

    chat.setSubscribed(isRoomSubscribedByConsultant(rocketChatRoomInformation.getUserRooms(),
        consultantSessionResponseDTO));
    chat.setMessagesRead(rocketChatRoomInformation
        .getReadMessages().getOrDefault(chat.getGroupId(), true));

    if (sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        rocketChatRoomInformation.getLastMessagesRoom(), groupId)) {
      updateChatWithAvailableLastMessage(rocketChatRoomInformation, rcUserId,
          consultantSessionResponseDTO, chat, groupId);
    } else {
      consultantSessionResponseDTO.setLatestMessage(Timestamp.valueOf(chat.getStartDateWithTime()));
    }
    return consultantSessionResponseDTO;
  }

  private void updateChatWithAvailableLastMessage(
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId,
      ConsultantSessionResponseDTO dto, UserChatDTO chat, String groupId) {
    RoomsLastMessageDTO roomsLastMessage = rocketChatRoomInformation
        .getLastMessagesRoom().get(groupId);
    chat.setLastMessage(isNotBlank(roomsLastMessage.getMessage()) ? sessionListAnalyser
        .prepareMessageForSessionList(roomsLastMessage.getMessage(), groupId) : null);
    chat.setMessageDate(Helper
        .getUnixTimestampFromDate(rocketChatRoomInformation.getLastMessagesRoom().get(groupId)
            .getTimestamp()));
    dto.setLatestMessage(roomsLastMessage.getTimestamp());
    chat.setAttachment(sessionListAnalyser
        .getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
  }

  private boolean getMonitoringProperty(SessionDTO session) {

    Optional<ConsultingType> consultingType = ConsultingType.valueOf(session.getConsultingType());

    if (!consultingType.isPresent()) {
      throw new ServiceException(String
          .format("Session with id %s does not have a valid consulting type.", session.getId()));
    }

    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultingTypeSettings(consultingType.get());

    return consultingTypeSettings.isMonitoring();
  }

  private boolean isFeedbackFlagAvailable(RocketChatRoomInformation rocketChatRoomInformation,
      ConsultantSessionResponseDTO session) {
    return rocketChatRoomInformation.getLastMessagesRoom()
        .containsKey(session.getSession().getFeedbackGroupId())
        && rocketChatRoomInformation.getReadMessages()
        .containsKey(session.getSession().getFeedbackGroupId());
  }

  private boolean isRoomSubscribedByConsultant(List<String> userRoomsList,
      ConsultantSessionResponseDTO chat) {
    return nonNull(userRoomsList) && userRoomsList.contains(chat.getChat().getGroupId());
  }
}
