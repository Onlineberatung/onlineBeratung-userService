package de.caritas.cob.userservice.api.admin.facade;

import static de.caritas.cob.userservice.api.model.AgencyTypeDTO.AgencyTypeEnum.DEFAULT_AGENCY;
import static de.caritas.cob.userservice.api.model.AgencyTypeDTO.AgencyTypeEnum.TEAM_AGENCY;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.admin.service.agency.ConsultantAgencyAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminFilterService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation.ConsultantAgencyRelationCreatorService;
import de.caritas.cob.userservice.api.model.AgencyConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.AgencyTypeDTO;
import de.caritas.cob.userservice.api.model.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantFilter;
import de.caritas.cob.userservice.api.model.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
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
  private final @NonNull ConsultantAgencyRelationCreatorService consultantAgencyRelationCreatorService;

  /**
   * Finds a consultant by given consultant id.
   *
   * @param consultantId the id of the consultant to search for
   * @return the generated {@link ConsultantResponseDTO}
   */
  public ConsultantAdminResponseDTO findConsultant(String consultantId) {
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
   * Creates a new {@link Consultant} based on the {@link CreateConsultantDTO} input.
   *
   * @param createConsultantDTO the input data used for {@link Consultant} creation
   * @return the generated and persisted {@link Consultant} representation as {@link
   * ConsultantAdminResponseDTO}
   */
  public ConsultantAdminResponseDTO createNewConsultant(CreateConsultantDTO createConsultantDTO) {
    return this.consultantAdminService.createNewConsultant(createConsultantDTO);
  }

  /**
   * Updates a {@link Consultant} based on the {@link UpdateConsultantDTO} input.
   *
   * @param consultantId        the id of the consultant to update
   * @param updateConsultantDTO the input data used for {@link Consultant} update
   * @return the generated and persisted {@link Consultant} representation as {@link
   * ConsultantAdminResponseDTO}
   */
  public ConsultantAdminResponseDTO updateConsultant(String consultantId,
      UpdateAdminConsultantDTO updateConsultantDTO) {
    return this.consultantAdminService.updateConsultant(consultantId, updateConsultantDTO);
  }

  /**
   * Returns all Agencies for the given consultantId.
   *
   * @param consultantId id of the consultant
   * @return the generated {@link ConsultantAgencyResponseDTO}
   */
  public ConsultantAgencyResponseDTO findConsultantAgencies(String consultantId) {
    return this.consultantAgencyAdminService.findConsultantAgencies(consultantId);
  }

  /**
   * Creates a new {@link ConsultantAgency} based on the consultantId and {@link
   * CreateConsultantAgencyDTO} input.
   *
   * @param consultantId              the consultant to use
   * @param createConsultantAgencyDTO the agencyId and role {@link CreateConsultantAgencyDTO}
   */
  public void createNewConsultantAgency(String consultantId,
      CreateConsultantAgencyDTO createConsultantAgencyDTO) {
    consultantAgencyRelationCreatorService
        .createNewConsultantAgency(consultantId, createConsultantAgencyDTO);
  }

  /**
   * Changes the consultant flag is_team_consultant and assignments for agency type changes.
   *
   * @param agencyId      the id of the changed agency
   * @param agencyTypeDTO the request object containing the target type
   */
  public void changeAgencyType(Long agencyId, AgencyTypeDTO agencyTypeDTO) {
    if (TEAM_AGENCY.equals(agencyTypeDTO.getAgencyType())) {
      this.consultantAgencyAdminService.markAllAssignedConsultantsAsTeamConsultant(agencyId);
    }
    if (DEFAULT_AGENCY.equals(agencyTypeDTO.getAgencyType())) {
      this.consultantAgencyAdminService.removeConsultantsFromTeamSessionsByAgencyId(agencyId);
    }
  }

  /**
   * Marks the {@link ConsultantAgency} as deleted.
   *
   * @param consultantId the consultant id
   * @param agencyId     the agency id
   */
  public void markConsultantAgencyForDeletion(String consultantId, Long agencyId) {
    this.consultantAgencyAdminService.markConsultantAgencyForDeletion(consultantId, agencyId);
  }

  /**
   * Marks the {@link Consultant} as deleted.
   *
   * @param consultantId the consultant id
   */
  public void markConsultantForDeletion(String consultantId) {
    this.consultantAdminService.markConsultantForDeletion(consultantId);
  }

  /**
   * Retrieves all consultants of the agency with given id.
   *
   * @param agencyId the agency id
   * @return the generated {@link AgencyConsultantResponseDTO}
   */
  public AgencyConsultantResponseDTO findConsultantsForAgency(String agencyId) {
    var parsedAgencyId = Long.valueOf(agencyId);
    return this.consultantAgencyAdminService.findConsultantsForAgency(parsedAgencyId);
  }
}
