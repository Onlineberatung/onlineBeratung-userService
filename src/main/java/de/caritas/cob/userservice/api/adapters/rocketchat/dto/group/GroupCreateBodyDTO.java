package de.caritas.cob.userservice.api.adapters.rocketchat.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// Body object for Rocket.Chat API Call for creating a group
@Setter
@Getter
@AllArgsConstructor
public class GroupCreateBodyDTO {

  private String name;
  private boolean readOnly;
}
