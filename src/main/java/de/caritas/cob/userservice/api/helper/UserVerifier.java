package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Verifier class for user verifications.
 */
@Component
@RequiredArgsConstructor
public class UserVerifier {

  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;

  /**
   * Checks if the username of provided {@link UserDTO} is still available for registration. If not,
   * throws {@link HttpStatusExceptionReason#USERNAME_NOT_AVAILABLE}.
   *
   * @param userDTO {@link UserDTO}
   */
  public void checkIfUsernameIsAvailable(UserDTO userDTO) {
    if (!keycloakAdminClientService.isUsernameAvailable(userDTO.getUsername())) {
      throw new CustomValidationHttpStatusException(
          HttpStatusExceptionReason.USERNAME_NOT_AVAILABLE, HttpStatus.CONFLICT);
    }
  }
}
