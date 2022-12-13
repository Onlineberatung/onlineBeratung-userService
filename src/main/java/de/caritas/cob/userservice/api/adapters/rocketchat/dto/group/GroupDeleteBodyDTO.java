package de.caritas.cob.userservice.api.adapters.rocketchat.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Body object for Rocket.Chat API Call for deleting a group
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupDeleteBodyDTO {

  private String roomId;
}
