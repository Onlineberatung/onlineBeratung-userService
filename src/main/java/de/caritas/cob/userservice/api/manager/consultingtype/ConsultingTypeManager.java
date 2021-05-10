package de.caritas.cob.userservice.api.manager.consultingtype;

import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.service.ConsultingTypeService;
import java.util.List;
import javax.annotation.PostConstruct;
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
   * @param consultingTypeId The consulting ID for which the seetings are searched
   * @return {@link ExtendedConsultingTypeResponseDTO} for the provided consulting ID
   * @throws MissingConsultingTypeException when no settings for provided consulting type where
   *                                        found
   */
  public ExtendedConsultingTypeResponseDTO getConsultingTypeSettings(int consultingTypeId) throws MissingConsultingTypeException {
    try {
      return consultingTypeService.getExtendedConsultingTypeResponseDTO(consultingTypeId);
    }
    catch(RestClientException ex) {
      throw new MissingConsultingTypeException(
          String.format("No settings for consulting type %s found.", consultingTypeId));
    }
  }

  public ExtendedConsultingTypeResponseDTO getConsultingTypeSettings(String consultingTypeId) {
    return getConsultingTypeSettings(Integer.parseInt(consultingTypeId));
  }

  /**
   * Returns the a list with all consulting type IDs
   *
   * @return {@link List} with all consulting type IDS
   */
  public List<Integer> getAllConsultingTypeIds() {
    return consultingTypeService.getAllConsultingTypeIds();
  }

  public boolean isConsultantBoundedToAgency(int consultingTypeId) {
    return consultingTypeService.getExtendedConsultingTypeResponseDTO(consultingTypeId).getConsultantBoundedToConsultingType();
  }

}
