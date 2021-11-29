package de.caritas.cob.userservice.api.model.rocketchat.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response wrapper object for Rocket.Chat API Call for retrieving all groups.
 * https://developer.rocket.chat/api/rest-api/endpoints/groups/listall.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupsListAllResponseDTO {

  @JsonProperty("groups")
  private GroupDTO[] groups;

}
