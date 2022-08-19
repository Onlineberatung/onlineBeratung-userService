package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.workflow.delete.action.DeleteRocketChatUserAction;
import de.caritas.cob.userservice.api.workflow.delete.model.AskerDeletionWorkflowDTO;
import lombok.NonNull;
import org.springframework.stereotype.Component;

/** Action to delete a rocket chat user. */
@Component
public class DeleteRocketChatAskerAction extends DeleteRocketChatUserAction
    implements ActionCommand<AskerDeletionWorkflowDTO> {

  public DeleteRocketChatAskerAction(@NonNull RocketChatService rocketChatService) {
    super(rocketChatService);
  }

  /**
   * Deletes the given {@link AskerDeletionWorkflowDTO}s user related Rocket.Chat account.
   *
   * @param actionTarget the {@link AskerDeletionWorkflowDTO}
   */
  @Override
  public void execute(AskerDeletionWorkflowDTO actionTarget) {
    try {
      deleteUserInRocketChat(actionTarget.getUser().getRcUserId());
    } catch (Exception e) {
      appendErrorsForSourceType(
          actionTarget.getDeletionWorkflowErrors(), ASKER, actionTarget.getUser().getRcUserId(), e);
    }
  }
}
