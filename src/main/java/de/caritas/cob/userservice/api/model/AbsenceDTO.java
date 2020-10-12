package de.caritas.cob.userservice.api.model;

import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/***
 * Absence model
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "Absence")
public class AbsenceDTO {

  @NotNull(message = "{consultant.custom.isAbsent.notNull}")
  @ApiModelProperty(required = false, example = "true", position = 0)
  @JsonProperty("isAbsent")
  private boolean isAbsent;

  @ApiModelProperty(required = true, example = "\"Ich bin abwesend vom...bis.\"", position = 1)
  @JsonProperty("message")
  private String message;
}
