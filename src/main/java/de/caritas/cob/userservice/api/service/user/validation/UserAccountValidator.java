package de.caritas.cob.userservice.api.service.user.validation;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Validation class for user accounts. */
@Component
@RequiredArgsConstructor
public class UserAccountValidator {

  private final @NotNull IdentityClient identityClient;

  /**
   * Checks if user can be logged in via the provided credentials. If password is wrong a {@link
   * BadRequestException} is thrown by {@link IdentityClient}.
   *
   * @param username username
   * @param password password
   */
  public void checkPasswordValidity(String username, String password) {
    KeycloakLoginResponseDTO loginResponse = identityClient.loginUser(username, password);
    identityClient.logoutUser(loginResponse.getRefreshToken());
  }
}
