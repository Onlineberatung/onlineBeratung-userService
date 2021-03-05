package de.caritas.cob.userservice.api.admin.service.agency;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.CONSULTANT_AGENCY_RELATION_DOES_NOT_EXIST;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class to handle administrative operations on consultant-agencies.
 */
@Service
@RequiredArgsConstructor
public class ConsultantAgencyAdminService {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull ConsultantRepository consultantRepository;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull RemoveConsultantFromRocketChatService removeFromRocketChatService;
  private final @NonNull AgencyServiceHelper agencyServiceHelper;
  private final @NonNull ConsultantAgencyDeletionValidationService agencyDeletionValidationService;

  /**
   * Returns all Agencies for the given consultantId.
   *
   * @param consultantId id of the consultant
   * @return the list of agencies for the given consultant
   */
  public ConsultantAgencyAdminResultDTO findConsultantAgencies(String consultantId) {
    Optional<Consultant> consultant = consultantRepository
        .findByIdAndDeleteDateIsNull(consultantId);
    if (!consultant.isPresent()) {
      throw new BadRequestException(
          String.format("Consultant with id %s does not exist", consultantId));
    }
    List<ConsultantAgency> agencyList = consultantAgencyRepository
        .findByConsultantIdAndDeleteDateIsNull(consultantId);

    return ConsultantAgencyAdminResultDTOBuilder
        .getInstance()
        .withConsultantId(consultantId)
        .withResult(agencyList)
        .build();
  }

  /**
   * Marks all assigned consultants of team agency as team consultants.
   *
   * @param agencyId the id of the agency
   */
  public void markAllAssignedConsultantsAsTeamConsultant(Long agencyId) {
    this.consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(agencyId)
        .stream()
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
    List<Session> teamSessionsInProgress = this.sessionRepository
        .findByAgencyIdAndStatusAndTeamSessionIsTrue(agencyId, SessionStatus.IN_PROGRESS);

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
    try {
      return this.agencyServiceHelper.getAgency(consultantAgency.getAgencyId());
    } catch (AgencyServiceHelperException e) {
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }
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
   * @param agencyId     the agency id
   */
  public void markConsultantAgencyForDeletion(String consultantId, Long agencyId) {
    List<ConsultantAgency> consultantAgencies =
        this.consultantAgencyRepository
            .findByConsultantIdAndAgencyIdAndDeleteDateIsNull(consultantId, agencyId);
    if (isEmpty(consultantAgencies)) {
      throw new CustomValidationHttpStatusException(CONSULTANT_AGENCY_RELATION_DOES_NOT_EXIST);
    }
    consultantAgencies.stream()
        .filter(consultantAgency -> isNull(consultantAgency.getDeleteDate()))
        .forEach(this::markAsDeleted);
  }

  private void markAsDeleted(ConsultantAgency consultantAgency) {
    this.agencyDeletionValidationService.validateForDeletion(consultantAgency);
    consultantAgency.setDeleteDate(nowInUtc());
    this.consultantAgencyRepository.save(consultantAgency);
  }

}
