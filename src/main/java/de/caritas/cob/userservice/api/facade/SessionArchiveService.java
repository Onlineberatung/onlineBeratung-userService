package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade for capsuling the archive functionality.
 */
@Service
@RequiredArgsConstructor
public class SessionArchiveService {

  private @NonNull SessionRepository sessionRepository;
  private @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private @NonNull AuthenticatedUser authenticatedUser;

  /**
   * Put a session into the archive.
   *
   * @param sessionId the session id
   */
  public void archiveSession(Long sessionId) {

    Session session = retrieveSession(sessionId);
    validateSession(session);
    checkSessionPermission(session);

    session.setStatus(SessionStatus.IN_ARCHIVE);
    try {
      sessionRepository.save(session);
    } catch (InternalServerErrorException ex) {
      throw new InternalServerErrorException(String
          .format("Could not archive session %s for consultant %s",
              session.getId(), authenticatedUser.getUserId()));
    }
  }

  private Session retrieveSession(Long sessionId) {
    return sessionRepository.findById(sessionId).orElseThrow(
        () -> new NotFoundException(String.format("Session with id %s not found.", sessionId)));
  }

  private void validateSession(Session session) {
    if (!session.getStatus().equals(SessionStatus.IN_PROGRESS)) {
      throw new ConflictException(
          String.format("Session %s should be archived but is not in progress.",
              session.getId()));
    }
  }

  private void checkSessionPermission(Session session) {
    if (!isConsultantAssignedToSession(session)
        && !isTeamSessionAndConsultantInSessionAgency(session)) {
      throw new ForbiddenException(
          String.format("Put session %s in the archive is not allowed for consultant with id %s",
              session.getId(), authenticatedUser.getUserId()));
    }
  }

  private boolean isConsultantAssignedToSession(Session session) {
    return nonNull(session.getConsultant())
        && session.getConsultant().getId().equals(authenticatedUser.getUserId());
  }

  private boolean isTeamSessionAndConsultantInSessionAgency(Session session) {
    List<ConsultantAgency> consultantAgencies =
        consultantAgencyRepository.findByConsultantId(authenticatedUser.getUserId());
    return session.isTeamSession() && consultantAgencies.stream()
        .anyMatch(ca -> session.getAgencyId().equals(ca.getAgencyId()));
  }

}
