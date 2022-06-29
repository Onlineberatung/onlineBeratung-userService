package de.caritas.cob.userservice.api.adapters.rocketchat.dto.room;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rocket.Chat update DTO for rooms
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomsUpdateDTO {

  @JsonProperty("_id")
  private String id;
  private String name;
  private String fname;
  @JsonProperty("t")
  private String roomType;
  @JsonProperty("u")
  private RocketChatUserDTO user;
  @JsonProperty("ro")
  private boolean readOnly;
  @JsonProperty("sysMes")
  private boolean systemMessages;
  @JsonProperty("_updatedAt")
  private Date updatedAt;
  private RoomsLastMessageDTO lastMessage;

}
