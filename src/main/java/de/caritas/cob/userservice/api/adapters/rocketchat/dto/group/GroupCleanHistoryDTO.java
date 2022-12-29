package de.caritas.cob.userservice.api.adapters.rocketchat.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// Body object for Rocket.Chat API Call for cleaning up a room (remove specific messages)
@Setter
@Getter
@AllArgsConstructor
public class GroupCleanHistoryDTO {

  private String roomId;
  private String oldest;
  private String latest;
  private String[] users;
}
