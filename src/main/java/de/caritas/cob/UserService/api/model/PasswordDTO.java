package de.caritas.cob.UserService.api.model;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/***
 * PasswordDTO
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel()
public class PasswordDTO {

  @NotBlank(message = "{user.old.password.notBlank}")
  @ApiModelProperty(required = true, example = "oldpass@w0rd", position = 0)
  @JsonProperty("oldPassword")
  private String oldPassword;

  @NotBlank(message = "{user.new.password.notBlank}")
  @ApiModelProperty(required = true, example = "newpass@w0rd", position = 1)
  @JsonProperty("newPassword")
  private String newPassword;
}
