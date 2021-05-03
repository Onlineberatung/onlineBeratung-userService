package de.caritas.cob.userservice.api.model.validation;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.manager.consultingtype.registration.mandatoryfields.MandatoryFields;
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
   * @param consultingTypeId (required)
   * @return the {@link MandatoryFields} for the given consulting type
   */
  public MandatoryFields fetchMandatoryFieldsForConsultingType(String consultingTypeId) {
    ConsultingTypeSettings consultingTypeSettings = consultingTypeManager
        .getConsultingTypeSettings(consultingTypeId);
    ensureConsultingTypeSettingsAreNotNull(consultingTypeId, consultingTypeSettings);
    return consultingTypeSettings.getRegistration().getMandatoryFields();

  }

  private void ensureConsultingTypeSettingsAreNotNull(String consultingTypeId,
      ConsultingTypeSettings consultingTypeSettings) {
    if (isNull(consultingTypeSettings.getRegistration())
        || isNull(consultingTypeSettings.getRegistration().getMandatoryFields())) {
      throw new InternalServerErrorException(String.format(
          "Could not get mandatory fields for consulting type %s. Please check configuration",
          consultingTypeId), LogService::logInternalServerError);
    }
  }

}
