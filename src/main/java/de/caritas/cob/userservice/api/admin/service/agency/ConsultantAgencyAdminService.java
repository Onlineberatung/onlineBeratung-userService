package de.caritas.cob.userservice.api.admin.service.agency;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_AGENCY_RELATION_DOES_NOT_EXIST;
import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

/** Service class to handle administrative operations on consultant-agencies. */
@Service
@RequiredArgsConstructor
public class ConsultantAgencyAdminService {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull RemoveConsultantFromRocketChatService removeFromRocketChatService;
  private final @NonNull AgencyService agencyService;
  private final @NonNull AgencyAdminService agencyAdminService;
  private final @NonNull ConsultantAgencyDeletionValidationService agencyDeletionValidationService;

  /**
   * Returns all Agencies for the given consultantId.
   *
   * @param consultantId id of the consultant
   * @return the list of agencies for the given consultant wrapped in a {@link
   *     ConsultantAgencyResponseDTO}
   */
  public ConsultantAgencyResponseDTO findConsultantAgencies(String consultantId) {
    var consultant = consultantRepository.findByIdAndDeleteDateIsNull(consultantId);
    if (consultant.isEmpty()) {
      throw new BadRequestException(
          String.format("Consultant with id %s does not exist", consultantId));
    }

    var consultantAgencyIds =
        consultantAgencyRepository.findByConsultantIdAndDeleteDateIsNull(consultantId).stream()
            .map(ConsultantAgency::getAgencyId)
            .collect(Collectors.toList());

    var agencyList =
        this.agencyAdminService.retrieveAllAgencies().stream()
            .filter(agency -> consultantAgencyIds.contains(agency.getId()))
            .collect(Collectors.toList());

    return ConsultantResponseDTOBuilder.getInstance()
        .withConsultantId(consultantId)
        .withResult(agencyList)
        .build();
  }

  public void appendAgenciesForConsultants(Set<ConsultantDTO> consultants) {
    var consultantIds = consultants.stream().map(ConsultantDTO::getId).collect(Collectors.toSet());

    var consultantAgencies = consultantAgencyRepository.findByConsultantIdIn(consultantIds);

    var agencyIds =
        consultantAgencies.stream().map(ConsultantAgency::getAgencyId).collect(Collectors.toSet());

    var agencies =
        this.agencyAdminService.retrieveAllAgencies().stream()
            .filter(agency -> agencyIds.contains(agency.getId()))
            .map(this::buildCopiedAgency)
            .collect(Collectors.toList());

    enrichConsultantsWithAgencies(consultants, consultantAgencies, agencies);
  }

  @SneakyThrows
  private AgencyAdminResponseDTO buildCopiedAgency(
      de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO
          agency) {
    var result = new AgencyAdminResponseDTO();
    BeanUtils.copyProperties(result, agency);

    return result;
  }

  private void enrichConsultantsWithAgencies(
      Set<ConsultantDTO> consultants,
      List<ConsultantAgency> consultantAgencies,
      List<AgencyAdminResponseDTO> agencies) {
    consultants.forEach(
        consultant -> {
          var agencyIdsOfConsultant =
              consultantAgencies.stream()
                  .filter(
                      consultantAgency -> onlyRelevantAgencyRelations(consultant, consultantAgency))
                  .map(ConsultantAgency::getAgencyId)
                  .collect(Collectors.toSet());

          var agenciesOfConsultant =
              agencies.stream()
                  .filter(agency -> agencyIdsOfConsultant.contains(agency.getId()))
                  .collect(Collectors.toList());

          consultant.setAgencies(agenciesOfConsultant);
        });
  }

  private boolean onlyRelevantAgencyRelations(
      ConsultantDTO consultant, ConsultantAgency consultantAgency) {
    var isConsultantAgencyRelatedToConsultant =
        consultant.getId().equals(consultantAgency.getConsultant().getId());
    if (isConsultantAgencyRelatedToConsultant) {
      var isConsultantDeleted = notStringNull(consultant.getDeleteDate());
      var isConsultantAgencyNotDeleted = isNull(consultantAgency.getDeleteDate());
      return isConsultantDeleted || isConsultantAgencyNotDeleted;
    }
    return false;
  }

  private boolean notStringNull(String stringToCheck) {
    return isNotBlank(stringToCheck) && !"null".equals(stringToCheck);
  }

  /**
   * Marks all assigned consultants of team agency as team consultants.
   *
   * @param agencyId the id of the agency
   */
  public void markAllAssignedConsultantsAsTeamConsultant(Long agencyId) {
    this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(agencyId).stream()
        .map(ConsultantAgency::getConsultant)
        .filter(this::notAlreadyTeamConsultant)
        .forEach(this::markConsultantAsTeamConsultant);
  }

