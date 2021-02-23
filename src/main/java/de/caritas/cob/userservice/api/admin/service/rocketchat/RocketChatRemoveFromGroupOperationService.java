package de.caritas.cob.userservice.api.admin.service.rocketchat;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.List;
import java.util.Map;

/**
 * Provider for group operations with Rocket.Chat.
 */
public class RocketChatRemoveFromGroupOperationService extends RocketChatGroupOperation {

  private Map<Session, List<Consultant>> consultantsToRemoveFromSessions;

  private RocketChatRemoveFromGroupOperationService(RocketChatService rocketChatService,
      KeycloakAdminClientService keycloakAdminClientService) {
    super(rocketChatService, keycloakAdminClientService);
  }

  /**
   * Creates the {@link RocketChatRemoveFromGroupOperationService} instance.
   *
   * @param rocketChatService the target service to perform operations
   * @return the {@link RocketChatRemoveFromGroupOperationService} instance
   */
  public static RocketChatRemoveFromGroupOperationService getInstance(
      RocketChatService rocketChatService, KeycloakAdminClientService keycloakAdminClientService) {
    return new RocketChatRemoveFromGroupOperationService(rocketChatService,
        keycloakAdminClientService);
  }

  /**
   * Sets the {@link Map} containing the {@link Session}s with a {@link List} of {@link Consultant}s
   * to remove from Rocket.Chat groups.
   *
   * @param sessionConsultants the {@link Map} containing all consultants who will be removed from
   *                           sessions
   * @return the {@link RocketChatRemoveFromGroupOperationService} instance
   */
  public RocketChatRemoveFromGroupOperationService onSessionConsultants(
      Map<Session, List<Consultant>> sessionConsultants) {
    this.consultantsToRemoveFromSessions = sessionConsultants;
    return this;
  }

  /**
   * Removes the given consultant from Rocket.Chat rooms of given session.
   */
  public void removeFromGroupsOrRollbackOnFailure() {
    this.consultantsToRemoveFromSessions.forEach((session, consultants) ->
        consultants.forEach(consultant -> performRemove(session, consultant)));
  }

  private void performRemove(Session session, Consultant consultant) {
    try {
      removeConsultantFromSession(session, consultant);
    } catch (Exception e) {
      rollback();
      throw new InternalServerErrorException(
          String.format("ERROR: Failed to remove consultant %s from group %s:",
              consultant.getRocketChatId(), session.getGroupId()), e,
          LogService::logRocketChatError);
    }
  }

  private void rollback() {
    this.consultantsToRemoveFromSessions.forEach((session, consultants) ->
        consultants.forEach(consultant -> performRollback(session, consultant)));
  }

  private void performRollback(Session session, Consultant consultant) {
    try {
      addConsultantToGroupOfSession(session, consultant);
    } catch (Exception e) {
      throw new InternalServerErrorException(
          String.format("ERROR: Failed to rollback %s of group %s:",
              resolveTypeOfSession(session), session.getGroupId()), e,
          LogService::logRocketChatError);
    }
  }

}
