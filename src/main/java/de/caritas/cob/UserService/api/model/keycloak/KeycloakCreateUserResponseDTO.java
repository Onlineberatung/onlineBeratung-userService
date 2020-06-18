package de.caritas.cob.UserService.api.model.keycloak;

import org.springframework.http.HttpStatus;
import de.caritas.cob.UserService.api.model.CreateUserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO containing the HttpStatus and possible errors.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakCreateUserResponseDTO {
  private HttpStatus status;
  private CreateUserResponseDTO responseDTO;
  private String userId;

  public KeycloakCreateUserResponseDTO(String userId) {
    this.userId = userId;
    this.status = HttpStatus.CREATED;
  }

  public KeycloakCreateUserResponseDTO(HttpStatus status) {
    this.status = status;
  }
}
