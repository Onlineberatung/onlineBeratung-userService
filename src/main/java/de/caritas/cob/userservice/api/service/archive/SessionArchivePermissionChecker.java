package de.caritas.cob.userservice.api.service.archive;

import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
 * Permission checker for sessions to archive / dearchive.
 */
@Component
@RequiredArgsConstructor
public class SessionArchivePermissionChecker {

  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull ConsultantAgencyService consultantAgencyService;

  /**
   * Check if authenticated user has permission for session.
   *
   * @param session the session to check
   */
  public void checkPermission(Session session) {
    if (!hasConsultantPermission(session) && !session.isAdvised(authenticatedUser.getUserId())) {
      var template = "Archive/reactivate session %s is not allowed for user with id %s";
      var message = String.format(template, session.getId(), authenticatedUser.getUserId());

      throw new ForbiddenException(message);
    }
  }

  private boolean hasConsultantPermission(Session session) {
    var userId = authenticatedUser.getUserId();
    var agencyId = session.getAgencyId();

    return session.isAdvisedBy(userId) || session.isTeamSession()
        && consultantAgencyService.isConsultantInAgency(userId, agencyId);
  }

}
