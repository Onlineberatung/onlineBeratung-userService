package de.caritas.cob.userservice.api.conversation.service;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.sessionlist.AvailableLastMessageUpdater;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultantEnquiryUpdater {

  private final @NonNull SessionListAnalyser sessionListAnalyser;
  private final @NonNull RocketChatRoomInformationProvider rocketChatRoomInformationProvider;
  private final @NonNull ConsultingTypeManager consultingTypeManager;

  public ConsultantSessionResponseDTO updateRequiredConsultantSessionValues(
      ConsultantSessionResponseDTO consultantSessionResponseDTO, String rcToken,
      Consultant consultant) {

    var rocketChatRoomInformation = this.rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(RocketChatCredentials.builder()
            .rocketChatToken(rcToken)
            .rocketChatUserId(consultant.getRocketChatId())
            .build());

    SessionDTO session = consultantSessionResponseDTO.getSession();
    String groupId = session.getGroupId();

    session.setMonitoring(getMonitoringProperty(session));

    session.setMessagesRead(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
        rocketChatRoomInformation.getReadMessages(), groupId));

    if (sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        rocketChatRoomInformation.getLastMessagesRoom(), groupId)) {
      new AvailableLastMessageUpdater(this.sessionListAnalyser)
          .updateSessionWithAvailableLastMessage(rocketChatRoomInformation,
              consultant.getRocketChatId(), consultantSessionResponseDTO::setLatestMessage, session,
              groupId);
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

  private boolean getMonitoringProperty(SessionDTO session) {

    Optional<ConsultingType> consultingType = ConsultingType.valueOf(session.getConsultingType());

    if (consultingType.isEmpty()) {
      throw new ServiceException(String
          .format("Session with id %s does not have a valid consulting type.", session.getId()));
    }
    var consultingTypeSettings =
        consultingTypeManager.getConsultingTypeSettings(consultingType.get());

    return consultingTypeSettings.isMonitoring();
  }

  private void setFallbackDate(ConsultantSessionResponseDTO consultantSessionResponseDTO,
      SessionDTO session) {
    session.setMessageDate(Helper.UNIXTIME_0.getTime());
    consultantSessionResponseDTO.setLatestMessage(Helper.UNIXTIME_0);
  }

  private boolean isFeedbackFlagAvailable(RocketChatRoomInformation rocketChatRoomInformation,
      ConsultantSessionResponseDTO session) {
    return rocketChatRoomInformation.getLastMessagesRoom()
        .containsKey(session.getSession().getFeedbackGroupId())
        && rocketChatRoomInformation.getReadMessages()
        .containsKey(session.getSession().getFeedbackGroupId());
  }

}
