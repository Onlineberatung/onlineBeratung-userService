package de.caritas.cob.UserService.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "Attachment")
public class SessionAttachmentDTO {

  @ApiModelProperty(example = "image/png", position = 0)
  private String fileType;
  @ApiModelProperty(example = "/9j/2wBDAAYEBQYFBAYGBQY", position = 1)
  private String imagePreview;
  @ApiModelProperty(example = "true", position = 2)
  private boolean fileReceived;

}
