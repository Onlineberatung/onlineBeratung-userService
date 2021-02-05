package de.caritas.cob.userservice.api.admin.service.rocketchat;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;

/**
 * Provider for group operations with Rocket.Chat.
 */
public class RocketChatRemoveFromGroupOperationService extends RocketChatGroupOperation {

  private Session session;
  private Consultant consultant;

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
   * Sets the {@link Session} for group operations.
   *
   * @param session the {@link Session}
   * @return the {@link RocketChatRemoveFromGroupOperationService} instance
   */
  public RocketChatRemoveFromGroupOperationService onSession(Session session) {
    this.session = session;
    return this;
  }

  /**
   * Sets the {@link Consultant}.
   *
   * @param consultant the consultant to add in the groups
   * @return the {@link RocketChatRemoveFromGroupOperationService} instance
   */
  public RocketChatRemoveFromGroupOperationService withConsultant(Consultant consultant) {
    this.consultant = consultant;
    return this;
  }

  /**
   * Removes the given consultant from Rocket.Chat rooms of given session.
   */
  public void removeFromGroupsOrRollbackOnFailure() {
    try {
      removeConsultantFromSession(this.session, this.consultant);
    } catch (Exception e) {
      rollback();
      throw new InternalServerErrorException(
          String.format("ERROR: Failed to remove consultant %s from group %s:",
              consultant.getRocketChatId(), session.getGroupId()), e,
          LogService::logRocketChatError);
    }
  }

  private void rollback() {
    try {
      addConsultantToGroupOfSession(this.session, this.consultant);
    } catch (Exception e) {
      throw new InternalServerErrorException(
          String.format("ERROR: Failed to rollback %s of group %s:",
              resolveTypeOfSession(session), session.getGroupId()), e,
          LogService::logRocketChatError);
    }
  }

}
