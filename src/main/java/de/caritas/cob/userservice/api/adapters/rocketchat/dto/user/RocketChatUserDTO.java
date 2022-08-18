package de.caritas.cob.userservice.api.adapters.rocketchat.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
  private String name;
  private List<UserRoomDTO> rooms;
}
