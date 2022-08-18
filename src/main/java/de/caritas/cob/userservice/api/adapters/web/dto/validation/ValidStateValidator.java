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

/**
 * Checks if the state in a {@link UserDTO} is valid (depending on value in {@link
 * ExtendedConsultingTypeResponseDTO}.
 */
@RequiredArgsConstructor
public class ValidStateValidator implements ConstraintValidator<ValidState, UserDTO> {

  private final @NonNull MandatoryFieldsProvider mandatoryFieldsProvider;

  /**
   * Checks if the state is valid in relation to the consulting type.
   *
   * @param userDTO the {@link UserDTO} instance
   * @param context the {@link ConstraintValidatorContext}
   * @return true, if state is valid
   */
  @Override
  public boolean isValid(UserDTO userDTO, ConstraintValidatorContext context) {

    if (isNull(userDTO.getConsultingType())) {
      return false;
    }

    MandatoryFields mandatoryFields =
        mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(userDTO.getConsultingType());

    if (mandatoryFields.isState()) {
      return isStateValid(userDTO);
    }

    return true;
  }

  private boolean isStateValid(UserDTO userDTO) {
    return nonNull(userDTO.getState()) && Pattern.matches("[0-9]|1[0-6]", userDTO.getState());
  }
}
