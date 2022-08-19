package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.workflow.delete.action.DeleteKeycloakUserAction;
import de.caritas.cob.userservice.api.workflow.delete.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Action to delete a user account in keycloak. */
@Slf4j
@Component
public class DeleteKeycloakAskerAction extends DeleteKeycloakUserAction
    implements ActionCommand<AskerDeletionWorkflowDTO> {

  public DeleteKeycloakAskerAction(@NonNull IdentityClient identityClient) {
    super(identityClient);
  }

  /**
   * Deletes the given {@link AskerDeletionWorkflowDTO}s user in keycloak.
   *
   * @param actionTarget the {@link AskerDeletionWorkflowDTO} to delete
   */
  @Override
  public void execute(AskerDeletionWorkflowDTO actionTarget) {
    try {
      this.deleteUserWithId(actionTarget.getUser().getUserId());
    } catch (Exception e) {
      log.error("UserService delete workflow error: ", e);
      actionTarget
          .getDeletionWorkflowErrors()
          .add(
              DeletionWorkflowError.builder()
                  .deletionSourceType(ASKER)
                  .deletionTargetType(DeletionTargetType.KEYCLOAK)
                  .identifier(actionTarget.getUser().getUserId())
                  .reason(ERROR_REASON)
                  .timestamp(nowInUtc())
                  .build());
    }
  }
}
