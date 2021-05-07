package de.caritas.cob.userservice.api.helper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
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
   * Checks if the given agency ID {@link AgencyDTO#getId()} is assigned to the provided {@link
   * ConsultingType} and returns the corresponding agency as {@link AgencyDTO}.
   *
   * @param agencyId       {@link AgencyDTO#getId()}
   * @param consultingType {@link ConsultingType}
   * @return {@link AgencyDTO} or null if agency is not found
   */
  public AgencyDTO getVerifiedAgency(Long agencyId, ConsultingType consultingType) {

    AgencyDTO agencyDTO = agencyService.getAgencyWithoutCaching(agencyId);
    if (nonNull(agencyDTO) && !agencyDTO.getConsultingType().equals(consultingType)) {
      throw new BadRequestException(String.format(
          "The provided agency with id %s is not assigned to the provided consulting type %s",
          agencyId, consultingType));
    }

    return agencyDTO;
  }

  public void checkIfConsultingTypeMatchesToAgency(UserDTO userDTO) {
    ConsultingType consultingType =
        ConsultingType.values()[Integer.parseInt(userDTO.getConsultingType())];

    if (isNull(getVerifiedAgency(userDTO.getAgencyId(), consultingType))) {
      throw new BadRequestException(String.format("Agency with id %s does not match to consulting"
          + " type %s", userDTO.getAgencyId(), consultingType.getValue()));
    }
  }
}
