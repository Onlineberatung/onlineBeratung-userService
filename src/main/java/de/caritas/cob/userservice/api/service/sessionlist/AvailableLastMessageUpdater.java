package de.caritas.cob.userservice.api.service.sessionlist;

import static de.caritas.cob.userservice.api.adapters.web.dto.MessageType.FURTHER_STEPS;
import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toDate;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.ANONYMOUS;
import static java.util.Objects.isNull;
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
   * @param session                   the {@link SessionDTO}
   * @param latestMessageDate         consumer for setting the date of the latest message
   * @param rocketChatRoomInformation the {@link RocketChatRoomInformation}
   * @param rcUserId                  the Rocket.Chat user id
   */
  void updateSessionWithAvailableLastMessage(SessionDTO session, Consumer<Date> latestMessageDate,
      RocketChatRoomInformation rocketChatRoomInformation, String rcUserId) {
    var groupId = session.getGroupId();
    var roomsLastMessage = rocketChatRoomInformation.getLastMessagesRoom().get(groupId);

    setLastMessage(session, groupId, roomsLastMessage);
    setLatestMessageDateOrFallback(session, latestMessageDate, rocketChatRoomInformation, groupId,
        roomsLastMessage);
    setAttachmentAndVideoCallMessage(session, rcUserId, roomsLastMessage);
  }

  private void setLastMessage(SessionDTO session, String groupId,
      RoomsLastMessageDTO roomsLastMessage) {
    var lastMessage = extractLastMessage(roomsLastMessage, groupId);
    session.setE2eLastMessage(lastMessage);
    session.setLastMessage(lastMessage != null ? lastMessage.getMsg() : "");
  }

  private LastMessageDTO extractLastMessage(RoomsLastMessageDTO roomsLastMessage, String groupId) {
    var lastMessage = new LastMessageDTO();

    if (isNull(roomsLastMessage) || isLastMessageFurtherStepsAlias(roomsLastMessage)) {
      lastMessage.setMsg(FURTHER_STEPS_MESSAGE);
      return lastMessage;
    }

    lastMessage.setT(roomsLastMessage.getType());
    if (isNotBlank(roomsLastMessage.getMessage())) {
      var message = sessionListAnalyser.prepareMessageForSessionList(
          roomsLastMessage.getMessage(), groupId);
      lastMessage.setMsg(message);
      return lastMessage;
    }
    return null;
  }

  private boolean isLastMessageFurtherStepsAlias(RoomsLastMessageDTO roomsLastMessageDTO) {
    var alias = roomsLastMessageDTO.getAlias();
    return nonNull(alias) && nonNull(alias.getMessageType()) && FURTHER_STEPS.name()
        .equals(roomsLastMessageDTO.getAlias().getMessageType().name());
  }

  private void setLatestMessageDateOrFallback(SessionDTO session, Consumer<Date> latestMessageDate,
      RocketChatRoomInformation rocketChatRoomInformation, String groupId,
      RoomsLastMessageDTO roomsLastMessage) {
    if (isNull(roomsLastMessage)) {
      var fallbackDate = rocketChatRoomInformation.getGroupIdToLastMessageFallbackDate()
          .get(groupId);
      setFallbackDate(latestMessageDate, session, fallbackDate);
      return;
    }

    latestMessageDate.accept(roomsLastMessage.getTimestamp());
    session.setMessageDate(Helper.getUnixTimestampFromDate(roomsLastMessage.getTimestamp()));
  }

  private void setFallbackDate(Consumer<Date> latestMessageDate, SessionDTO session,
      Date fallbackDate) {
    if (nonNull(fallbackDate)) {
      session.setMessageDate(Helper.getUnixTimestampFromDate(fallbackDate));
      latestMessageDate.accept(fallbackDate);
      return;
    }

    session.setMessageDate(Helper.UNIXTIME_0.getTime());
    if (ANONYMOUS.name().equals(session.getRegistrationType())) {
      latestMessageDate.accept(toDate(session.getCreateDate()));
    } else {
      latestMessageDate.accept(Helper.UNIXTIME_0);
    }
  }

  private void setAttachmentAndVideoCallMessage(SessionDTO session, String rcUserId,
      RoomsLastMessageDTO roomsLastMessage) {
    if (isNull(roomsLastMessage)) {
      session.setLastMessageType(FURTHER_STEPS);
      return;
    }
    var attachment = sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(rcUserId,
        roomsLastMessage);
    session.setAttachment(attachment);
    var alias = roomsLastMessage.getAlias();
    if (nonNull(alias)) {
      session.setVideoCallMessageDTO(alias.getVideoCallMessageDTO());
      session.setLastMessageType(alias.getMessageType());
    }
  }
}
