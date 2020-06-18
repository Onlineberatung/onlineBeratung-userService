package de.caritas.cob.UserService.api.model.rocketChat.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Body object for Rocket.Chat API Call for creating a group
 * https://rocket.chat/docs/developer-guides/rest-api/groups/create/
 * 
 */
@Setter
@Getter
@AllArgsConstructor
public class GroupCreateBodyDTO {

  private String name;
  private boolean readOnly;

}
