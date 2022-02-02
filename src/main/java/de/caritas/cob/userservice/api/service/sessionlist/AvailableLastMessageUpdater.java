package de.caritas.cob.userservice.api.service.sessionlist;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetMessagesStreamException;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.SessionListAnalyser;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.service.message.MessageServiceProvider;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessagesDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Updater singleton to update Rocket.Chat relevant data on session items.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AvailableLastMessageUpdater {

  private final @NonNull SessionListAnalyser sessionListAnalyser;
  private final @NonNull MessageServiceProvider messageServiceProvider;
  @Value("${rocket.systemuser.id}")
  private String rocketChatSystemUserId;

  /**
   * Updates the given session with further Rocket.Chat last message information.
   *
   * @param rocketChatRoomInformation the {@link RocketChatRoomInformation}
   * @param latestMessageSetter       the function to set the latest message timestamp
   * @param session                   the session to be updated
   * @param rocketChatCredentials     the {@link RocketChatCredentials}
   */
  void updateSessionWithAvailableLastMessage(RocketChatRoomInformation rocketChatRoomInformation,
      Consumer<Date> latestMessageSetter, SessionDTO session,
      RocketChatCredentials rocketChatCredentials) {

    var groupId = session.getGroupId();
    var roomsLastMessage = getLastMessageOfRoom(rocketChatRoomInformation, rocketChatCredentials,
        groupId);
    session.setLastMessage(isNotBlank(roomsLastMessage.getMessage())
        ? sessionListAnalyser.prepareMessageForSessionList(roomsLastMessage.getMessage(), groupId)
        : null);
    session.setMessageDate(Helper.getUnixTimestampFromDate(
        rocketChatRoomInformation.getLastMessagesRoom().get(groupId).getTimestamp()));
    latestMessageSetter.accept(roomsLastMessage.getTimestamp());
    session.setAttachment(sessionListAnalyser.getAttachmentFromRocketChatMessageIfAvailable(
        rocketChatCredentials.getRocketChatUserId(), roomsLastMessage));
    if (nonNull(roomsLastMessage.getAlias())) {
      session.setVideoCallMessageDTO(roomsLastMessage.getAlias().getVideoCallMessageDTO());
    }
  }

  private RoomsLastMessageDTO getLastMessageOfRoom(
      RocketChatRoomInformation rocketChatRoomInformation, RocketChatCredentials credentials,
      String groupId) {
    var lastMessageOfRoom = rocketChatRoomInformation.getLastMessagesRoom()
        .get(groupId);

    if (lastMessageIsAliasFromSystem(lastMessageOfRoom)) {
      lastMessageOfRoom.setMessage(fetchLastNonSystemMessage(credentials, groupId));
    }
    return lastMessageOfRoom;
  }

  private boolean lastMessageIsAliasFromSystem(RoomsLastMessageDTO roomsLastMessage) {
    var chatUser = roomsLastMessage.getUser();
    return nonNull(chatUser) && rocketChatSystemUserId.equals(chatUser.getId()) && nonNull(
        roomsLastMessage.getAlias());
  }

  private String fetchLastNonSystemMessage(RocketChatCredentials rocketChatCredentials,
      String groupId) {
    List<MessagesDTO> messages = new ArrayList<>();
    try {
      messages = messageServiceProvider.getMessages(rocketChatCredentials, groupId).stream()
          .filter(message -> nonNull(message.getU()))
          .filter(message -> !rocketChatSystemUserId.equals(message.getU().getId()))
          .collect(Collectors.toList());
    } catch (RocketChatGetMessagesStreamException e) {
      log.error("failed to load last non system message", e);
    }
    if (messages.isEmpty()) {
      return "";
    }
    var lastMessage = messages.get(messages.size() - 1);
    return lastMessage.getMsg();
  }

}
