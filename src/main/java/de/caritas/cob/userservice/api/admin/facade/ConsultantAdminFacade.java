package de.caritas.cob.userservice.api.admin.facade;

import static de.caritas.cob.userservice.api.adapters.web.dto.AgencyTypeDTO.AgencyTypeEnum.DEFAULT_AGENCY;
import static de.caritas.cob.userservice.api.adapters.web.dto.AgencyTypeDTO.AgencyTypeEnum.TEAM_AGENCY;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyTypeDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort.FieldEnum;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort.OrderEnum;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.admin.service.agency.ConsultantAgencyAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminFilterService;
import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation.ConsultantAgencyRelationCreatorService;
import de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation.CreateConsultantAgencyDTOInputAdapter;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Facade to encapsulate admin functions for consultants. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultantAdminFacade {

  private final @NonNull ConsultantAdminService consultantAdminService;
  private final @NonNull ConsultantAdminFilterService consultantAdminFilterService;
  private final @NonNull ConsultantAgencyAdminService consultantAgencyAdminService;
  private final @NonNull ConsultantAgencyRelationCreatorService
      consultantAgencyRelationCreatorService;

  private final @NonNull AdminUserFacade adminUserFacade;

  private final @NonNull AuthenticatedUser authenticatedUser;

  private final @NonNull AgencyService agencyService;

  @Value("${multitenancy.enabled}")
  private boolean multiTenancyEnabled;

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
   * @param page the current requested page
   * @param perPage the amount of items in one page
   * @return the result list
   */
  public ConsultantSearchResultDTO findFilteredConsultants(
      Integer page, Integer perPage, ConsultantFilter consultantFilter, Sort sort) {
    sort = getValidSorter(sort);
    var filteredConsultants =
        this.consultantAdminFilterService.findFilteredConsultants(
            page, perPage, consultantFilter, sort);
    retrieveAndMergeAgenciesToConsultants(filteredConsultants);

    return filteredConsultants;
  }

  private Sort getValidSorter(Sort sort) {
    if (sort == null
        || Stream.of(FieldEnum.values()).noneMatch(providedSortFieldIgnoringCase(sort))) {
      sort = new Sort();
      sort.setField(FieldEnum.LASTNAME);
      sort.setOrder(OrderEnum.ASC);
    }
    return sort;
  }

  private Predicate<FieldEnum> providedSortFieldIgnoringCase(Sort sort) {
    return field -> {
      if (nonNull(sort.getField())) {
        return field.getValue().equalsIgnoreCase(sort.getField().getValue());
      }
      return false;
    };
  }

  private void retrieveAndMergeAgenciesToConsultants(
      ConsultantSearchResultDTO filteredConsultants) {
    if (nonNull(filteredConsultants)) {
      var consultants =
          filteredConsultants.getEmbedded().stream()
              .map(ConsultantAdminResponseDTO::getEmbedded)
              .collect(Collectors.toSet());
      consultantAgencyAdminService.appendAgenciesForConsultants(consultants);
    }
  }

  /**
   * Creates a new {@link Consultant} based on the {@link CreateConsultantDTO} input.
   *
   * @param createConsultantDTO the input data used for {@link Consultant} creation
   * @return the generated and persisted {@link Consultant} representation as {@link
   *     ConsultantAdminResponseDTO}
   */
  public ConsultantAdminResponseDTO createNewConsultant(CreateConsultantDTO createConsultantDTO) {
    return this.consultantAdminService.createNewConsultant(createConsultantDTO);
  }

  /**
   * Updates a {@link Consultant} based on the {@link UpdateConsultantDTO} input.
   *
   * @param consultantId the id of the consultant to update
   * @param updateConsultantDTO the input data used for {@link Consultant} update
   * @return the generated and persisted {@link Consultant} representation as {@link
   *     ConsultantAdminResponseDTO}
   */
  public ConsultantAdminResponseDTO updateConsultant(
      String consultantId, UpdateAdminConsultantDTO updateConsultantDTO) {
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
   * @param consultantId the consultant to use
   * @param createConsultantAgencyDTO the agencyId and role {@link CreateConsultantAgencyDTO}
   */
  public void createNewConsultantAgency(
      String consultantId, CreateConsultantAgencyDTO createConsultantAgencyDTO) {
    consultantAgencyRelationCreatorService.createNewConsultantAgency(
        consultantId, createConsultantAgencyDTO);
  }

  /**
   * Changes the consultant flag is_team_consultant and assignments for agency type changes.
   *
   * @param agencyId the id of the changed agency
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
   * @param agencyId the agency id
   */
  public void markConsultantAgencyForDeletion(String consultantId, Long agencyId) {
    this.consultantAgencyAdminService.markConsultantAgencyForDeletion(consultantId, agencyId);
  }

  /**
   * Marks given list of agencies assigned to given consultant for deletion.
   *
   * @param consultantId given consultant id
   * @param agencyIds agencies that need to be removed from consultant
   */
  public void markConsultantAgenciesForDeletion(String consultantId, List<Long> agencyIds) {
    consultantAgencyAdminService.markConsultantAgenciesForDeletion(consultantId, agencyIds);
  }

  /**
   * Marks the {@link Consultant} as deleted.
   *
   * @param consultantId the consultant id
   */
  public void markConsultantForDeletion(String consultantId, Boolean forceDeleteSessions) {
    this.consultantAdminService.markConsultantForDeletion(consultantId, forceDeleteSessions);
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

  /**
   * Creates consultant agencies from given consultant id and agencies list and sets to status
   * IN_PROGRESS.
   *
   * @param consultantId given consultant
   * @param agencies list of agencies
   */
  public void prepareConsultantAgencyRelation(
      String consultantId, List<CreateConsultantAgencyDTO> agencies) {
    agencies.forEach(
        agency ->
            this.consultantAgencyRelationCreatorService.prepareConsultantAgencyRelation(
                new CreateConsultantAgencyDTOInputAdapter(consultantId, agency)));
  }

  /**
   * Completes the assigment process of a consultant to given list of agencies and sets status of
   * each relation to CREATED if successfully executed.
   *
   * @param consultantId
   * @param agencies
   */
  public void completeConsultantAgencyAssigment(
      String consultantId, List<CreateConsultantAgencyDTO> agencies) {
    agencies.forEach(
        agency ->
            this.consultantAgencyRelationCreatorService.completeConsultantAgencyAssigment(
                new CreateConsultantAgencyDTOInputAdapter(consultantId, agency),
                LogService::logInfo));
  }

  /**
   * Determines which agencies should be set for deletion process.
   *
   * @param consultantId given consultant
   * @param newList new list agencies that consultant belongs to
   * @return filtered list of existing @{@link ConsultantAgency} ready for deletion
   */
  public List<Long> filterAgencyListForDeletion(
      String consultantId, List<CreateConsultantAgencyDTO> newList) {
    var newListIds =
        newList.stream().map(CreateConsultantAgencyDTO::getAgencyId).collect(Collectors.toList());
    var persistedAgencyIds =
        consultantAgencyAdminService.findConsultantAgencies(consultantId).getEmbedded().stream()
            .map(agencyAdminResponse -> agencyAdminResponse.getEmbedded().getId())
            .collect(Collectors.toList());
    return persistedAgencyIds.stream()
        .filter(persistedAgencyId -> !newListIds.contains(persistedAgencyId))
        .collect(Collectors.toList());
  }

  /**
   * Determines which from new agencies should be created.
   *
   * @param consultantId given consultant
   * @param newList new list of agencies that consultant belongs to
   */
  public void filterAgencyListForCreation(
      String consultantId, List<CreateConsultantAgencyDTO> newList) {
    var persistedAgencyIds =
        consultantAgencyAdminService.findConsultantAgencies(consultantId).getEmbedded().stream()
            .map(agencyAdminFullResponse -> agencyAdminFullResponse.getEmbedded().getId())
            .collect(Collectors.toList());
    var filteredList =
        newList.stream()
            .filter(agency -> !persistedAgencyIds.contains(agency.getAgencyId()))
            .collect(Collectors.toList());
    newList.clear();
    newList.addAll(filteredList);
  }

  public void checkPermissionsToAssignedAgencies(List<CreateConsultantAgencyDTO> agencyList) {
    if (authenticatedUser.hasRestrictedAgencyPriviliges()) {
      List<Long> adminUserAgencyIds =
          adminUserFacade.findAdminUserAgencyIds(authenticatedUser.getUserId());
      List<Long> agencyIdsFromTheRequest =
          agencyList.stream()
              .map(CreateConsultantAgencyDTO::getAgencyId)
              .collect(Collectors.toList());

      if (!adminUserAgencyIds.containsAll(agencyIdsFromTheRequest)) {
        log.warn(
            "User does not have access to some of the agencies. Admin agencies {}, requested agencies to  update: {}",
            adminUserAgencyIds,
            agencyIdsFromTheRequest);
        throw new ForbiddenException(
            "Does not have permissions to update some of the agencies from the request");
      }
    }
  }

  public void checkAssignedAgenciesMatchConsultantTenant(
      String consultantId, List<CreateConsultantAgencyDTO> agencyList) {

    if (multiTenancyEnabled) {
      ConsultantAdminResponseDTO consultantById =
          consultantAdminService.findConsultantById(consultantId);
      validateConsultantExistsAndHasTenantAssigned(consultantId, consultantById);
      Long consultantTenantId = consultantById.getEmbedded().getTenantId().longValue();
      checkAssignedAgenciesMatchConsultantTenant(agencyList, consultantTenantId);
    }
  }

  private void checkAssignedAgenciesMatchConsultantTenant(
      List<CreateConsultantAgencyDTO> agencyList, Long consultantTenantId) {
    agencyList.stream()
        .map(a -> agencyService.getAgency(a.getAgencyId()))
        .map(a -> a.getTenantId())
        .filter(agencyTenantId -> !agencyTenantId.equals(consultantTenantId))
        .findAny()
        .ifPresent(
            agencyTenantId -> {
              log.warn(
                  "Tenant of the consultant does not match tenant of the agency. "
                      + "Consultant tenant {}, agency tenant {}. Requested agencies to  update: {}",
                  consultantTenantId,
                  agencyTenantId,
                  agencyList);
              throw new BadRequestException(
                  "Tenant of the consultant does not match tenant of the agency");
            });
  }

  private void validateConsultantExistsAndHasTenantAssigned(
      String consultantId, ConsultantAdminResponseDTO consultantById) {
    if (consultantById == null || consultantById.getEmbedded() == null) {
      log.warn("Consultant with id {} not found", consultantId);
      throw new BadRequestException("Consultant not found");
    }
    if (consultantById.getEmbedded().getTenantId() == null) {
      log.warn("Consultant has no tenant assigned ", consultantId);
      throw new BadRequestException("Consultant has no tenant assigned");
    }
  }
}