  private boolean notAlreadyTeamConsultant(Consultant consultant) {
    return !consultant.isTeamConsultant();
  }

  private void markConsultantAsTeamConsultant(Consultant consultant) {
    consultant.setTeamConsultant(true);
    this.consultantRepository.save(consultant);
  }

  /**
   * Removes the consultant from all Rocket.Chat rooms where he is not directly assigned, changes
   * regarding sessions to non team sessions and removes the team consultant identifier when
   * consultant has no other team agency assigned.
   *
   * @param agencyId the id of the agency
   */
  public void removeConsultantsFromTeamSessionsByAgencyId(Long agencyId) {
    List<Session> teamSessionsInProgress =
        this.sessionRepository.findByAgencyIdAndStatusAndTeamSessionIsTrue(
            agencyId, SessionStatus.IN_PROGRESS);

    this.removeFromRocketChatService.removeConsultantFromSessions(teamSessionsInProgress);
    teamSessionsInProgress.forEach(this::changeSessionToNonTeamSession);

    this.consultantRepository
        .findByConsultantAgenciesAgencyIdInAndDeleteDateIsNull(singletonList(agencyId))
        .stream()
        .filter(consultant -> noOtherTeamAgency(consultant, agencyId))
        .forEach(this::removeTeamConsultantFlag);
  }

  private void changeSessionToNonTeamSession(Session session) {
    session.setTeamSession(false);
    this.sessionRepository.save(session);
  }

  private boolean noOtherTeamAgency(Consultant consultant, Long agencyId) {
    return consultant.getConsultantAgencies().stream()
        .map(this::toAgencyDto)
        .filter(agencyDTO -> !agencyId.equals(agencyDTO.getId()))
        .noneMatch(AgencyDTO::getTeamAgency);
  }

  private AgencyDTO toAgencyDto(ConsultantAgency consultantAgency) {
    return this.agencyService.getAgency(consultantAgency.getAgencyId());
  }

  private void removeTeamConsultantFlag(Consultant consultant) {
    consultant.setTeamConsultant(false);
    this.consultantRepository.save(consultant);
  }

  /**
   * Marks an {@link ConsultantAgency} as deleted if consultant is not the last in agency or agency
   * is offline and no open enquiries are assigned.
   *
   * @param consultantId the consultant id
   * @param agencyId the agency id
   */
  public void markConsultantAgencyForDeletion(String consultantId, Long agencyId) {
    List<ConsultantAgency> consultantAgencies =
        this.consultantAgencyRepository.findByConsultantIdAndAgencyIdAndDeleteDateIsNull(
            consultantId, agencyId);
    if (isEmpty(consultantAgencies)) {
      throw new CustomValidationHttpStatusException(CONSULTANT_AGENCY_RELATION_DOES_NOT_EXIST);
    }
    consultantAgencies.stream()
        .filter(consultantAgency -> isNull(consultantAgency.getDeleteDate()))
        .forEach(this::markAsDeleted);
  }

  public void markConsultantAgenciesForDeletion(String consultantId, List<Long> agencyIds) {

    agencyIds.forEach(
        agencyId -> {
          List<ConsultantAgency> result =
              consultantAgencyRepository.findByConsultantIdAndAgencyIdAndDeleteDateIsNull(
                  consultantId, agencyId);
          result.forEach(this::markAsDeleted);
        });
  }

  private void markAsDeleted(ConsultantAgency consultantAgency) {
    this.agencyDeletionValidationService.validateAndMarkForDeletion(consultantAgency);
    consultantAgency.setDeleteDate(nowInUtc());
    this.consultantAgencyRepository.save(consultantAgency);
  }

  /**
   * retrieves all consultants of the agency with given id.
   *
   * @param agencyId the agency id
   * @return the generated {@link AgencyConsultantResponseDTO}
   */
  public AgencyConsultantResponseDTO findConsultantsForAgency(Long agencyId) {
    var consultants =
        this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(agencyId).stream()
            .map(ConsultantAgency::getConsultant)
            .map(
                de.caritas.cob.userservice.api.admin.service.consultant.ConsultantResponseDTOBuilder
                    ::getInstance)
            .map(
                de.caritas.cob.userservice.api.admin.service.consultant.ConsultantResponseDTOBuilder
                    ::buildResponseDTO)
            .collect(Collectors.toList());
    return AgencyConsultantResponseDTOBuilder.getInstance(consultants)
        .withAgencyId(String.valueOf(agencyId))
        .build();
  }
}
