package de.caritas.cob.UserService.api.model.validation;

import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.UserService.api.model.IRegistrationDto;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.service.LogService;

/**
 * Checks if the postcode of a {@link IRegistrationDto} is valid (depending on minimum size value in
 * {@link ConsultingTypeSettings}.
 *
 */

public class ValidPostcodeValidator implements ConstraintValidator<ValidPostcode, Object> {

  private final ConsultingTypeManager consultingTypeManager;
  private final LogService logService;

  @Autowired
  public ValidPostcodeValidator(ConsultingTypeManager consultingTypeManager,
      LogService logService) {
    this.consultingTypeManager = consultingTypeManager;
    this.logService = logService;
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {

    if (value == null || !isRegistrationDto(value) || !getConsultingType(value).isPresent()
        || !getPostCode(value).isPresent()) {
      return false;
    }

    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultantTypeSettings(getConsultingType(value).get());

    if (consultingTypeSettings.getRegistration() == null) {
      logService.logValidationError(String.format(
          "Could not get registration settings for consulting type %s. Please check configuration",
          getConsultingType(value).get()));
      return false;
    }

    return getPostCode(value).get().length() >= consultingTypeSettings.getRegistration()
        .getMinPostcodeSize();
  }

  /**
   * Returns the {@link ConsultingType} of an given object which needs to use the
   * {@link IRegistrationDto} interface.
   * 
   * @param value Object implementing the {@link IRegistrationDto}
   * @return {@link Optional} of {@link ConsultingType}
   */
  private Optional<ConsultingType> getConsultingType(Object value) {
    if (((IRegistrationDto) value).getConsultingType() != null) {
      try {
        return Optional.ofNullable(ConsultingType.values()[Integer
            .valueOf(((IRegistrationDto) value).getConsultingType())]);
      } catch (Exception e) {
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  /**
   * Returns the post code of an given object which needs to use the {@link IRegistrationDto}
   * interface.
   * 
   * @param value Object implementing the {@link IRegistrationDto}
   * @return {@link String}
   */
  private Optional<String> getPostCode(Object value) {
    if (((IRegistrationDto) value).getPostcode() != null) {
      return Optional.ofNullable(((IRegistrationDto) value).getPostcode());
    }

    return Optional.empty();
  }

  /**
   * Returns true if given object is of type {@link IRegistrationDto}
   * 
   * @param value {@link Object}
   * @return true if given object is of type {@link IRegistrationDto}
   */
  private boolean isRegistrationDto(Object value) {
    return value instanceof IRegistrationDto;
  }
}
