package de.caritas.cob.userservice.api.adapters.rocketchat.dto.room;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.message.attachment.AttachmentDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.message.attachment.FileDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.deserializer.AliasJsonDeserializer;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AliasMessageDTO;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Rocket.Chat last message DTO for rooms get */
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

  @JsonProperty("t")
  private String type;

  @JsonProperty("file")
  private FileDTO file;

  @JsonProperty("attachments")
  private AttachmentDTO[] attachements;

  @JsonDeserialize(using = AliasJsonDeserializer.class)
  private AliasMessageDTO alias;
}
