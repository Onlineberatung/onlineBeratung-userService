package de.caritas.cob.userservice.api.container;

import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.RoomsUpdateDTO;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RocketChatRoomInformation {

  private final Map<String, Boolean> readMessages;
  private final List<RoomsUpdateDTO> roomsForUpdate;
  private final List<String> userRooms;
  private final Map<String, RoomsLastMessageDTO> lastMessagesRoom;
  private final Map<String, Date> groupIdToLastMessageFallbackDate;
}
