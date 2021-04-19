package de.caritas.cob.userservice.api.admin.service.rocketchat;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Provides conditions used for Rocket.Chat group interactions.
 */
@RequiredArgsConstructor
class RocketChatOperationConditionProvider {

  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull Session session;
  private final @NonNull Consultant consultant;
  private final @NonNull ConsultingTypeManager consultingTypeManager;

  /**
   * Checks if the current {@link Consultant} can be added to Rocket.Chat group.
   *
   * @return true if consultant can be added
   */
  boolean canAddToRocketChatGroup() {
    return isEnquiry() || isTeamSessionAndMainConsultant();
  }

  private boolean isEnquiry() {
    return this.session.getStatus().equals(SessionStatus.NEW);
  }

  private boolean isTeamSessionAndMainConsultant() {
    return this.session.getStatus().equals(SessionStatus.IN_PROGRESS)
        && this.session.isTeamSession() && canAddToTeamConsultingSession();
  }

  private boolean canAddToTeamConsultingSession() {
    return !consultingTypeManager.getConsultingTypeSettings(this.session.getConsultingId())
        .isExcludeNonMainConsultantsFromTeamSessions() || isMainConsultant();
  }

  private boolean isMainConsultant() {
    return keycloakAdminClientService
        .userHasAuthority(this.consultant.getId(), Authority.VIEW_ALL_FEEDBACK_SESSIONS)
        || keycloakAdminClientService
        .userHasRole(this.consultant.getId(), UserRole.U25_MAIN_CONSULTANT.name());
  }

  /**
   * Checks if a given {@link Consultant} can be added to a Rocket.Chat feedback group.
   *
   * @return true if {@link Session} has feedback room and {@link Session} is an enquiry or {@link
   * Consultant} is a main consultant
   */
  boolean canAddToRocketChatFeedbackGroup() {
    if (isNull(this.session.getFeedbackGroupId())) {
      return false;
    }
    return isEnquiry() || isMainConsultant();
  }

}
