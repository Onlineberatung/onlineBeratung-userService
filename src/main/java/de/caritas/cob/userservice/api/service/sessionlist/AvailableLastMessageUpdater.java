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
import de.caritas.cob.userservice.api.adapters.web.dto.UserChatDTO;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Updater singleton to update Rocket.Chat relevant data on session items. */
@RequiredArgsConstructor
public class AvailableLastMessageUpdater {

  private static final String FURTHER_STEPS_MESSAGE = "So geht es weiter";

  private final @NonNull SessionListAnalyser sessionListAnalyser;

  /**
   * Updates the given session with further Rocket.Chat last message information.
   *
   * @param session the {@link SessionDTO}
   * @param latestMessageDate consumer for setting the date of the latest message
   * @param rocketChatRoomInformation the {@link RocketChatRoomInformation}
   * @param rcUserId the Rocket.Chat user id
   */
  void updateSessionWithAvailableLastMessage(
      SessionDTO session,
      Consumer<Date> latestMessageDate,
      RocketChatRoomInformation rocketChatRoomInformation,
      String rcUserId) {
    var groupId = session.getGroupId();
    var roomsLastMessage = rocketChatRoomInformation.getLastMessagesRoom().get(groupId);

    setLastMessage(session::setE2eLastMessage, session::setLastMessage, groupId, roomsLastMessage);
    setLatestMessageDateOrFallback(
        session, latestMessageDate, rocketChatRoomInformation, groupId, roomsLastMessage);
    setAttachmentAndVideoCallMessage(session, rcUserId, roomsLastMessage);
  }

  void updateChatWithAvailableLastMessage(
      UserChatDTO chat,
      Consumer<Date> latestMessageDate,
      RocketChatRoomInformation rocketChatRoomInformation,
      String rcUserId) {
    var groupId = chat.getGroupId();
    var roomsLastMessage = rocketChatRoomInformation.getLastMessagesRoom().get(groupId);

    if (isNull(roomsLastMessage)) {
      latestMessageDate.accept(Timestamp.valueOf(chat.getStartDateWithTime()));
      return;
    }

    setLastMessage(chat::setE2eLastMessage, ignoreFurtherSteps(chat), groupId, roomsLastMessage);
    chat.setMessageDate(Helper.getUnixTimestampFromDate(roomsLastMessage.getTimestamp()));
    latestMessageDate.accept(roomsLastMessage.getTimestamp());
    chat.setAttachment(
        sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
            rcUserId, roomsLastMessage));
  }

  private void setLastMessage(
      Consumer<LastMessageDTO> e2eLastMessageSetter,
      Consumer<String> lastMessageSetter,
      String groupId,
      RoomsLastMessageDTO roomsLastMessage) {
    var lastMessage = extractLastMessage(roomsLastMessage, groupId);
    e2eLastMessageSetter.accept(lastMessage);
    lastMessageSetter.accept(lastMessage != null ? lastMessage.getMsg() : "");
  }

  private LastMessageDTO extractLastMessage(RoomsLastMessageDTO roomsLastMessage, String groupId) {
    var lastMessage = new LastMessageDTO();

    if (isNull(roomsLastMessage) || isLastMessageFurtherStepsAlias(roomsLastMessage)) {
      lastMessage.setMsg(FURTHER_STEPS_MESSAGE);
      return lastMessage;
    }

    lastMessage.setT(roomsLastMessage.getType());
    if (isNotBlank(roomsLastMessage.getMessage())) {
      var message =
          sessionListAnalyser.prepareMessageForSessionList(roomsLastMessage.getMessage(), groupId);
      lastMessage.setMsg(message);
      return lastMessage;
    }
    return null;
  }

  private boolean isLastMessageFurtherStepsAlias(RoomsLastMessageDTO roomsLastMessageDTO) {
    var alias = roomsLastMessageDTO.getAlias();
    return nonNull(alias)
        && nonNull(alias.getMessageType())
        && FURTHER_STEPS.name().equals(roomsLastMessageDTO.getAlias().getMessageType().name());
  }

  private void setLatestMessageDateOrFallback(
      SessionDTO session,
      Consumer<Date> latestMessageDate,
      RocketChatRoomInformation rocketChatRoomInformation,
      String groupId,
      RoomsLastMessageDTO roomsLastMessage) {
    if (isNull(roomsLastMessage)) {
      var fallbackDate =
          rocketChatRoomInformation.getGroupIdToLastMessageFallbackDate().get(groupId);
      setFallbackDate(latestMessageDate, session, fallbackDate);
      return;
    }

    var lastMessageDate =
        findNewestLastMessageDateFromRoom(rocketChatRoomInformation, groupId, roomsLastMessage);
    latestMessageDate.accept(lastMessageDate);
    session.setMessageDate(Helper.getUnixTimestampFromDate(lastMessageDate));
  }

  private Date findNewestLastMessageDateFromRoom(
      RocketChatRoomInformation rocketChatRoomInformation,
      String groupId,
      RoomsLastMessageDTO roomsLastMessage) {
    var updateRoomTimestamp =
        rocketChatRoomInformation.getRoomsForUpdate().stream()
            .filter(room -> room.getId().equals(groupId))
            .findFirst();
    var latestMessageFromUpdateRoom =
        updateRoomTimestamp.isPresent()
            ? updateRoomTimestamp.get().getLastMessageDate()
            : Date.from(Instant.EPOCH);
    var roomsLastMessageDate =
        nonNull(roomsLastMessage.getTimestamp())
            ? roomsLastMessage.getTimestamp()
            : Date.from(Instant.EPOCH);

    return latestMessageFromUpdateRoom.after(roomsLastMessageDate)
        ? latestMessageFromUpdateRoom
        : roomsLastMessageDate;
  }

  private void setFallbackDate(
      Consumer<Date> latestMessageDate, SessionDTO session, Date fallbackDate) {
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

  private void setAttachmentAndVideoCallMessage(
      SessionDTO session, String rcUserId, RoomsLastMessageDTO roomsLastMessage) {
    if (isNull(roomsLastMessage)) {
      session.setLastMessageType(FURTHER_STEPS);
      return;
    }
    var attachment =
        sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
            rcUserId, roomsLastMessage);
    session.setAttachment(attachment);
    var alias = roomsLastMessage.getAlias();
    if (nonNull(alias)) {
      session.setVideoCallMessageDTO(alias.getVideoCallMessageDTO());
      session.setLastMessageType(alias.getMessageType());
    }
  }

  private Consumer<String> ignoreFurtherSteps(UserChatDTO chat) {
    return msg -> {
      if (!FURTHER_STEPS_MESSAGE.equals(msg)) {
        chat.setLastMessage(msg);
      }
    };
  }
}
