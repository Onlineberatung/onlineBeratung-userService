package de.caritas.cob.userservice.api.adapters.web.dto.validation;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.registration.mandatoryfields.MandatoryFields;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Helper class for getting the mandatory fields for a given consulting type. */
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
    var extendedConsultingTypeResponseDTO =
        consultingTypeManager.getConsultingTypeSettings(consultingTypeId);
    ensureConsultingTypeSettingsAreNotNull(consultingTypeId, extendedConsultingTypeResponseDTO);
    var registration = extendedConsultingTypeResponseDTO.getRegistration();
    assert nonNull(registration) && nonNull(registration.getMandatoryFields());
    return MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
        registration.getMandatoryFields());
  }

  private void ensureConsultingTypeSettingsAreNotNull(
      String consultingTypeId,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO) {
    if (isNull(extendedConsultingTypeResponseDTO.getRegistration())
        || isNull(extendedConsultingTypeResponseDTO.getRegistration().getMandatoryFields())) {
      throw new InternalServerErrorException(
          String.format(
              "Could not get mandatory fields for consulting type %s. Please check configuration",
              consultingTypeId),
          LogService::logInternalServerError);
    }
  }
}
