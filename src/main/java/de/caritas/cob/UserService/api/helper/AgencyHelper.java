package de.caritas.cob.UserService.api.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;

/**
 * 
 * Helper class for agency dependent tasks
 *
 */
@Component
public class AgencyHelper {

  private final AgencyServiceHelper agencyServiceHelper;

  @Autowired
  public AgencyHelper(AgencyServiceHelper agencyServiceHelper) {
    this.agencyServiceHelper = agencyServiceHelper;
  }

  /**
   * Checks if the given agency ID {@link AgencyDTO#getId()} is assigned to the provided
   * {@link ConsultingType} and returns the corresponding agency as {@link AgencyDTO}.
   * 
   * @param agencyId {@link AgencyDTO#getId()}
   * @param consultingType {@link ConsultingType}
   * @return {@link AgencyDTO}
   * @throws ServiceException when getting the agency information fails
   * @throws BadRequestException when the given {@link ConsultingType} is not assigned to the
   *         provided agency
   */
  public AgencyDTO getVerifiedAgency(Long agencyId, ConsultingType consultingType)
      throws ServiceException, BadRequestException {

    AgencyDTO agencyDTO = null;
    try {
      agencyDTO = agencyServiceHelper.getAgencyWithoutCaching(agencyId);
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      throw new ServiceException(
          String.format("Could not get agency with id %s for Kreuzbund registration", agencyId),
          agencyServiceHelperException);
    }
    if (agencyDTO == null) {
      throw new ServiceException(
          String.format("Could not get agency with id %s for Kreuzbund registration", agencyId));
    }
    if (!agencyDTO.getConsultingType().equals(consultingType)) {
      throw new BadRequestException(String.format(
          "The provided agency with id %s is not assigned to the provided consulting type %s",
          agencyId, consultingType));
    }
    return agencyDTO;
  }

  /**
   * Checks if the given agency ID {@link AgencyDTO#getId()} is assigned to the provided
   * {@link ConsultingType}.
   * 
   * @param agencyId {@link AgencyDTO#getId()}
   * @param consultingType {@link ConsultingType}
   * @return
   *         <ul>
   *         <li>true if agency is assigned to the provided {@link ConsultingType}</li>
   *         <li>false if agency is not assigned to the provided {@link ConsultingType}</li>
   *         </ul>
   * @throws ServiceException when getting the agency information fails
   * @throws BadRequestException when the given {@link ConsultingType} is not assigned to the
   *         provided agency
   */
  public boolean doesConsultingTypeMatchToAgency(Long agencyId, ConsultingType consultingType)
      throws ServiceException, BadRequestException {

    AgencyDTO agencyDTO = getVerifiedAgency(agencyId, consultingType);

    return agencyDTO != null;
  }
}
