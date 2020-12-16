package de.caritas.cob.userservice.api.admin.facade;

import de.caritas.cob.userservice.api.admin.service.ConsultantAgencyAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminFilterService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminService;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.model.ConsultantFilter;
import de.caritas.cob.userservice.api.model.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.model.GetConsultantResponseDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate admin functions for consultants.
 */
@Service
@RequiredArgsConstructor
public class ConsultantAdminFacade {

  private final @NonNull ConsultantAdminService consultantAdminService;
  private final @NonNull ConsultantAdminFilterService consultantAdminFilterService;
  private final @NonNull ConsultantAgencyAdminService consultantAgencyAdminService;

  /**
   * Finds a consultant by given consultant id.
   *
   * @param consultantId the id of the consultant to search for
   * @return the generated {@link GetConsultantResponseDTO}
   */
  public GetConsultantResponseDTO findConsultant(String consultantId) {
    return this.consultantAdminService.findConsultantById(consultantId);
  }

  /**
   * Searches for consultants by given {@link ConsultantFilter}, limits the result by perPage and
   * generates a {@link ConsultantSearchResultDTO} containing hal links.
   *
   * @param consultantFilter the filter object containing filter values
   * @param page             the current requested page
   * @param perPage          the amount of items in one page
   * @return the result list
   */
  public ConsultantSearchResultDTO findFilteredConsultants(Integer page, Integer perPage,
      ConsultantFilter consultantFilter) {
    return this.consultantAdminFilterService.findFilteredConsultants(page, perPage,
        consultantFilter);
  }

  /**
   * Returns all Agencies for the given consultantId.
   *
   * @param consultantId id of the consultant
   * @return the list of agencies for the given consultant
   */
  public ConsultantAgencyAdminResultDTO findConsultantAgencies(String consultantId) {
    return consultantAgencyAdminService.findConsultantAgencies(consultantId);
  }
}
