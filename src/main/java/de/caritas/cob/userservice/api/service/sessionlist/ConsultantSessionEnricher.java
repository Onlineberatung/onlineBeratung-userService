package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toDate;
import static de.caritas.cob.userservice.api.repository.session.RegistrationType.ANONYMOUS;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
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

    var rocketChatRoomInformation = this.rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(RocketChatCredentials.builder()
            .rocketChatToken(rcToken)
            .rocketChatUserId(consultant.getRocketChatId())
            .build());

    consultantSessionResponseDTOs.forEach(consultantSessionResponseDTO -> this
        .enrichConsultantSession(consultantSessionResponseDTO, rocketChatRoomInformation,
            consultant));

    return consultantSessionResponseDTOs;
  }

  private void enrichConsultantSession(ConsultantSessionResponseDTO consultantSessionResponseDTO,
      RocketChatRoomInformation rocketChatRoomInformation, Consultant consultant) {
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
  }

  private boolean getMonitoringProperty(SessionDTO session) {

    var extendedConsultingTypeResponseDTO = consultingTypeManager
        .getConsultingTypeSettings(session.getConsultingType());

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
