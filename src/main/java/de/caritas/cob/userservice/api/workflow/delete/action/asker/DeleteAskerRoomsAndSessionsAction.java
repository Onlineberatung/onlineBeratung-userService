package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.workflow.delete.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.port.out.MonitoringRepository;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.SessionDataRepository;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Delete action for sessions and Rocket.Chat rooms of a {@link User}.
 */
@Component
public class DeleteAskerRoomsAndSessionsAction extends DeleteRoomsAndSessionAction implements
    ActionCommand<AskerDeletionWorkflowDTO> {

  @Autowired
  public DeleteAskerRoomsAndSessionsAction(SessionRepository sessionRepository,
      SessionDataRepository sessionDataRepository,
      MonitoringRepository monitoringRepository,
      RocketChatService rocketChatService) {
    super(sessionRepository, sessionDataRepository, monitoringRepository, rocketChatService);
  }

  /**
   * Deletes all Rocket.Chat rooms containing all messages and uploads and sessions in database of a
   * given {@link User}.
   *
   * @param actionTarget the {@link AskerDeletionWorkflowDTO} with the user for session and room
   *                     deletion
   */
  @Override
  public void execute(AskerDeletionWorkflowDTO actionTarget) {
    this.sessionRepository.findByUser(actionTarget.getUser())
        .forEach(
            session -> performSessionDeletion(session, actionTarget.getDeletionWorkflowErrors()));
  }

}
