package de.caritas.cob.userservice.api.model.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.caritas.cob.userservice.api.model.jsonserializer.DecodeUsernameJsonSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Consultant object for a session representing the assigned consultant (for the user session list
 * call)
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "SessionConsultantForUser")
public class SessionConsultantForUserDTO {

  @ApiModelProperty(example = "\"Username\"")
  @JsonSerialize(using = DecodeUsernameJsonSerializer.class)
  private String username;

  @ApiModelProperty(example = "\"true\"")
  private boolean isAbsent;

  @ApiModelProperty(example = "\"Bin nicht da\"")
  private String absenceMessage;

}
