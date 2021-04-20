package de.caritas.cob.userservice.api.helper;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.AgencyService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Verifier class for agency verifications.
 */
@Component
@RequiredArgsConstructor
public class AgencyVerifier {

  private final @NonNull AgencyService agencyService;

  /**
   * Checks if the given agency ID {@link AgencyDTO#getId()} is assigned to the provided consulting ID
   * and returns the corresponding agency as {@link AgencyDTO}.
   *
   * @param agencyId       {@link AgencyDTO#getId()}
   * @param consultingTypeId the consulting Id
   * @return {@link AgencyDTO} or null if agency is not found
   */
  public AgencyDTO getVerifiedAgency(Long agencyId, int consultingTypeId) {

    AgencyDTO agencyDTO = agencyService.getAgencyWithoutCaching(agencyId);
    if (nonNull(agencyDTO) && !agencyDTO.getConsultingType().equals(consultingTypeId)) {
      throw new BadRequestException(String.format(
          "The provided agency with id %s is not assigned to the provided consulting type %s",
          agencyId, consultingTypeId));
    }

    return agencyDTO;
  }

  /**
   * Checks if the given agency ID {@link AgencyDTO#getId()} is assigned to the provided consulting ID
   *
   * @param agencyId       {@link AgencyDTO#getId()}
   * @param consultingTypeId the consulting ID
   * @return <ul>
   *         <li>true if agency is assigned to the provided {@link ConsultingType}</li>
   *         <li>false if agency is not assigned to the provided {@link ConsultingType}</li>
   *         </ul>
   */
  public boolean doesConsultingTypeMatchToAgency(Long agencyId, int consultingTypeId) {
    AgencyDTO agencyDTO = getVerifiedAgency(agencyId, consultingTypeId);
    return nonNull(agencyDTO);
  }
}
