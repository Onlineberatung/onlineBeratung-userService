package de.caritas.cob.userservice.api.adapters.web.dto.validation;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.manager.consultingtype.registration.mandatoryfields.MandatoryFields;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Checks if the age in a {@link UserDTO} is valid (depending on value in the consulting type). */
@RequiredArgsConstructor
public class ValidAgeValidator implements ConstraintValidator<ValidAge, UserDTO> {

  private final @NonNull MandatoryFieldsProvider mandatoryFieldsProvider;

  /**
   * Checks if the age is valid in relation to the consulting type.
   *
   * @param userDTO the {@link UserDTO} instance
   * @param context the {@link ConstraintValidatorContext}
   * @return true, if age is valid
   */
  @Override
  public boolean isValid(UserDTO userDTO, ConstraintValidatorContext context) {

    if (isNull(userDTO.getConsultingType())) {
      return false;
    }

    MandatoryFields mandatoryFields =
        mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(userDTO.getConsultingType());

    if (mandatoryFields.isAge()) {
      return isAgeValid(userDTO);
    }

    return true;
  }

  private boolean isAgeValid(UserDTO userDTO) {
    return nonNull(userDTO.getAge()) && Pattern.matches("[0-9]+", userDTO.getAge());
  }
}
