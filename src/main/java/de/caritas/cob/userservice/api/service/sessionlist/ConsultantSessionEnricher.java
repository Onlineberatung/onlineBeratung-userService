package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.api.repository.session.RegistrationType.ANONYMOUS;
import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toDate;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class to enrich a session of an consultant with required Rocket.Chat data.
 */
@Service
@RequiredArgsConstructor
public class ConsultantSessionEnricher {

  private final @NonNull SessionListAnalyser sessionListAnalyser;
  private final @NonNull RocketChatRoomInformationProvider rocketChatRoomInformationProvider;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull AvailableLastMessageUpdater availableLastMessageUpdater;

  /**
   * Enriches the given session with the following information from Rocket.Chat: "last message",
   * "last message date", and "messages read".
   *
   * @param consultantSessionResponseDTOs the session list to be enriched
   * @param rcToken                       the Rocket.Chat authentication token of the current
   *                                      consultant
   * @param consultant                    the {@link Consultant}
   * @return the enriched {@link ConsultantSessionResponseDTO}s
   */
  public List<ConsultantSessionResponseDTO> updateRequiredConsultantSessionValues(
      List<ConsultantSessionResponseDTO> consultantSessionResponseDTOs, String rcToken,
      Consultant consultant) {

    var rocketChatCredentials = RocketChatCredentials.builder()
        .rocketChatToken(rcToken)
        .rocketChatUserId(consultant.getRocketChatId())
        .build();

    consultantSessionResponseDTOs.forEach(
        consultantSessionResponseDTO -> this.enrichConsultantSession(consultantSessionResponseDTO,
            rocketChatCredentials));

    return consultantSessionResponseDTOs;
  }

  private void enrichConsultantSession(ConsultantSessionResponseDTO consultantSessionResponseDTO,
      RocketChatCredentials rocketChatCredentials) {
    SessionDTO session = consultantSessionResponseDTO.getSession();
    String groupId = session.getGroupId();
    session.setMonitoring(getMonitoringProperty(session));

    var rocketChatRoomInformation = this.rocketChatRoomInformationProvider.retrieveRocketChatInformation(
        rocketChatCredentials);
    session.setMessagesRead(sessionListAnalyser.areMessagesForRocketChatGroupReadByUser(
        rocketChatRoomInformation.getReadMessages(), groupId));

    if (sessionListAnalyser.isLastMessageForRocketChatGroupIdAvailable(
        rocketChatRoomInformation.getLastMessagesRoom(), groupId)) {
      availableLastMessageUpdater.updateSessionWithAvailableLastMessage(rocketChatRoomInformation,
          consultantSessionResponseDTO::setLatestMessage, session, rocketChatCredentials);
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
  }

  private boolean getMonitoringProperty(SessionDTO session) {
    var extendedConsultingTypeResponseDTO = consultingTypeManager.getConsultingTypeSettings(
        session.getConsultingType());

    return nonNull(extendedConsultingTypeResponseDTO) && nonNull(
        extendedConsultingTypeResponseDTO.getMonitoring()) && isTrue(
        extendedConsultingTypeResponseDTO.getMonitoring().getInitializeMonitoring());
  }

  private boolean isFeedbackFlagAvailable(RocketChatRoomInformation rocketChatRoomInformation,
      ConsultantSessionResponseDTO session) {
    return rocketChatRoomInformation.getLastMessagesRoom()
        .containsKey(session.getSession().getFeedbackGroupId())
        && rocketChatRoomInformation.getReadMessages()
        .containsKey(session.getSession().getFeedbackGroupId());
  }

  private void setFallbackDate(ConsultantSessionResponseDTO consultantSessionResponseDTO,
      SessionDTO session) {
    session.setMessageDate(Helper.UNIXTIME_0.getTime());
    if (ANONYMOUS.name().equals(session.getRegistrationType())) {
      consultantSessionResponseDTO.setLatestMessage(toDate(session.getCreateDate()));
    } else {
      consultantSessionResponseDTO.setLatestMessage(Helper.UNIXTIME_0);
    }
  }

}
