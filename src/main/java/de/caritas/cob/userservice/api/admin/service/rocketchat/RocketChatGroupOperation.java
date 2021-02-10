package de.caritas.cob.userservice.api.admin.service.rocketchat;

import static org.apache.commons.lang3.StringUtils.isBlank;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class RocketChatGroupOperation {

  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;

  protected Consumer<String> logMethod = LogService::logInfo;

  void addConsultantToGroupOfSession(Session session, Consultant consultant)
      throws RocketChatAddUserToGroupException, RocketChatUserNotInitializedException {
    rocketChatService.addTechnicalUserToGroup(session.getGroupId());

    RocketChatOperationConditionProvider operationConditionProvider =
        new RocketChatOperationConditionProvider(this.keycloakAdminClientService, session,
           consultant);

    if (operationConditionProvider.canAddToRocketChatGroup()) {
      rocketChatService.addUserToGroup(consultant.getRocketChatId(), session.getGroupId());
      logMethod.accept(String.format("Consultant added to rc group %s (%s).",
          session.getGroupId(), resolveTypeOfSession(session)));
    }

    if (operationConditionProvider.canAddToRocketChatFeedbackGroup()) {
      rocketChatService
          .addUserToGroup(consultant.getRocketChatId(), session.getFeedbackGroupId());
      logMethod.accept(String.format("Consultant added to rc feedback group %s (%s).",
          session.getFeedbackGroupId(), resolveTypeOfSession(session)));
    }

    removeTechnicalUserFromRocketChatGroup(logMethod, session);
  }

  String resolveTypeOfSession(Session session) {
    if (SessionStatus.NEW.equals(session.getStatus())) {
      return "enquiry";
    }
    return session.isTeamSession() ? "team-session" : "standard-session";
  }

  private void removeTechnicalUserFromRocketChatGroup(Consumer<String> logMethod, Session session)
      throws RocketChatUserNotInitializedException {
    try {
      rocketChatService.removeTechnicalUserFromGroup(session.getGroupId());
    } catch (RocketChatRemoveUserFromGroupException e) {
      logMethod.accept(String.format(
          "ERROR: Technical user could not be removed from rc group %s (%s).", session.getGroupId(),
          resolveTypeOfSession(session)));
    }
  }

  void removeConsultantFromSession(Session session, Consultant consultant)
      throws RocketChatAddUserToGroupException, RocketChatUserNotInitializedException, RocketChatRemoveUserFromGroupException, RocketChatGetGroupMembersException {
    this.rocketChatService.addTechnicalUserToGroup(session.getGroupId());

    if (isUserInRocketChatGroup(session.getGroupId(), consultant)) {
      this.rocketChatService
          .removeUserFromGroup(consultant.getRocketChatId(), session.getGroupId());
    }

    if (isUserInRocketChatGroup(session.getFeedbackGroupId(), consultant)) {
      this.rocketChatService
          .removeUserFromGroup(consultant.getRocketChatId(), session.getFeedbackGroupId());
    }

    this.rocketChatService.removeTechnicalUserFromGroup(session.getGroupId());
  }

  private boolean isUserInRocketChatGroup(String rcGroupId, Consultant consultant)
      throws RocketChatGetGroupMembersException {
    if (isBlank(rcGroupId)) {
      return false;
    }
    return this.rocketChatService.getMembersOfGroup(rcGroupId).stream()
        .anyMatch(groupMember -> groupMember.get_id().equals(consultant.getRocketChatId()));
  }

}
