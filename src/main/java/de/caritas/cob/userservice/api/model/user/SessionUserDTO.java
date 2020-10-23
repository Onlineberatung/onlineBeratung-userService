package de.caritas.cob.userservice.api.model.user;

import java.util.LinkedHashMap;
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
@ApiModel(value = "SessionUser")
public class SessionUserDTO {

  @ApiModelProperty(example = "Username", position = 0)
  private String username;
  private LinkedHashMap<String, Object> sessionData;

}
