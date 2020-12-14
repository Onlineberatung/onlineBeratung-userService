package de.caritas.cob.userservice.api.model.validation;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.manager.consultingType.registration.mandatoryFields.MandatoryFields;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper class for getting the mandatory fields for a given consulting type.
 */
@Component
@RequiredArgsConstructor
public class MandatoryFieldsProvider {

  private final ConsultingTypeManager consultingTypeManager;

  /**
   * Fetch the {@link MandatoryFields} for a consulting type.
   *
   * @param consultingType (required)
   * @return the {@link MandatoryFields} for the given consulting type
   */
  public MandatoryFields fetchMandatoryFieldsForConsultingType(String consultingType) {
    ConsultingTypeSettings consultingTypeSettings = consultingTypeManager.getConsultingTypeSettings(
        ConsultingType.values()[Integer.parseInt(consultingType)]);
    ensureConsultingTypeSettingsAreNotNull(consultingType, consultingTypeSettings);
    return consultingTypeSettings.getRegistration().getMandatoryFields();

  }

  private void ensureConsultingTypeSettingsAreNotNull(String consultingType, ConsultingTypeSettings consultingTypeSettings) {
    if (isNull(consultingTypeSettings.getRegistration())
        || isNull(consultingTypeSettings.getRegistration().getMandatoryFields())) {
      throw new InternalServerErrorException(String.format(
          "Could not get mandatory fields for consulting type %s. Please check configuration",
          consultingType), LogService::logInternalServerError);
    }
  }

}
