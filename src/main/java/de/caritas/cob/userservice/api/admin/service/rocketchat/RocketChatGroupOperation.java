package de.caritas.cob.userservice.api.admin.service.rocketchat;

import static de.caritas.cob.userservice.api.authorization.Authorities.Authority.VIEW_ALL_FEEDBACK_SESSIONS;
import static de.caritas.cob.userservice.api.authorization.Authorities.Authority.VIEW_ALL_PEER_SESSIONS;
import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class RocketChatGroupOperation {

  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;

  protected Consumer<String> logMethod = LogService::logInfo;

  void addConsultantToGroupOfSession(Session session, Consultant consultant, ConsultingTypeManager consultingTypeManager) {
    var operationConditionProvider =
        new RocketChatOperationConditionProvider(this.keycloakAdminClientService, session,
            consultant, consultingTypeManager);

    if (operationConditionProvider.canAddToRocketChatGroup()) {
      rocketChatFacade.addUserToRocketChatGroup(consultant.getRocketChatId(), session.getGroupId());
      logMethod.accept(String.format("Consultant added to rc group %s (%s).",
          session.getGroupId(), resolveTypeOfSession(session)));
    }

    if (operationConditionProvider.canAddToRocketChatFeedbackGroup()) {
      rocketChatFacade
          .addUserToRocketChatGroup(consultant.getRocketChatId(), session.getFeedbackGroupId());
      logMethod.accept(String.format("Consultant added to rc feedback group %s (%s).",
          session.getFeedbackGroupId(), resolveTypeOfSession(session)));
    }
  }

  String resolveTypeOfSession(Session session) {
    if (SessionStatus.NEW.equals(session.getStatus())) {
      return "enquiry";
    }
    return session.isTeamSession() ? "team-session" : "standard-session";
  }

  void removeConsultantFromSession(Session session, Consultant consultant) {
    if (isUserInRocketChatGroup(session.getGroupId(), consultant)
        && consultantHasNoAuthorityTo(VIEW_ALL_PEER_SESSIONS, consultant)) {
      this.rocketChatFacade
          .removeUserFromGroup(consultant.getRocketChatId(), session.getGroupId());
    }

    if (isUserInRocketChatGroup(session.getFeedbackGroupId(), consultant)
        && consultantHasNoAuthorityTo(VIEW_ALL_FEEDBACK_SESSIONS, consultant)) {
      this.rocketChatFacade
          .removeUserFromGroup(consultant.getRocketChatId(), session.getFeedbackGroupId());
    }
  }

  private boolean isUserInRocketChatGroup(String rcGroupId, Consultant consultant) {
    if (isBlank(rcGroupId)) {
      return false;
    }
    return this.rocketChatFacade.retrieveRocketChatMembers(rcGroupId).stream()
        .anyMatch(groupMember -> groupMember.get_id().equals(consultant.getRocketChatId()));
  }

  private boolean consultantHasNoAuthorityTo(String authorityValue, Consultant consultant) {
    return !keycloakAdminClientService.userHasAuthority(consultant.getId(), authorityValue);
  }

}
