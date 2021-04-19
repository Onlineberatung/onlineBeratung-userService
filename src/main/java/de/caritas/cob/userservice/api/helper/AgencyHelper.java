package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class for agency dependent tasks
 */
@Component
public class AgencyHelper {

  private final AgencyServiceHelper agencyServiceHelper;

  @Autowired
  public AgencyHelper(AgencyServiceHelper agencyServiceHelper) {
    this.agencyServiceHelper = agencyServiceHelper;
  }

  /**
   * Checks if the given agency ID {@link AgencyDTO#getId()} is assigned to the provided consulting ID
   * and returns the corresponding agency as {@link AgencyDTO}.
   *
   * @param agencyId       {@link AgencyDTO#getId()}
   * @param consultingId the consulting ID
   * @return {@link AgencyDTO} or null if agency is not found
   * @throws BadRequestException when the given sector id is not assigned to the
   *                             provided agency
   */
  public AgencyDTO getVerifiedAgency(Long agencyId, int consultingId)
      throws BadRequestException {

    AgencyDTO agencyDTO = null;
    try {
      agencyDTO = agencyServiceHelper.getAgencyWithoutCaching(agencyId);
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      throw new InternalServerErrorException(
          String.format("Could not get agency with id %s to check with consulting type %s",
              agencyId, consultingId));
    }
    if (agencyDTO != null && !(agencyDTO.getConsultingId() == consultingId)) {
      throw new BadRequestException(String.format(
          "The provided agency with id %s is not assigned to the provided consulting type %s",
          agencyId, consultingId));
    }

    return agencyDTO;
  }

  /**
   * Checks if the given agency ID {@link AgencyDTO#getId()} is assigned to the provided consulting ID.
   *
   * @param agencyId       {@link AgencyDTO#getId()}
   * @param consultingId the consulting ID
   * @return <ul>
   *         <li>true if agency is assigned to the provided consulting ID</li>
   *         <li>false if agency is not assigned to the provided consulting ID</li>
   *         </ul>
   * @throws BadRequestException when the given consulting ID is not assigned to the
   *                             provided agency
   */
  public boolean doesConsultingTypeMatchToAgency(Long agencyId, int consultingId)
      throws BadRequestException {

    AgencyDTO agencyDTO = getVerifiedAgency(agencyId, consultingId);

    return agencyDTO != null;
  }
}
