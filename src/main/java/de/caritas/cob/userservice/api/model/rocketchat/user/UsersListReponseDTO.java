package de.caritas.cob.userservice.api.model.rocketchat.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.caritas.cob.userservice.api.model.rocketchat.RocketChatUserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * Response wrapper object for Rocket.Chat API Call for retrieving all users.
 * https://developer.rocket.chat/api/rest-api/endpoints/users/list
 *
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UsersListReponseDTO {

  @JsonProperty("users")
  private RocketChatUserDTO[] users;

}
