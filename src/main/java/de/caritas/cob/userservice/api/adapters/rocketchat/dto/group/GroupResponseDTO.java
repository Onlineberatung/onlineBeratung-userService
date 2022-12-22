package de.caritas.cob.userservice.api.adapters.rocketchat.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Response object for Rocket.Chat API Call for creating a group
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupResponseDTO {

  private GroupDTO group;
  private boolean success;
  private String error;
  private String errorType;
}
