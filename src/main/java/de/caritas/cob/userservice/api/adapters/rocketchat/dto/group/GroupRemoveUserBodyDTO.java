package de.caritas.cob.userservice.api.adapters.rocketchat.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// Body object for Rocket.Chat API Call for removing a user to a group
@Setter
@Getter
@AllArgsConstructor
public class GroupRemoveUserBodyDTO {

  private String userId;
  private String roomId;
}
