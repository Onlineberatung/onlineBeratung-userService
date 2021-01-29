package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provider for group operations with Rocket.Chat.
 */
public class RocketChatGroupOperationService {

  private final RocketChatService rocketChatService;
  private final KeycloakAdminClientService keycloakAdminClientService;

  private List<Session> sessions;
  private Consultant consultant;
  private Consumer<String> logMethod;

  private RocketChatGroupOperationService(RocketChatService rocketChatService,
      KeycloakAdminClientService keycloakAdminClientService) {
    this.rocketChatService = requireNonNull(rocketChatService);
    this.keycloakAdminClientService = requireNonNull(keycloakAdminClientService);
  }

  /**
   * Creates the {@link RocketChatGroupOperationService} instance.
   *
   * @param rocketChatService the target service to perform operations
   * @return the {@link RocketChatGroupOperationService} instance
   */
  public static RocketChatGroupOperationService getInstance(RocketChatService rocketChatService,
      KeycloakAdminClientService keycloakAdminClientService) {
    return new RocketChatGroupOperationService(rocketChatService, keycloakAdminClientService);
  }

  /**
   * Sets the {@link Session} list for group operations.
   *
   * @param sessions the {@link Session} list
   * @return the {@link RocketChatGroupOperationService} instance
   */
  public RocketChatGroupOperationService onSessions(List<Session> sessions) {
    this.sessions = sessions;
    return this;
  }

  /**
   * Sets the {@link Consultant}.
   *
   * @param consultant the consultant to add in the groups
   * @return the {@link RocketChatGroupOperationService} instance
   */
  public RocketChatGroupOperationService withConsultant(Consultant consultant) {
    this.consultant = consultant;
    return this;
  }

  /**
   * Sets the {@link Consumer} of the logging.
   *
   * @param logMethod the logging method
   * @return the {@link RocketChatGroupOperationService} instance
   */
  public RocketChatGroupOperationService withLogMethod(Consumer<String> logMethod) {
    this.logMethod = logMethod;
    return this;
  }

  /**
   * Adds the user to configured groups.
   */
  public void addToGroupsOrRollbackOnFailure() {
    this.sessions.forEach(this::addToSpecificSessionOrRollbackOnFailure);
  }

  private void addToSpecificSessionOrRollbackOnFailure(Session session) {
    try {
      addConsultantToGroupOfSession(session);
    } catch (Exception e) {
      rollback();
      throw new InternalServerErrorException(
          String.format(
              "ERROR: Consultant could not be added to rc group %s: Technical user could not be"
                  + " added to group (%s).",
              session.getGroupId(), resolveTypeOfSession(session)), e,
          LogService::logRocketChatError);
    }
  }

  private void addConsultantToGroupOfSession(Session session)
      throws RocketChatAddUserToGroupException, RocketChatUserNotInitializedException {
    rocketChatService.addTechnicalUserToGroup(session.getGroupId());

    RocketChatOperationConditionProvider operationConditionProvider =
        new RocketChatOperationConditionProvider(this.keycloakAdminClientService, session,
            this.consultant);

    if (operationConditionProvider.canAddToRocketChatGroup()) {
      rocketChatService.addUserToGroup(this.consultant.getRocketChatId(), session.getGroupId());
      logMethod.accept(String.format("Consultant added to rc group %s (%s).",
          session.getGroupId(), resolveTypeOfSession(session)));
    }

    if (operationConditionProvider.canAddToRocketChatFeedbackGroup()) {
      rocketChatService
          .addUserToGroup(this.consultant.getRocketChatId(), session.getFeedbackGroupId());
      logMethod.accept(String.format("Consultant added to rc feedback group %s (%s).",
          session.getFeedbackGroupId(), resolveTypeOfSession(session)));
    }

    removeTechnicalUserFromRocketChatGroup(logMethod, session);
  }

  private String resolveTypeOfSession(Session session) {
    return session.getStatus().equals(SessionStatus.NEW) ? "enquiry" : "team-session";
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

  private void rollback() {
    this.sessions.forEach(this::removeUserFromSession);
  }

  private void removeUserFromSession(Session session) {
    try {
      removeUsersWithTechnicalUser(session);
    } catch (Exception e) {
      throw new InternalServerErrorException(
          String.format("ERROR: Failed to rollback %s of group %s:",
              resolveTypeOfSession(session), session.getGroupId()), e,
          LogService::logRocketChatError);
    }
  }

  private void removeUsersWithTechnicalUser(Session session)
      throws RocketChatAddUserToGroupException, RocketChatUserNotInitializedException, RocketChatRemoveUserFromGroupException, RocketChatGetGroupMembersException {
    this.rocketChatService.addTechnicalUserToGroup(session.getGroupId());

    if (isUserInRocketChatGroup(session.getGroupId())) {
      this.rocketChatService
          .removeUserFromGroup(this.consultant.getRocketChatId(), session.getGroupId());
    }

    if (isUserInRocketChatGroup(session.getFeedbackGroupId())) {
      this.rocketChatService
          .removeUserFromGroup(this.consultant.getRocketChatId(), session.getFeedbackGroupId());
    }

    this.rocketChatService.removeTechnicalUserFromGroup(session.getGroupId());
  }

  private boolean isUserInRocketChatGroup(String rcGroupId)
      throws RocketChatGetGroupMembersException {
    return this.rocketChatService.getMembersOfGroup(rcGroupId).stream()
        .anyMatch(groupMember -> groupMember.get_id().equals(this.consultant.getRocketChatId()));
  }

}
