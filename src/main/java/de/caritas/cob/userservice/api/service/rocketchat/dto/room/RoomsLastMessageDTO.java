package de.caritas.cob.userservice.api.service.rocketchat.dto.room;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.caritas.cob.userservice.api.adapters.web.dto.AliasMessageDTO;
import de.caritas.cob.userservice.api.service.rocketchat.dto.room.deserializer.AliasJsonDeserializer;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.caritas.cob.userservice.api.service.rocketchat.dto.RocketChatUserDTO;
import de.caritas.cob.userservice.api.service.rocketchat.dto.message.attachment.AttachmentDTO;
import de.caritas.cob.userservice.api.service.rocketchat.dto.message.attachment.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rocket.Chat last message DTO for rooms get
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
  @JsonDeserialize(using = AliasJsonDeserializer.class)
  private AliasMessageDTO alias;
}
