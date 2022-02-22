package de.caritas.cob.userservice.api.service.sessionlist;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.facade.sessionlist.RocketChatRoomInformationProvider;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserChatDTO;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import java.sql.Timestamp;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class to enrich a session of an consultant with required Rocket.Chat data.
 */
@Service
@RequiredArgsConstructor
public class ConsultantChatEnricher {

  private final @NonNull SessionListAnalyser sessionListAnalyser;
  private final @NonNull RocketChatRoomInformationProvider rocketChatRoomInformationProvider;

  /**
   * Enriches the given session with the following information from Rocket.Chat. - last message -
   * last message date - messages read
   *
   * @param consultantSessionResponseDTOs the session list to be enriched
   * @param rcToken                       the Rocket.Chat authentiaction token of the current
   *                                      consultant
   * @param consultant                    the {@link Consultant}
   * @return the enriched {@link ConsultantSessionResponseDTO}s
   */
  public List<ConsultantSessionResponseDTO> updateRequiredConsultantChatValues(
      List<ConsultantSessionResponseDTO> consultantSessionResponseDTOs, String rcToken,
      Consultant consultant) {

    var rocketChatRoomInformation = this.rocketChatRoomInformationProvider
        .retrieveRocketChatInformation(RocketChatCredentials.builder()
            .rocketChatToken(rcToken)
            .rocketChatUserId(consultant.getRocketChatId())
            .build());

    consultantSessionResponseDTOs.forEach(
        consultantSessionResponseDTO -> updateRequiredChatValues(rocketChatRoomInformation,
            consultant.getRocketChatId(), consultantSessionResponseDTO));

    return consultantSessionResponseDTOs;
  }

  private void updateRequiredChatValues(
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
  }

  private boolean isRoomSubscribedByConsultant(List<String> userRoomsList,
      ConsultantSessionResponseDTO chat) {
    return nonNull(userRoomsList) && userRoomsList.contains(chat.getChat().getGroupId());
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

}
