package de.caritas.cob.userservice.api.model.validation;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.manager.consultingtype.registration.mandatoryfields.MandatoryFields;
import de.caritas.cob.userservice.api.model.SessionDataDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mandatory fields validator class.
 */
@Component
@RequiredArgsConstructor
public class MandatoryFieldsValidator {

  private final @NonNull ConsultingTypeManager consultingTypeManager;

  /**
   * Validates all mandatory fields for the given {@link SessionDataDTO} depending on its {@link
   * ConsultingType} settings.
   *
   * @param consultingType {@link ConsultingType}
   * @param sessionData {@link SessionDataDTO}
   */
  public void validateFields(ConsultingType consultingType, SessionDataDTO sessionData) {
    MandatoryFields mandatoryFields = this.fetchMandatoryFieldsForConsultingType(consultingType);

    if (mandatoryFields.isAge()) {
      validateAge(sessionData);
    }
    if (mandatoryFields.isState()) {
      validateState(sessionData);
    }
  }

  private MandatoryFields fetchMandatoryFieldsForConsultingType(ConsultingType consultingType) {
    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultingTypeSettings(consultingType);
    ensureConsultingTypeSettingsAreNotNull(consultingType, consultingTypeSettings);
    return consultingTypeSettings.getRegistration().getMandatoryFields();
  }

  private void ensureConsultingTypeSettingsAreNotNull(
      ConsultingType consultingType, ConsultingTypeSettings consultingTypeSettings) {
    if (isNull(consultingTypeSettings.getRegistration())
        || isNull(consultingTypeSettings.getRegistration().getMandatoryFields())) {
      throw new InternalServerErrorException(
          String.format(
              "Could not get mandatory fields for consulting type %s. Please check configuration",
              consultingType),
          LogService::logInternalServerError);
    }
  }

  private void validateAge(SessionDataDTO sessionData) {
    if (isNoNumber(sessionData.getAge())) {
      throw new BadRequestException("Invalid age provided.", LogService::logInfo);
    }
  }

  private void validateState(SessionDataDTO sessionData) {
    if (isNoNumber(sessionData.getState())) {
      throw new BadRequestException("Invalid state provided.", LogService::logInfo);
    }
  }

  private boolean isNoNumber(String number) {
    return !isNumeric(number) && !isParsable(number);
  }
}
