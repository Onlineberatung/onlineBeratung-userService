package de.caritas.cob.userservice.api.adapters.web.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
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
  private Map<String, Object> sessionData;

}
