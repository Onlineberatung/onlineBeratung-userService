package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatAddToGroupOperationService;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.AgencyService;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantImportService.ImportRecord;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
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
  private final @NonNull RocketChatService rocketChatService;
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
    ConsultantAgencyCreationInput adapter = new CreateConsultantAgencyDTOInputAdapter(
        consultantId, createConsultantAgencyDTO);
    createNewConsultantAgency(adapter, LogService::logInfo);
  }

  private void createNewConsultantAgency(ConsultantAgencyCreationInput input,
      Consumer<String> logMethod) {
    Consultant consultant = this.retrieveConsultant(input.getConsultantId());

    this.checkConsultantHasRole(input);

    AgencyDTO agency = retrieveAgency(input.getAgencyId());

    if (consultingTypeManager.isConsultantBoundedToAgency(agency.getConsultingId())) {
      this.verifyAllAssignedAgenciesHaveSameConsultingType(agency.getConsultingId(), consultant);
    }

    this.addConsultantToSessions(consultant, agency, logMethod);

    if (isTeamAgencyButNotTeamConsultant(agency, consultant)) {
      consultant.setTeamConsultant(true);
      consultantRepository.save(consultant);
    }

    consultantAgencyService.saveConsultantAgency(buildConsultantAgency(consultant, agency.getId()));
  }

  private Consultant retrieveConsultant(String consultantId) {
    return this.consultantRepository.findByIdAndDeleteDateIsNull(consultantId)
        .orElseThrow(() -> new BadRequestException(
            String.format("Consultant with id %s does not exist", consultantId)));
  }

  private void checkConsultantHasRole(ConsultantAgencyCreationInput input) {
    input.getRoles().stream()
        .filter(role -> keycloakAdminClientService.userHasRole(input.getConsultantId(), role))
        .findAny()
        .orElseThrow(() -> new BadRequestException(
            String
                .format("Consultant with id %s does not have the role %s", input.getConsultantId(),
                    input.getRoles())));
  }

  private AgencyDTO retrieveAgency(Long agencyId) {
    AgencyDTO agencyDto = this.agencyService.getAgencyWithoutCaching(agencyId);
    return Optional.ofNullable(agencyDto)
        .orElseThrow(() -> new BadRequestException(
            String.format("AgencyId %s is not a valid agency", agencyId)));
  }

  private void verifyAllAssignedAgenciesHaveSameConsultingType(int consultingId,
      Consultant consultant) {
    if (nonNull(consultant.getConsultantAgencies())) {
      consultant.getConsultantAgencies().stream()
          .map(ConsultantAgency::getAgencyId)
          .map(this::retrieveAgency)
          .filter(agency -> !(agency.getConsultingId() == consultingId))
          .findFirst()
          .ifPresent(agency -> {
            throw new BadRequestException(String
                .format("ERROR: different consulting types found than %d for consultant with id %s",
                    consultingId, consultant.getId()));
          });
    }
  }

  private void addConsultantToSessions(Consultant consultant, AgencyDTO agency,
      Consumer<String> logMethod) {
    List<Session> relevantSessions = collectRelevantSessionsToAddConsultant(agency);
    RocketChatAddToGroupOperationService
        .getInstance(this.rocketChatService, this.keycloakAdminClientService, logMethod,
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
