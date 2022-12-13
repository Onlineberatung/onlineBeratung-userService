package de.caritas.cob.userservice.api.adapters.rocketchat.dto.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Rocket.Chat group object
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {

  @JsonProperty("_id")
  private String id;

  private String name;
  private String fname;

  @JsonProperty("t")
  private String type;

  @JsonProperty("msgs")
  private int messagesCount;

  private int usersCount;

  @JsonProperty("u")
  private RocketChatUserDTO user;

  @JsonProperty("ts")
  private Date timestamp;

  @JsonProperty("ro")
  private boolean readOnly;

  @JsonProperty("sysMes")
  private boolean displaySystemMessages;

  @JsonProperty("_updatedAt")
  private Date updatedAt;
}
