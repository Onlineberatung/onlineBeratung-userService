package de.caritas.cob.userservice.api.helper;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;

/**
 * Helper class to check permissions, authorization, etc. of the currently authenticated user.
 *
 */

@Component
public class AuthenticatedUserHelper {

  private final AuthenticatedUser authenticatedUser;
  private final ConsultantAgencyService consultantAgencyService;

  @Autowired
  public AuthenticatedUserHelper(AuthenticatedUser authenticatedUser,
      ConsultantAgencyService consultantAgencyService) {

    this.authenticatedUser = authenticatedUser;
    this.consultantAgencyService = consultantAgencyService;
  }

  /**
   * Checks if the currently authenticated user has the permission to access the given session.
   *
   * @param session the session
   * @return true, if authenticated user has permission for session
   */
  public boolean hasPermissionForSession(Session session) {

    // Session is assigned directly to consultant
    if (session.getConsultant().getId().equals(authenticatedUser.getUserId())) {
      return true;
    }

    // Session is team session and consultant is assigned to session's agency
    return session.isTeamSession() && consultantAgencyService
        .isConsultantInAgency(authenticatedUser.getUserId(), session.getAgencyId());
  }

  /**
   * Checks if the currently authenticated user has the any of given roles.
   *
   * @param roles the roles to check
   * @return true, if authenticated user has one the roles
   */
  public boolean authenticatedUserRolesContainAnyRoleOf(String... roles) {
    return Arrays.stream(roles)
        .anyMatch(authenticatedUser.getRoles()::contains);
  }

}
