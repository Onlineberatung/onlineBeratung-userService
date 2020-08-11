package de.caritas.cob.userservice.api.model.keycloak.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response body object for a Keycloak login call
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
  private String access_token;
  private int expires_in;
  private int refresh_expires_in;
  private String refresh_token;
  private String token_type;
  private String session_state;
  private String scope;
}
