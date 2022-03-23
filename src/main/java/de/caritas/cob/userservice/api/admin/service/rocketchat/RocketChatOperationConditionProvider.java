package de.caritas.cob.userservice.api.admin.service.rocketchat;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Provides conditions used for Rocket.Chat group interactions.
 */
@RequiredArgsConstructor
class RocketChatOperationConditionProvider {

  private final @NonNull IdentityClient identityClient;
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

  private Boolean canAddToTeamConsultingSession() {
    var consultingTypeSettings = consultingTypeManager
        .getConsultingTypeSettings(this.session.getConsultingTypeId());
    return (nonNull(consultingTypeSettings) && isFalse(
        consultingTypeSettings.getExcludeNonMainConsultantsFromTeamSessions()))
        || isMainConsultant();
  }

  private boolean isMainConsultant() {
    return identityClient
        .userHasAuthority(this.consultant.getId(), AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS)
        || identityClient
        .userHasRole(this.consultant.getId(), UserRole.MAIN_CONSULTANT.name());
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
