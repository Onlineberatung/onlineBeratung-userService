package de.caritas.cob.userservice.api.deleteworkflow.action.asker;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.deleteworkflow.model.SessionDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataRepository;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteSingleRoomAndSessionAction extends DeleteRoomsAndSessionAction implements
    ActionCommand<SessionDeletionWorkflowDTO> {

  @Autowired
  public DeleteSingleRoomAndSessionAction(SessionRepository sessionRepository,
      SessionDataRepository sessionDataRepository,
      MonitoringRepository monitoringRepository,
      RocketChatService rocketChatService) {
    super(sessionRepository, sessionDataRepository, monitoringRepository, rocketChatService);
  }

  /**
   * Deletes the given {@link Session} in the database with the related Rocket.Chat room containing all messages and uploads.
   *
   * @param actionTarget the {@link SessionDeletionWorkflowDTO} with the session to delete
   */
  @Override
  public void execute(SessionDeletionWorkflowDTO actionTarget) {
    performSessionDeletion(actionTarget.getSession(), actionTarget.getDeletionWorkflowErrors());
  }

}
