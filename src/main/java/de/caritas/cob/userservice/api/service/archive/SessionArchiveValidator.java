package de.caritas.cob.userservice.api.service.archive;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import org.springframework.stereotype.Component;

/*
 * Validation for sessions to archive / dearchive.
 */
@Component
public class SessionArchiveValidator {

  /**
   * Check whether the given session is valid for archiving.
   *
   * @param session the session to check
   */
  public void isValidForArchiving(Session session) {
    if (!session.getStatus().equals(SessionStatus.IN_PROGRESS)) {
      throw new ConflictException(
          String.format(
              "Session %s should be archived but has not status IN_PROGRESS", session.getId()));
    }
  }

  /**
   * Check whether the given session is valid for dearchiving.
   *
   * @param session the session to check
   */
  public void isValidForDearchiving(Session session) {
    if (!session.getStatus().equals(SessionStatus.IN_ARCHIVE)) {
      throw new ConflictException(
          String.format(
              "Session %s should be archived but has not status IN_ARCHIVE", session.getId()));
    }
  }
}
