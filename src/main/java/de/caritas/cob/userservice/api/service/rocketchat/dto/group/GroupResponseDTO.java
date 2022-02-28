package de.caritas.cob.userservice.api.service.rocketchat.dto.group;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response object for Rocket.Chat API Call for creating a group https://rocket.chat/docs/developer-guides/rest-api/groups/create/
 */
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
