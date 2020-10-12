package de.caritas.cob.userservice.api.model.validation;

import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.UserDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Checks if the state in a {@link UserDTO} is valid (depending on value in
 * {@link ConsultingTypeSettings}.
 *
 */

public class ValidStateValidator implements ConstraintValidator<ValidState, UserDTO> {

  private final ConsultingTypeManager consultingTypeManager;

  @Autowired
  public ValidStateValidator(ConsultingTypeManager consultingTypeManager) {
    this.consultingTypeManager = consultingTypeManager;
  }

  @Override
  public boolean isValid(UserDTO userDTO, ConstraintValidatorContext context) {

    if (userDTO == null || userDTO.getConsultingType() == null) {
      return false;
    }

    ConsultingTypeSettings consultingTypeSettings = consultingTypeManager.getConsultantTypeSettings(
        ConsultingType.values()[Integer.valueOf(userDTO.getConsultingType())]);

    if (consultingTypeSettings.getRegistration() == null
        || consultingTypeSettings.getRegistration().getMandatoryFields() == null) {
      LogService.logValidationError(String.format(
          "Could not get mandatory fields for consulting type %s. Please check configuration",
          userDTO.getConsultingType()));
      return false;
    }

    if (consultingTypeSettings.getRegistration().getMandatoryFields().isState()) {
      return userDTO.getState() != null && Pattern.matches("[0-9]|1[0-6]", userDTO.getState());
    }

    return true;

  }

}
