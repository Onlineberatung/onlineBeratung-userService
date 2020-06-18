package de.caritas.cob.UserService.api.model;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.caritas.cob.UserService.api.model.jsonDeserializer.UrlDecodePasswordJsonDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "DeleteUser")
public class DeleteUserDTO {
  @NotBlank(message = "{user.password.notBlank}")
  @ApiModelProperty(required = true, example = "pass@w0rd", position = 0)
  @JsonDeserialize(using = UrlDecodePasswordJsonDeserializer.class)
  @JsonProperty("password")
  private String password;
}
