package de.caritas.cob.userservice.api.service.agency;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.model.AgencyDTO;

/**
 * Provides loading of agencyies without bean creation.
 */
public class AgencyLoader {

  private static AgencyService agencyService;

  private AgencyLoader() {}

  /**
   * Sets the {@link AgencyService} instance.
   *
   * @param agencyService the required {@link AgencyService}
   */
  public static void setAgencyService(AgencyService agencyService) {
    AgencyLoader.agencyService = agencyService;
  }

  /**
   * Retrieves the {@link AgencyDTO} by given id.
   *
   * @param id the id of the agency
   * @return the {@link AgencyDTO}
   */
  public static AgencyDTO getAgency(Long id) {
    return nonNull(agencyService) ? agencyService.getAgency(id) : null;
  }

}
