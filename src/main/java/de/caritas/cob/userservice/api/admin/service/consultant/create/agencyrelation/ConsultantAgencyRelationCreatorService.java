package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatAddToGroupOperationService;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantImportService.ImportRecord;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Creator class to generate new {@link ConsultantAgency} instances.
 */
@Service
@RequiredArgsConstructor
public class ConsultantAgencyRelationCreatorService {

  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull AgencyService agencyService;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull ConsultingTypeManager consultingTypeManager;

  /**
   * Creates a new {@link ConsultantAgency} based on the {@link ImportRecord} and agency ids.
   *
   * @param consultantId the consultant id
   * @param agencyIds    the agency ids to be added
   * @param roles        the roles
   * @param logMethod    the methode used for logging
   */
  public void createConsultantAgencyRelations(String consultantId, Set<Long> agencyIds,
      Set<String> roles, Consumer<String> logMethod) {
    checkConsultantHasRoleSet(roles, consultantId);
    agencyIds.stream()
        .map(agencyId -> new ImportRecordAgencyCreationInputAdapter(consultantId, agencyId, roles))
        .forEach(input -> createNewConsultantAgency(input, logMethod));
  }

  /**
   * Creates a new {@link ConsultantAgency} based on the consultantId and {@link
   * CreateConsultantAgencyDTO} input.
   *
   * @param consultantId              the consultant to use
   * @param createConsultantAgencyDTO the agencyId and role ConsultantAgencyAdminResultDTO}
   */
  public void createNewConsultantAgency(String consultantId,
      CreateConsultantAgencyDTO createConsultantAgencyDTO) {
    var adapter = new CreateConsultantAgencyDTOInputAdapter(consultantId,
        createConsultantAgencyDTO);
    createNewConsultantAgency(adapter, LogService::logInfo);
  }

  private void createNewConsultantAgency(ConsultantAgencyCreationInput input,
      Consumer<String> logMethod) {
    var consultant = this.retrieveConsultant(input.getConsultantId());

    var agency = retrieveAgency(input.getAgencyId());
    if (consultingTypeManager.isConsultantBoundedToAgency(agency.getConsultingType())) {
      this.verifyAllAssignedAgenciesHaveSameConsultingType(agency.getConsultingType(), consultant);
    }

    ensureConsultingTypeRoles(input, agency);
    addConsultantToSessions(consultant, agency, logMethod);

    if (isTeamAgencyButNotTeamConsultant(agency, consultant)) {
      consultant.setTeamConsultant(true);
      consultantRepository.save(consultant);
    }

    consultantAgencyService.saveConsultantAgency(buildConsultantAgency(consultant, agency.getId()));
  }

  private void ensureConsultingTypeRoles(ConsultantAgencyCreationInput input, AgencyDTO agency) {
    var roles = consultingTypeManager
        .getConsultingTypeSettings(agency.getConsultingType())
        .getRoles();
    if (nonNull(roles) && nonNull(roles.getConsultant())) {
      var roleSets = roles.getConsultant().getRoleSets();
      for (var roleSetName : input.getRoleSetNames()) {
        roleSets.getOrDefault(roleSetName, Collections.emptyList()).forEach(
            roleName -> keycloakAdminClientService.ensureRole(input.getConsultantId(), roleName));
      }
    }
  }

  private Consultant retrieveConsultant(String consultantId) {
    return this.consultantRepository.findByIdAndDeleteDateIsNull(consultantId)
        .orElseThrow(() -> new BadRequestException(
            String.format("Consultant with id %s does not exist", consultantId)));
  }

  private void checkConsultantHasRoleSet(Set<String> roles, String consultantId) {
    roles.stream()
        .filter(role -> keycloakAdminClientService.userHasRole(consultantId, role))
        .findAny()
        .orElseThrow(() -> new BadRequestException(
            String
                .format("Consultant with id %s does not have the role set %s", consultantId,
                    roles)));
  }

  private AgencyDTO retrieveAgency(Long agencyId) {
    var agencyDto = this.agencyService.getAgencyWithoutCaching(agencyId);
    return Optional.ofNullable(agencyDto)
        .orElseThrow(() -> new BadRequestException(
            String.format("AgencyId %s is not a valid agency", agencyId)));
  }

  private void verifyAllAssignedAgenciesHaveSameConsultingType(int consultingTypeId,
      Consultant consultant) {
    if (nonNull(consultant.getConsultantAgencies())) {
      consultant.getConsultantAgencies().stream()
          .map(ConsultantAgency::getAgencyId)
          .map(this::retrieveAgency)
          .filter(agency -> agency.getConsultingType() != consultingTypeId)
          .findFirst()
          .ifPresent(agency -> {
            throw new BadRequestException(String
                .format("ERROR: different consulting types found than %d for consultant with id %s",
                    consultingTypeId, consultant.getId()));
          });
    }
  }

  private void addConsultantToSessions(Consultant consultant, AgencyDTO agency,
      Consumer<String> logMethod) {
    List<Session> relevantSessions = collectRelevantSessionsToAddConsultant(agency);
    RocketChatAddToGroupOperationService
        .getInstance(this.rocketChatFacade, this.keycloakAdminClientService, logMethod,
            consultingTypeManager)
        .onSessions(relevantSessions)
        .withConsultant(consultant)
        .addToGroupsOrRollbackOnFailure();
  }

  private List<Session> collectRelevantSessionsToAddConsultant(AgencyDTO agency) {
    List<Session> sessionsToAddConsultant = sessionRepository
        .findByAgencyIdAndStatusAndConsultantIsNull(agency.getId(), SessionStatus.NEW);
    if (isTrue(agency.getTeamAgency())) {
      sessionsToAddConsultant.addAll(sessionRepository
          .findByAgencyIdAndStatusAndTeamSessionIsTrue(agency.getId(), SessionStatus.IN_PROGRESS));
    }
    return sessionsToAddConsultant;
  }

  private boolean isTeamAgencyButNotTeamConsultant(AgencyDTO agency,
      Consultant consultant) {
    return isTrue(agency.getTeamAgency()) && !consultant.isTeamConsultant();
  }

  private ConsultantAgency buildConsultantAgency(Consultant consultant, Long agencyId) {
    return ConsultantAgency
        .builder()
        .consultant(consultant)
        .agencyId(agencyId)
        .createDate(nowInUtc())
        .updateDate(nowInUtc())
        .build();
  }

}
