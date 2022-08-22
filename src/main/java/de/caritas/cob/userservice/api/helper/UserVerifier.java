package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Verifier class for user verifications. */
@Component
@RequiredArgsConstructor
public class UserVerifier {

  public static final int MAX_AGE_VALUE = 100;
  private final @NonNull IdentityClient identityClient;

  @Value("${feature.demographics.enabled}")
  private boolean demographicsFeatureEnabled;

  /**
   * Checks if the username of provided {@link UserDTO} is still available for registration. If not,
   * throws {@link HttpStatusExceptionReason#USERNAME_NOT_AVAILABLE}.
   *
   * @param userDTO {@link UserDTO}
   */
  public void checkIfUsernameIsAvailable(UserDTO userDTO) {
    if (!identityClient.isUsernameAvailable(userDTO.getUsername())) {
      throw new CustomValidationHttpStatusException(
          HttpStatusExceptionReason.USERNAME_NOT_AVAILABLE, HttpStatus.CONFLICT);
    }
  }

  public void checkIfAllRequiredAttributesAreCorrectlyFilled(UserDTO userDTO) {
    if (demographicsFeatureEnabled
        && anyRequiredDemographicsAttributeMissingOrHaveInvalidFormat(userDTO)) {
      throw new CustomValidationHttpStatusException(
          HttpStatusExceptionReason.DEMOGRAPHICS_ATTRIBUTE_MISSING, HttpStatus.BAD_REQUEST);
    }
  }

  private boolean ageIsNotCorrect(UserDTO userDTO) {
    return StringUtils.isEmpty(userDTO.getAge())
        || !StringUtils.isNumeric(userDTO.getAge())
        || (userDTO.getUserAge() < 0 || userDTO.getUserAge() > MAX_AGE_VALUE);
  }

  private boolean anyRequiredDemographicsAttributeMissingOrHaveInvalidFormat(UserDTO userDTO) {
    return ageIsNotCorrect(userDTO) || StringUtils.isEmpty(userDTO.getUserGender());
  }
}
