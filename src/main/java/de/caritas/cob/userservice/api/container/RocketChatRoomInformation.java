package de.caritas.cob.userservice.api.container;

import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsUpdateDTO;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RocketChatRoomInformation {

  private final Map<String, Boolean> messagesReadMap;
  private final List<RoomsUpdateDTO> roomsUpdateList;
  private final List<String> userRoomList;
  private final Map<String, RoomsLastMessageDTO> roomLastMessageMap;

}
