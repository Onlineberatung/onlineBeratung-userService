package de.caritas.cob.UserService.api.model.rocketChat.room;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.caritas.cob.UserService.api.model.rocketChat.RocketChatUserDTO;
import de.caritas.cob.UserService.api.model.rocketChat.message.attachment.AttachmentDTO;
import de.caritas.cob.UserService.api.model.rocketChat.message.attachment.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rocket.Chat last message DTO for rooms get
 *
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomsLastMessageDTO {

  @JsonProperty("_id")
  private String id;
  @JsonProperty("rid")
  private String roomId;
  @JsonProperty("ts")
  private Date timestamp;
  @JsonProperty("u")
  private RocketChatUserDTO user;
  private boolean unread;
  @JsonProperty("_updatedAt")
  private Date updatedAt;
  @JsonProperty("msg")
  private String message;
  @JsonProperty("file")
  private FileDTO file;
  @JsonProperty("attachments")
  private AttachmentDTO[] attachements;

}
