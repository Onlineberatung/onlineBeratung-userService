package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.api.adapters.web.dto.MessageType.FURTHER_STEPS;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.LastMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import java.util.Date;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Updater singleton to update Rocket.Chat relevant data on session items.
 */
@RequiredArgsConstructor
public class AvailableLastMessageUpdater {

  private static final String FURTHER_STEPS_MESSAGE = "So geht es weiter";

  private final @NonNull SessionListAnalyser sessionListAnalyser;

  /**
   * Updates the given session with further Rocket.Chat last message information.
   *
   * @param rocketChatRoomInformation the {@link RocketChatRoomInformation}
   * @param rcUserId                  the Rocket.Chat user id
   * @param latestMessageSetter       the function to set the latest message timestamp
   * @param session                   the session to be updated
   * @param groupId                   the Rocket.Chat group id
   */
  void updateSessionWithAvailableLastMessage(
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId,
      Consumer<Date> latestMessageSetter, SessionDTO session, String groupId) {

    var roomsLastMessage = rocketChatRoomInformation.getLastMessagesRoom().get(groupId);
    session.setLastMessage(extractLastMessage(roomsLastMessage, groupId));

    session.setMessageDate(Helper.getUnixTimestampFromDate(
        rocketChatRoomInformation.getLastMessagesRoom().get(groupId).getTimestamp()));
    latestMessageSetter.accept(roomsLastMessage.getTimestamp());
    session.setAttachment(sessionListAnalyser
        .getAttachmentFromRocketChatMessageIfAvailable(rcUserId, roomsLastMessage));
    if (nonNull(roomsLastMessage.getAlias())) {
      session.setVideoCallMessageDTO(roomsLastMessage.getAlias().getVideoCallMessageDTO());
    }
  }

  private LastMessageDTO extractLastMessage(RoomsLastMessageDTO roomsLastMessage, String groupId) {
    var lastMessage = new LastMessageDTO();
    lastMessage.setType(roomsLastMessage.getType());

    if (isLastMessageFurtherStepsAlias(roomsLastMessage)) {
      lastMessage.setMessage(FURTHER_STEPS_MESSAGE);
      return lastMessage;
    }

    if (isNotBlank(roomsLastMessage.getMessage())) {
      var message = sessionListAnalyser.prepareMessageForSessionList(
          roomsLastMessage.getMessage(), groupId);
      lastMessage.setMessage(message);
      return lastMessage;
    }
    return null;
  }

  private boolean isLastMessageFurtherStepsAlias(RoomsLastMessageDTO roomsLastMessageDTO) {
    var alias = roomsLastMessageDTO.getAlias();
    return nonNull(alias) && nonNull(alias.getMessageType()) && FURTHER_STEPS.name()
        .equals(roomsLastMessageDTO.getAlias().getMessageType().name());
  }

}
