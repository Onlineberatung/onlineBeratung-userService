package de.caritas.cob.userservice.api.model.validation;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.registration.UserRegistrationDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Checks if the postcode of a {@link UserRegistrationDTO} is valid (depending on minimum size value
 * in {@link ConsultingTypeSettings}.
 */

public class ValidPostcodeValidator implements ConstraintValidator<ValidPostcode, Object> {

  private final ConsultingTypeManager consultingTypeManager;

  @Autowired
  public ValidPostcodeValidator(ConsultingTypeManager consultingTypeManager) {
    this.consultingTypeManager = consultingTypeManager;
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {

    if (value == null || !isRegistrationDto(value) || !getConsultingType(value).isPresent()
        || !getPostCode(value).isPresent()) {
      return false;
    }

    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultingTypeSettings(getConsultingType(value).get());

    if (consultingTypeSettings.getRegistration() == null) {
      LogService.logValidationError(String.format(
          "Could not get registration settings for consulting type %s. Please check configuration",
          getConsultingType(value).get()));
      return false;
    }

    return getPostCode(value).get().length() >= consultingTypeSettings.getRegistration()
        .getMinPostcodeSize();
  }

  /**
   * Returns the {@link ConsultingType} of an given object which needs to use the
   * {@link UserRegistrationDTO} interface.
   *
   * @param value Object implementing the {@link UserRegistrationDTO}
   * @return {@link Optional} of {@link ConsultingType}
   */
  private Optional<ConsultingType> getConsultingType(Object value) {
    if (((UserRegistrationDTO) value).getConsultingType() != null) {
      try {
        return Optional.ofNullable(ConsultingType.values()[Integer
            .parseInt(((UserRegistrationDTO) value).getConsultingType())]);
      } catch (Exception e) {
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  /**
   * Returns the post code of an given object which needs to use the {@link UserRegistrationDTO}
   * interface.
   *
   * @param value Object implementing the {@link UserRegistrationDTO}
   * @return {@link String}
   */
  private Optional<String> getPostCode(Object value) {
    if (((UserRegistrationDTO) value).getPostcode() != null) {
      return Optional.ofNullable(((UserRegistrationDTO) value).getPostcode());
    }

    return Optional.empty();
  }

  /**
   * Returns true if given object is of type {@link UserRegistrationDTO}
   *
   * @param value {@link Object}
   * @return true if given object is of type {@link UserRegistrationDTO}
   */
  private boolean isRegistrationDto(Object value) {
    return value instanceof UserRegistrationDTO;
  }
}
