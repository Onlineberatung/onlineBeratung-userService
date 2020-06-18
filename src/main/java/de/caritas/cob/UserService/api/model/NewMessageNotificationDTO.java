package de.caritas.cob.UserService.api.model;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@ApiModel(value = "NewMessageNotification")
public class NewMessageNotificationDTO {

  @NotBlank(message = "{notification.group.id.notBlank}")
  @ApiModelProperty(required = true, position = 0, example = "fR2Rz7dmWmHdXE8uz")
  @JsonProperty("rcGroupId")
  private String rcGroupId;
}
