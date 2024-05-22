package de.caritas.cob.userservice.api.facade.sessionlist;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RocketChatRoomInformationProvider {

  private final RocketChatService rocketChatService;

  public RocketChatRoomInformationProvider(RocketChatService rocketChatService) {
    this.rocketChatService = requireNonNull(rocketChatService);
  }

  /**
   * Get room and update information from Rocket.Chat for a user.
   *
   * @param rocketChatCredentials the Rocket.Chat credentials of the user
   * @return an instance of {@link RocketChatRoomInformation}
   */
  public RocketChatRoomInformation retrieveRocketChatInformation(
      RocketChatCredentials rocketChatCredentials) {

    Map<String, Boolean> readMessages = emptyMap();
    List<RoomsUpdateDTO> roomsForUpdate = emptyList();

    if (nonNull(rocketChatCredentials.getRocketChatUserId())) {
      readMessages = buildMessagesWithReadInfo(rocketChatCredentials);
      roomsForUpdate = rocketChatService.getRoomsOfUser(rocketChatCredentials);
    }

    var userRooms = roomsForUpdate.stream().map(RoomsUpdateDTO::getId).collect(Collectors.toList());
    var lastMessagesRoom = getRcRoomLastMessages(roomsForUpdate);
    var groupIdToLastMessageFallbackDate =
        collectFallbackDateOfRoomsWithoutLastMessage(roomsForUpdate);

    return RocketChatRoomInformation.builder()
        .readMessages(readMessages)
        .roomsForUpdate(roomsForUpdate)
        .userRooms(userRooms)
        .lastMessagesRoom(lastMessagesRoom)
        .groupIdToLastMessageFallbackDate(groupIdToLastMessageFallbackDate)
        .build();
  }

  private Map<String, Boolean> buildMessagesWithReadInfo(
      RocketChatCredentials rocketChatCredentials) {

    List<SubscriptionsUpdateDTO> subscriptions =
        rocketChatService.getSubscriptionsOfUser(rocketChatCredentials);

    return subscriptions.stream()
        .collect(Collectors.toMap(SubscriptionsUpdateDTO::getRoomId, this::isMessageRead));
  }

  private boolean isMessageRead(SubscriptionsUpdateDTO subscription) {
    return nonNull(subscription.getUnread()) && subscription.getUnread() == 0;
  }

  private Map<String, RoomsLastMessageDTO> getRcRoomLastMessages(
      List<RoomsUpdateDTO> roomsUpdateList) {

    return roomsUpdateList.stream()
        .filter(this::isLastMessageAndTimestampForRocketChatRoomAvailable)
        .collect(Collectors.toMap(RoomsUpdateDTO::getId, RoomsUpdateDTO::getLastMessage));
  }

  private boolean isLastMessageAndTimestampForRocketChatRoomAvailable(RoomsUpdateDTO room) {
    return nonNull(room.getLastMessage()) && nonNull(room.getLastMessage().getTimestamp());
  }

  private Map<String, Date> collectFallbackDateOfRoomsWithoutLastMessage(
      List<RoomsUpdateDTO> roomsForUpdate) {
    return roomsForUpdate.stream()
        .filter(room -> !isLastMessageAndTimestampForRocketChatRoomAvailable(room))
        .filter(room -> nonNull(room.getLastMessageDate()))
        .collect(Collectors.toMap(RoomsUpdateDTO::getId, RoomsUpdateDTO::getLastMessageDate));
  }
}
