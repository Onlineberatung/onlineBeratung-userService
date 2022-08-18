package de.caritas.cob.userservice.api.workflow.delete.action.consultant;

import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.CONSULTANT;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.workflow.delete.action.DeleteRocketChatUserAction;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
import lombok.NonNull;
import org.springframework.stereotype.Component;

/** Action to delete a rocket chat user. */
@Component
public class DeleteRocketChatConsultantAction extends DeleteRocketChatUserAction
    implements ActionCommand<ConsultantDeletionWorkflowDTO> {

  public DeleteRocketChatConsultantAction(@NonNull RocketChatService rocketChatService) {
    super(rocketChatService);
  }

  /**
   * Deletes the given {@link ConsultantDeletionWorkflowDTO}s consultant related Rocket.Chat
   * account.
   *
   * @param actionTarget the {@link ConsultantDeletionWorkflowDTO}
   */
  @Override
  public void execute(ConsultantDeletionWorkflowDTO actionTarget) {
    try {
      deleteUserInRocketChat(actionTarget.getConsultant().getRocketChatId());
    } catch (Exception e) {
      appendErrorsForSourceType(
          actionTarget.getDeletionWorkflowErrors(),
          CONSULTANT,
          actionTarget.getConsultant().getRocketChatId(),
          e);
    }
  }
}
