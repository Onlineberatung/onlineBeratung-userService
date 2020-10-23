package de.caritas.cob.userservice.api.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response model class for a 409 Conflict on POST /users
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Response")
public class CreateUserResponseDTO {
  private int usernameAvailable;
  private int emailAvailable;
}
