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
@ApiModel(value = "ConsultantResponseDTO")
public class ConsultantResponseDTO {

  @ApiModelProperty(example = " aadc0ecf-c048-4bfc-857d-8c9b2e425500", position = 0)
  private String consultantId;
  @ApiModelProperty(example = "Max", position = 1)
  private String firstName;
  @ApiModelProperty(example = "Mustermann", position = 2)
  private String lastName;
}
