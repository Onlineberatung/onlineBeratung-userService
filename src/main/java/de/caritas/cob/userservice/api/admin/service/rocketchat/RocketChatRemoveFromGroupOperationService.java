package de.caritas.cob.userservice.api.admin.service.rocketchat;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.LogService;
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
      IdentityClient identityClient, ConsultingTypeManager consultingTypeManager) {
    super(rocketChatFacade, identityClient);
    this.consultingTypeManager = consultingTypeManager;
  }

  /**
   * Creates the {@link RocketChatRemoveFromGroupOperationService} instance.
   *
   * @param rocketChatFacade the target service to perform operations
   * @return the {@link RocketChatRemoveFromGroupOperationService} instance
   */
  public static RocketChatRemoveFromGroupOperationService getInstance(
      RocketChatFacade rocketChatFacade, IdentityClient identityClient,
      ConsultingTypeManager consultingTypeManager) {
    return new RocketChatRemoveFromGroupOperationService(rocketChatFacade,
        identityClient, consultingTypeManager);
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
  public void removeFromGroup() {
    this.consultantsToRemoveFromSessions.forEach(
        ((session, consultants) -> removeConsultantsFromSessionGroup(session.getGroupId(),
            consultants)));
  }

  /**
   * Removes the given consultant from Rocket.Chat group of given session with rollback on error.
   */
  public void removeFromGroupOrRollbackOnFailure() {
    this.consultantsToRemoveFromSessions.forEach(this::performGroupRemove);
  }

  /**
   * Removes the given consultant from Rocket.Chat feedback group of given session.
   */
  public void removeFromFeedbackGroup() {
    this.consultantsToRemoveFromSessions.forEach(
        ((session, consultants) -> removeConsultantsFromSessionGroup(session.getFeedbackGroupId(),
            consultants)));
  }

  /**
   * Removes the given consultant from Rocket.Chat feedback group of given session with rollback on
   * error.
   */
  public void removeFromFeedbackGroupOrRollbackOnFailure() {
    this.consultantsToRemoveFromSessions.forEach(this::performFeedbackGroupRemove);
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
