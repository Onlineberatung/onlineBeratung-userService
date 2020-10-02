package de.caritas.cob.userservice.api.facade.sessionlist;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.RocketChatRoomInformation;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetRoomsException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetSubscriptionsException;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.model.rocketChat.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import java.util.HashMap;
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
    this.rocketChatService = rocketChatService;
  }

  /**
   * Get room and update information from Rocket.Chat for a user.
   *
   * @param rocketChatCredentials the Rocket.Chat credentials of the user
   * @return an instance of {@link RocketChatRoomInformation}
   */
  public RocketChatRoomInformation retrieveRocketChatInformation(
      RocketChatCredentials rocketChatCredentials) {

    Map<String, Boolean> messagesReadMap = getMessagesReadMap(rocketChatCredentials);
    List<RoomsUpdateDTO> roomsUpdateList = getRcRoomsUpdateList(rocketChatCredentials);
    List<String> userRoomList =
        roomsUpdateList.stream().map(RoomsUpdateDTO::getId).collect(Collectors.toList());
    Map<String, RoomsLastMessageDTO> roomLastMessageMap = getRcRoomLastMessageMap(roomsUpdateList);

    return RocketChatRoomInformation.builder().messagesReadMap(messagesReadMap)
        .roomsUpdateList(roomsUpdateList).userRoomList(userRoomList)
        .roomLastMessageMap(roomLastMessageMap).build();

  }

  /**
   * Get a map with the read-status for each room id
   *
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return
   */
  private Map<String, Boolean> getMessagesReadMap(RocketChatCredentials rocketChatCredentials) {
    List<SubscriptionsUpdateDTO> subscriptions = getSubscriptionsOfUser(rocketChatCredentials);
    Map<String, Boolean> messagesReadMap = new HashMap<>();
    for (SubscriptionsUpdateDTO subscription : subscriptions) {
      messagesReadMap.put(subscription.getRoomId(),
          isMessagesRead(subscription));
    }
    return messagesReadMap;
  }

  private boolean isMessagesRead(SubscriptionsUpdateDTO subscription) {
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

  /**
   * Get the rooms update list for a user from RocketChat
   *
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return
   */
  private List<RoomsUpdateDTO> getRcRoomsUpdateList(RocketChatCredentials rocketChatCredentials) {
    try {
      return rocketChatService.getRoomsOfUser(rocketChatCredentials);
    } catch (RocketChatGetRoomsException rocketChatGetRoomsException) {
      throw new InternalServerErrorException(rocketChatGetRoomsException.getMessage(),
          LogService::logRocketChatError);
    }
  }

  /**
   * Get a map with the last Rocket.Chat message and its date for each room id.
   *
   * @param roomsUpdateList {@link List} of {@link RoomsUpdateDTO}
   * @return a map with each room's {@link RoomsLastMessageDTO}
   */
  private Map<String, RoomsLastMessageDTO> getRcRoomLastMessageMap(
      List<RoomsUpdateDTO> roomsUpdateList) {

    Map<String, RoomsLastMessageDTO> messageDateMap = new HashMap<>();
    for (RoomsUpdateDTO roomsUpdate : roomsUpdateList) {
      if (isLastMessageAndTimestampForRocketChatRoomAvailable(roomsUpdate)) {
        messageDateMap.put(roomsUpdate.getId(), roomsUpdate.getLastMessage());
      }
    }
    return messageDateMap;
  }

  private boolean isLastMessageAndTimestampForRocketChatRoomAvailable(RoomsUpdateDTO room) {
    return nonNull(room.getLastMessage()) && nonNull(room.getLastMessage().getTimestamp());
  }

}
