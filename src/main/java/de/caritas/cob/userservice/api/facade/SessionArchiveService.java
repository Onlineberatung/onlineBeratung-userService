package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.AuthenticatedUserHelper;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import java.util.function.Consumer;
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
  private @NonNull AuthenticatedUser authenticatedUser;
  private @NonNull AuthenticatedUserHelper authenticatedUserHelper;

  /**
   * Put a session into the archive.
   *
   * @param sessionId the session id
   */
  public void archiveSession(Long sessionId) {
    putSessionToStatus(sessionId,
        SessionStatus.IN_ARCHIVE,
        SessionArchiveService::isValidForArchive);
  }

  /**
   * Reactivate a session.
   *
   * @param sessionId the session id
   */
  public void reactivateSession(Long sessionId) {
    putSessionToStatus(sessionId,
        SessionStatus.IN_PROGRESS,
        SessionArchiveService::isValidForReactivate);
  }

  private void putSessionToStatus(Long sessionId, SessionStatus sessionStatusTo,
      Consumer<Session> sessionValidator) {
    Session session = retrieveSession(sessionId);
    checkSessionPermission(session);
    sessionValidator.accept(session);

    session.setStatus(sessionStatusTo);
    try {
      sessionRepository.save(session);
    } catch (InternalServerErrorException ex) {
      throw new InternalServerErrorException(String
          .format("Could not archive/reactivate session %s for user %s",
              session.getId(), authenticatedUser.getUserId()));
    }
  }

  private Session retrieveSession(Long sessionId) {
    return sessionRepository.findById(sessionId).orElseThrow(
        () -> new NotFoundException(String.format("Session with id %s not found.", sessionId)));
  }

  private void checkSessionPermission(Session session) {
    if (!hasConsultantPermission(session) && !hasUserPermission(session)) {
      throw new ForbiddenException(
          String.format("Archive/reactivate session %s is not allowed for user with id %s",
              session.getId(), authenticatedUser.getUserId()));
    }
  }

  private boolean hasConsultantPermission(Session session) {
    return authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(
        UserRole.CONSULTANT.getValue())
        && authenticatedUserHelper.hasPermissionForSession(session);
  }

  private boolean hasUserPermission(Session session) {
    return authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(
        UserRole.USER.getValue()) && nonNull(session.getUser())
        && session.getUser().getUserId().equals(authenticatedUser.getUserId());
  }

  private static void isValidForArchive(Session session) {
    if (!session.getStatus().equals(SessionStatus.IN_PROGRESS)) {
      throw new ConflictException(
          String.format(
              "Session %s should be archived but has not status IN_PROGRESS",
              session.getId()));
    }
  }

  private static void isValidForReactivate(Session session) {
    if (!session.getStatus().equals(SessionStatus.IN_ARCHIVE)) {
      throw new ConflictException(
          String.format(
              "Session %s should be archived but has not status IN_ARCHIVE",
              session.getId()));
    }
  }

}
