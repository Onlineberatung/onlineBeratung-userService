package de.caritas.cob.userservice.api.service.archive;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.AuthenticatedUserHelper;
import de.caritas.cob.userservice.api.repository.session.Session;
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
  private final @NonNull AuthenticatedUserHelper authenticatedUserHelper;

  /**
   * Check if authenticated user has permission for session.
   *
   * @param session the session to check
   */
  public void checkPermission(Session session) {
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

}
