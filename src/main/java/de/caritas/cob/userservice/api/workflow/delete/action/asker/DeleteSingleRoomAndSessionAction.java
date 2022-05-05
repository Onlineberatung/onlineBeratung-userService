package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.workflow.delete.model.SessionDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.port.out.MonitoringRepository;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.SessionDataRepository;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteSingleRoomAndSessionAction extends DeleteRoomsAndSessionAction implements
    ActionCommand<SessionDeletionWorkflowDTO> {

  /**
   * Constructor.
   *
   * @param sessionRepository     a {@link SessionRepository} instance
   * @param sessionDataRepository a {@link SessionDataRepository} instance
   * @param monitoringRepository  a {@link MonitoringRepository} instance
   * @param rocketChatService     a {@link RocketChatService} instance
   */
  @Autowired
  public DeleteSingleRoomAndSessionAction(SessionRepository sessionRepository,
      SessionDataRepository sessionDataRepository,
      MonitoringRepository monitoringRepository,
      RocketChatService rocketChatService) {
    super(sessionRepository, sessionDataRepository, monitoringRepository, rocketChatService);
  }

  /**
   * Deletes the given {@link Session} in the database with the related Rocket.Chat room containing
   * all messages and uploads.
   *
   * @param actionTarget the {@link SessionDeletionWorkflowDTO} with the session to delete
   */
  @Override
  public void execute(SessionDeletionWorkflowDTO actionTarget) {
    performSessionDeletion(actionTarget.getSession(), actionTarget.getDeletionWorkflowErrors());
  }

}
