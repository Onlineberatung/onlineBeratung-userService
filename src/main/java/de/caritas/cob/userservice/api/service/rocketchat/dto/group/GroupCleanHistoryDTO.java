package de.caritas.cob.userservice.api.service.rocketchat.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Body object for Rocket.Chat API Call for cleaning up a room (remove specific messages)
 * https://rocket.chat/docs/developer-guides/rest-api/rooms/cleanhistory/
 */
@Setter
@Getter
@AllArgsConstructor
public class GroupCleanHistoryDTO {

  private String roomId;
  private String oldest;
  private String latest;
  private String[] users;
}
