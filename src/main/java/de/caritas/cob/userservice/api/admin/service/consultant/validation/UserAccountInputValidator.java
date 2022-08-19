package de.caritas.cob.userservice.api.admin.service.consultant.validation;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_VALID;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.USERNAME_NOT_VALID;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import javax.validation.Validator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Validation class for data transferred within consultant creation process. */
@Component
@RequiredArgsConstructor
public class UserAccountInputValidator {

  private static final String EMAIL_FIELD = "email";
  private static final String USERNAME_FIELD = "username";

  private final @NonNull Validator validator;

  /**
   * Validates if given {@link CreateConsultantDTO} has set the property absent to true and no
   * therefore required absence message set.
   *
   * @param inputValidation the input data to check
   */
  public void validateAbsence(AbsenceInputValidation inputValidation) {
    if (inputValidation.isAbsent() && isBlank(inputValidation.absenceMessage())) {
      throw new CustomValidationHttpStatusException(MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER);
    }
  }

  /**
   * Validates email.
   *
   * @param emailAddress the email address to be validated
   */
  public void validateEmailAddress(String emailAddress) {
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail(emailAddress);
    validateField(userDTO, EMAIL_FIELD, EMAIL_NOT_VALID);
  }

  /**
   * Validates email and username.
   *
   * @param userDTO the {@link UserDTO} to be validated
   */
  public void validateUserDTO(UserDTO userDTO) {
    validateField(userDTO, EMAIL_FIELD, EMAIL_NOT_VALID);
    validateField(userDTO, USERNAME_FIELD, USERNAME_NOT_VALID);
  }

  private void validateField(
      UserDTO userDTO, String fieldName, HttpStatusExceptionReason failReason) {
    this.validator.validate(userDTO).stream()
        .filter(violation -> violation.getPropertyPath().toString().equals(fieldName))
        .findFirst()
        .ifPresent(
            violation -> {
              throw new CustomValidationHttpStatusException(failReason);
            });
  }

  /**
   * Validates the created keycloak object.
   *
   * @param keycloakResponse the keycloak response object to be validated
   */
  public void validateKeycloakResponse(KeycloakCreateUserResponseDTO keycloakResponse) {
    if (isNull(keycloakResponse.getUserId())) {
      throw new KeycloakException("ERROR: Keycloak user id is missing");
    }
  }
}
