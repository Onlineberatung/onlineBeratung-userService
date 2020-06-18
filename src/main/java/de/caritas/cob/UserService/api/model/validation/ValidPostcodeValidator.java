package de.caritas.cob.UserService.api.model.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.UserService.api.model.UserDTO;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.service.LogService;

/**
 * Checks if the postcode in a {@link UserDTO} is valid (depending on minimum size value in
 * {@link ConsultingTypeSettings}.
 *
 */

public class ValidPostcodeValidator implements ConstraintValidator<ValidPostcode, UserDTO> {

  private final ConsultingTypeManager consultingTypeManager;
  private final LogService logService;

  @Autowired
  public ValidPostcodeValidator(ConsultingTypeManager consultingTypeManager,
      LogService logService) {
    this.consultingTypeManager = consultingTypeManager;
    this.logService = logService;
  }

  @Override
  public boolean isValid(UserDTO userDTO, ConstraintValidatorContext context) {

    if (userDTO == null || userDTO.getConsultingType() == null || userDTO.getPostcode() == null) {
      return false;
    }

    ConsultingTypeSettings consultingTypeSettings = consultingTypeManager.getConsultantTypeSettings(
        ConsultingType.values()[Integer.valueOf(userDTO.getConsultingType())]);

    if (consultingTypeSettings.getRegistration() == null) {
      logService.logValidationError(String.format(
          "Could not get registration settings for consulting type %s. Please check configuration",
          userDTO.getConsultingType()));
      return false;
    }

    return userDTO.getPostcode().length() >= consultingTypeSettings.getRegistration()
        .getMinPostcodeSize();
  }

}
