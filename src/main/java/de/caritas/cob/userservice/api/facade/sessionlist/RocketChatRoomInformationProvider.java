package de.caritas.cob.userservice.api.facade.sessionlist;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetRoomsException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetSubscriptionsException;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.model.rocketchat.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.model.rocketchat.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RocketChatRoomInformationProvider {

  private final RocketChatService rocketChatService;

  @Autowired
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

    Map<String, Boolean> readMessages = buildMessagesWithReadInfo(rocketChatCredentials);
    List<RoomsUpdateDTO> roomsForUpdate = getRcRoomsForUpdate(rocketChatCredentials);
    List<String> userRooms = roomsForUpdate.stream()
        .map(RoomsUpdateDTO::getId)
        .collect(Collectors.toList());
    Map<String, RoomsLastMessageDTO> lastMessagesRoom = getRcRoomLastMessages(roomsForUpdate);

    return RocketChatRoomInformation.builder()
        .readMessages(readMessages)
        .roomsForUpdate(roomsForUpdate)
        .userRooms(userRooms)
        .lastMessagesRoom(lastMessagesRoom)
        .build();
  }

  private Map<String, Boolean> buildMessagesWithReadInfo(
      RocketChatCredentials rocketChatCredentials) {
    List<SubscriptionsUpdateDTO> subscriptions = getSubscriptionsOfUser(rocketChatCredentials);

    return subscriptions.stream()
        .collect(Collectors.toMap(SubscriptionsUpdateDTO::getRoomId, this::isMessageRead));
  }

  private boolean isMessageRead(SubscriptionsUpdateDTO subscription) {
    return nonNull(subscription.getUnread()) && subscription.getUnread() == 0;
  }

  private List<SubscriptionsUpdateDTO> getSubscriptionsOfUser(
      RocketChatCredentials rocketChatCredentials) {
    try {
      return rocketChatService.getSubscriptionsOfUser(rocketChatCredentials);
    } catch (RocketChatGetSubscriptionsException rocketChatGetSubscriptionsException) {
      throw new InternalServerErrorException(rocketChatGetSubscriptionsException.getMessage(),
          LogService::logRocketChatError);
    }
  }

  private List<RoomsUpdateDTO> getRcRoomsForUpdate(RocketChatCredentials rocketChatCredentials) {
    try {
      return rocketChatService.getRoomsOfUser(rocketChatCredentials);
    } catch (RocketChatGetRoomsException rocketChatGetRoomsException) {
      throw new InternalServerErrorException(rocketChatGetRoomsException.getMessage(),
          LogService::logRocketChatError);
    }
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

}
