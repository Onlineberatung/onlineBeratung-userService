package de.caritas.cob.userservice.api.manager.consultingtype;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.service.ConsultingTypeService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@Getter
@RequiredArgsConstructor
public class ConsultingTypeManager {

  private final @NonNull ConsultingTypeService consultingTypeService;

  /**
   * Returns the {@link ExtendedConsultingTypeResponseDTO} for the provided consulting ID.
   *
   * @param consultingTypeId The consulting ID for which the settings are searched
   * @return {@link ExtendedConsultingTypeResponseDTO} for the provided consulting ID
   */
  public ExtendedConsultingTypeResponseDTO getConsultingTypeSettings(int consultingTypeId) {
    try {
      return consultingTypeService.getExtendedConsultingTypeResponseDTO(consultingTypeId);
    } catch (RestClientException ex) {
      throw new MissingConsultingTypeException(
          String.format("No settings for consulting type %s found.", consultingTypeId));
    }
  }

  /**
   * Returns the {@link ExtendedConsultingTypeResponseDTO} for the provided consulting ID.
   *
   * @param consultingTypeId The consulting ID for which the settings are searched
   * @return {@link ExtendedConsultingTypeResponseDTO} for the provided consulting ID
   */
  public ExtendedConsultingTypeResponseDTO getConsultingTypeSettings(String consultingTypeId) {
    return getConsultingTypeSettings(Integer.parseInt(consultingTypeId));
  }

  /**
   * Returns the a list with all consulting type IDs
   *
   * @return {@link List} with all consulting type IDS
   */
  public List<Integer> getAllConsultingTypeIds() {
    return consultingTypeService.getAllConsultingTypeIds(TenantContext.getCurrentTenant());
  }

  public boolean isConsultantBoundedToAgency(int consultingTypeId) {
    var extendedConsultingTypeResponseDTO =
        consultingTypeService.getExtendedConsultingTypeResponseDTO(consultingTypeId);
    return isTrue(extendedConsultingTypeResponseDTO.getConsultantBoundedToConsultingType());
  }
}
