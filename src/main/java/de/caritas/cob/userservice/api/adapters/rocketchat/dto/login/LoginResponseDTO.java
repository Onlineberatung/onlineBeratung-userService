package de.caritas.cob.userservice.api.adapters.rocketchat.dto.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response body object for Rocket.Chat API Call login
 * https://rocket.chat/docs/developer-guides/rest-api/authentication/login
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

  private String status;
  private DataDTO data;
}
