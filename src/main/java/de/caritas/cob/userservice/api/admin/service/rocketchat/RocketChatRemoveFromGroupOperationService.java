package de.caritas.cob.userservice.api.admin.service.rocketchat;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.List;
import java.util.Map;

/**
 * Provider for group operations with Rocket.Chat.
 */
public class RocketChatRemoveFromGroupOperationService extends RocketChatGroupOperation {

  private Map<Session, List<Consultant>> consultantsToRemoveFromSessions;
  private final ConsultingTypeManager consultingTypeManager;
  private static final String FAILED_TO_REMOVE_CONSULTANTS_ERROR =
      "Failed to remove consultants from Rocket.Chat groups %s for session %s:";

  private RocketChatRemoveFromGroupOperationService(RocketChatFacade rocketChatFacade,
      KeycloakAdminClientService keycloakAdminClientService,
        ConsultingTypeManager consultingTypeManager) {
    super(rocketChatFacade, keycloakAdminClientService);
    this.consultingTypeManager = consultingTypeManager;
  }

  /**
   * Creates the {@link RocketChatRemoveFromGroupOperationService} instance.
   *
   * @param rocketChatFacade the target service to perform operations
   * @return the {@link RocketChatRemoveFromGroupOperationService} instance
   */
  public static RocketChatRemoveFromGroupOperationService getInstance(
      RocketChatFacade rocketChatFacade, KeycloakAdminClientService keycloakAdminClientService, ConsultingTypeManager consultingTypeManager) {
    return new RocketChatRemoveFromGroupOperationService(rocketChatFacade,
        keycloakAdminClientService, consultingTypeManager);
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
   * Removes the given consultant from Rocket.Chat group and feedback group of given session.
   */
  public void removeFromGroupsOrRollbackOnFailure() {
    this.consultantsToRemoveFromSessions.forEach((session, consultants) ->
        consultants.forEach(consultant -> performGroupsRemove(session, consultants)));
  }

  private void performGroupsRemove(Session session, List<Consultant> consultants) {
    try {
      removeConsultantsFromSessionGroups(session, consultants);
    } catch (Exception e) {
      rollback();
      throw new InternalServerErrorException(
          String.format(FAILED_TO_REMOVE_CONSULTANTS_ERROR, session.getGroupId(), session.getId()),
          e, LogService::logRocketChatError);
    }
  }

  /**
   * Removes the given consultant from Rocket.Chat group of given session.
   */
  public void removeFromGroupOrRollbackOnFailure() {
    this.consultantsToRemoveFromSessions.forEach((session, consultants) ->
        consultants.forEach(consultant -> performGroupRemove(session, consultants)));
  }

  /**
   * Removes the given consultant from Rocket.Chat feedback group of given session.
   */
  public void removeFromFeedbackGroupOrRollbackOnFailure() {
    this.consultantsToRemoveFromSessions.forEach((session, consultants) ->
        consultants.forEach(consultant -> performFeedbackGroupRemove(session, consultants)));
  }

  private void performGroupRemove(Session session, List<Consultant> consultants) {
    try {
      removeConsultantsFromSessionGroup(session.getGroupId(), consultants);
    } catch (Exception e) {
      rollback();
      throw new InternalServerErrorException(
          String.format(FAILED_TO_REMOVE_CONSULTANTS_ERROR, session.getGroupId(), session.getId()),
          e, LogService::logRocketChatError);
    }
  }

  private void performFeedbackGroupRemove(Session session, List<Consultant> consultants) {
    try {
      removeConsultantsFromSessionGroup(session.getFeedbackGroupId(), consultants);
    } catch (Exception e) {
      rollback();
      throw new InternalServerErrorException(
          String.format(FAILED_TO_REMOVE_CONSULTANTS_ERROR, session.getFeedbackGroupId(),
              session.getId()), e, LogService::logRocketChatError);
    }
  }

  private void rollback() {
    this.consultantsToRemoveFromSessions.forEach((session, consultants) ->
        consultants.forEach(consultant -> performRollback(session, consultant)));
  }

  private void performRollback(Session session, Consultant consultant) {
    try {
      addConsultantToGroupOfSession(session, consultant, consultingTypeManager);
    } catch (Exception e) {
      throw new InternalServerErrorException(
          String.format("ERROR: Failed to rollback %s of group %s:",
              resolveTypeOfSession(session), session.getGroupId()), e,
          LogService::logRocketChatError);
    }
  }

}
