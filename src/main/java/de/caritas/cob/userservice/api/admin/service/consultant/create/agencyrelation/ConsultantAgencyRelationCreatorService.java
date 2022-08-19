package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.ConsultantAgencyStatus;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantImportService.ImportRecord;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Creator class to generate new {@link ConsultantAgency} instances. */
@Service
@RequiredArgsConstructor
public class ConsultantAgencyRelationCreatorService {

  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull AgencyService agencyService;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull RocketChatAsyncHelper rocketChatAsyncHelper;

  /**
   * Creates a new {@link ConsultantAgency} based on the {@link ImportRecord} and agency ids.
   *
   * @param consultantId the consultant id
   * @param agencyIds the agency ids to be added
   * @param roles the roles
   * @param logMethod the methode used for logging
   */
  public void createConsultantAgencyRelations(
      String consultantId, Set<Long> agencyIds, Set<String> roles, Consumer<String> logMethod) {
    checkConsultantHasRoleSet(roles, consultantId);
    agencyIds.stream()
        .map(agencyId -> new ImportRecordAgencyCreationInputAdapter(consultantId, agencyId, roles))
        .forEach(input -> createNewConsultantAgency(input, logMethod));
  }

  /**
   * Creates a new {@link ConsultantAgency} based on the consultantId and {@link
   * CreateConsultantAgencyDTO} input.
   *
   * @param consultantId the consultant to use
   * @param createConsultantAgencyDTO the agencyId and role ConsultantAgencyAdminResultDTO}
   */
  public void createNewConsultantAgency(
      String consultantId, CreateConsultantAgencyDTO createConsultantAgencyDTO) {
    var adapter =
        new CreateConsultantAgencyDTOInputAdapter(consultantId, createConsultantAgencyDTO);
    createNewConsultantAgency(adapter, LogService::logInfo);
  }

  private void createNewConsultantAgency(
      ConsultantAgencyCreationInput input, Consumer<String> logMethod) {
    prepareConsultantAgencyRelation(input);
    completeConsultantAgencyAssigment(input, logMethod);
  }

  public void prepareConsultantAgencyRelation(ConsultantAgencyCreationInput input) {
    var consultant = this.retrieveConsultant(input.getConsultantId());

    var agency = retrieveAgency(input.getAgencyId());
    if (consultingTypeManager.isConsultantBoundedToAgency(agency.getConsultingType())) {
      this.verifyAllAssignedAgenciesHaveSameConsultingType(agency.getConsultingType(), consultant);
    }

    ensureConsultingTypeRoles(input, agency);
    consultantAgencyService.saveConsultantAgency(buildConsultantAgency(consultant, agency.getId()));
  }

  public void completeConsultantAgencyAssigment(
      ConsultantAgencyCreationInput input, Consumer<String> logMethod) {

    var consultant = this.retrieveConsultant(input.getConsultantId());
    var agency = retrieveAgency(input.getAgencyId());

    if (!ConsultantStatus.IN_PROGRESS.equals(consultant.getStatus())) {
      consultant.setStatus(ConsultantStatus.IN_PROGRESS);
      consultantRepository.save(consultant);
    }

    rocketChatAsyncHelper.addConsultantToSessions(
        consultant, agency, logMethod, TenantContext.getCurrentTenant());

    if (isTeamAgencyButNotTeamConsultant(agency, consultant)) {
      consultant.setTeamConsultant(true);
      consultantRepository.save(consultant);
    }
  }

  private void ensureConsultingTypeRoles(ConsultantAgencyCreationInput input, AgencyDTO agency) {
    var roles =
        consultingTypeManager.getConsultingTypeSettings(agency.getConsultingType()).getRoles();
    if (nonNull(roles) && nonNull(roles.getConsultant())) {
      var roleSets = roles.getConsultant().getRoleSets();
      for (var roleSetName : input.getRoleSetNames()) {
        roleSets
            .getOrDefault(roleSetName, Collections.emptyList())
            .forEach(roleName -> identityClient.ensureRole(input.getConsultantId(), roleName));
      }
    }
  }

  private Consultant retrieveConsultant(String consultantId) {
    return this.consultantRepository
        .findByIdAndDeleteDateIsNull(consultantId)
        .orElseThrow(
            () ->
                new BadRequestException(
                    String.format("Consultant with id %s does not exist", consultantId)));
  }

  private void checkConsultantHasRoleSet(Set<String> roles, String consultantId) {
    roles.stream()
        .filter(role -> identityClient.userHasRole(consultantId, role))
        .findAny()
        .orElseThrow(
            () ->
                new BadRequestException(
                    String.format(
                        "Consultant with id %s does not have the role set %s",
                        consultantId, roles)));
  }

  private AgencyDTO retrieveAgency(Long agencyId) {
    var agencyDto = this.agencyService.getAgencyWithoutCaching(agencyId);
    return Optional.ofNullable(agencyDto)
        .orElseThrow(
            () ->
                new BadRequestException(
                    String.format("AgencyId %s is not a valid agency", agencyId)));
  }

  private void verifyAllAssignedAgenciesHaveSameConsultingType(
      int consultingTypeId, Consultant consultant) {
    if (nonNull(consultant.getConsultantAgencies())) {
      consultant.getConsultantAgencies().stream()
          .map(ConsultantAgency::getAgencyId)
          .map(this::retrieveAgency)
          .filter(agency -> agency.getConsultingType() != consultingTypeId)
          .findFirst()
          .ifPresent(
              agency -> {
                throw new BadRequestException(
                    String.format(
                        "ERROR: different consulting types found than %d for consultant with id %s",
                        consultingTypeId, consultant.getId()));
              });
    }
  }

  private boolean isTeamAgencyButNotTeamConsultant(AgencyDTO agency, Consultant consultant) {
    return isTrue(agency.getTeamAgency()) && !consultant.isTeamConsultant();
  }

  private ConsultantAgency buildConsultantAgency(Consultant consultant, Long agencyId) {
    return ConsultantAgency.builder()
        .consultant(consultant)
        .agencyId(agencyId)
        .createDate(nowInUtc())
        .updateDate(nowInUtc())
        .tenantId(consultant.getTenantId())
        .status(ConsultantAgencyStatus.IN_PROGRESS)
        .build();
  }
}
