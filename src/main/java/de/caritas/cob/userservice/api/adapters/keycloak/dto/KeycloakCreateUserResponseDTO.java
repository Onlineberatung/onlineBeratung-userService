package de.caritas.cob.userservice.api.adapters.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/** Response DTO containing the HttpStatus and possible errors. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakCreateUserResponseDTO {

  private HttpStatus status;
  private String userId;

  public KeycloakCreateUserResponseDTO(String userId) {
    this.userId = userId;
    this.status = HttpStatus.CREATED;
  }

  public KeycloakCreateUserResponseDTO(HttpStatus status) {
    this.status = status;
  }
}
