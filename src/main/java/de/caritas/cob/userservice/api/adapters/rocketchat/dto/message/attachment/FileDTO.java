package de.caritas.cob.userservice.api.adapters.rocketchat.dto.message.attachment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Rocket.Chat file model (sub of MessagesDTO.lastMessage) */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileDTO {

  @ApiModelProperty(required = true, example = "filename.png", position = 0)
  private String name;

  @ApiModelProperty(
      required = true,
      example = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      position = 1)
  @JsonProperty("type")
  private String type;
}
