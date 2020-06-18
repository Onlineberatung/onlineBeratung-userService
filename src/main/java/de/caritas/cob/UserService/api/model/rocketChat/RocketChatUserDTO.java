package de.caritas.cob.UserService.api.model.rocketChat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RocketChatUserDTO {

  @JsonProperty("_id")
  private String id;
  private String username;

}
