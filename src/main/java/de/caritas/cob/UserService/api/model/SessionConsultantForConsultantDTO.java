package de.caritas.cob.UserService.api.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Consultant object for a session representing the assigned consultant (for the consultant session
 * list call)
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "SessionConsultantForConsultant")
public class SessionConsultantForConsultantDTO {

  private String id;
  private String firstName;
  private String lastName;
}
