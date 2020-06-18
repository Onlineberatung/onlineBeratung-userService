package de.caritas.cob.UserService.api.model.rocketChat.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Body object for Rocket.Chat API Call for adding a user to a group
 * https://rocket.chat/docs/developer-guides/rest-api/groups/invite/
 * 
 */
@Setter
@Getter
@AllArgsConstructor
public class GroupAddUserBodyDTO {
  private String userId;
  private String roomId;
}
