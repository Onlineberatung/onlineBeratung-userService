package de.caritas.cob.userservice.api.adapters.rocketchat.dto.message.attachment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Rocket.Chat attachment model (sub of MessagesDTO) */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDTO {

  @ApiModelProperty(required = true, example = "/9j/2wBDAAYEBQYFBAYGBQY", position = 2)
  @JsonProperty("image_preview")
  private String imagePreview;
}
