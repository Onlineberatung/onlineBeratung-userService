package de.caritas.cob.userservice.api.model.rocketchat.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Body object for Rocket.Chat API Call for removing a user to a group
 * https://rocket.chat/docs/developer-guides/rest-api/groups/kick/
 */
@Setter
@Getter
@AllArgsConstructor
public class GroupRemoveUserBodyDTO {

  private String userId;
  private String roomId;
}
