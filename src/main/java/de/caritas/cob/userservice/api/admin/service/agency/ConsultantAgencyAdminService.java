package de.caritas.cob.userservice.api.admin.service.agency;

import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgencyRepository;
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

  /**
   * Returns all Agencies for the given consultantId.
   *
   * @param consultantId id of the consultant
   * @return the list of agencies for the given consultant
   */
  public ConsultantAgencyAdminResultDTO findConsultantAgencies(String consultantId) {
    Optional<Consultant> consultant = consultantRepository.findById(consultantId);
    if (!consultant.isPresent()) {
      throw new BadRequestException(
          String.format("Consultant with id %s does not exist", consultantId));
    }
    List<ConsultantAgency> agencyList = consultantAgencyRepository
        .findByConsultantId(consultantId);

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
    List<ConsultantAgency> byAgencyId = this.consultantAgencyRepository.findByAgencyId(agencyId);
    if (isEmpty(byAgencyId)) {
      throw new NotFoundException(String.format("Agency with id %s does not exist", agencyId));
    }
    byAgencyId.stream()
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

    this.consultantRepository.findByConsultantAgenciesAgencyIdIn(singletonList(agencyId))
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

}
