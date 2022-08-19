package de.caritas.cob.userservice.api.workflow.delete.action.consultant;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.CONSULTANT;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.workflow.delete.action.DeleteKeycloakUserAction;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Action to delete a user account in keycloak. */
@Slf4j
@Component
public class DeleteKeycloakConsultantAction extends DeleteKeycloakUserAction
    implements ActionCommand<ConsultantDeletionWorkflowDTO> {

  public DeleteKeycloakConsultantAction(@NonNull IdentityClient identityClient) {
    super(identityClient);
  }

  /**
   * Deletes the given {@link ConsultantDeletionWorkflowDTO}s user in keycloak.
   *
   * @param actionTarget the {@link ConsultantDeletionWorkflowDTO} to delete
   */
  @Override
  public void execute(ConsultantDeletionWorkflowDTO actionTarget) {
    try {
      this.deleteUserWithId(actionTarget.getConsultant().getId());
    } catch (Exception e) {
      log.error("UserService delete workflow error: ", e);
      actionTarget
          .getDeletionWorkflowErrors()
          .add(
              DeletionWorkflowError.builder()
                  .deletionSourceType(CONSULTANT)
                  .deletionTargetType(DeletionTargetType.KEYCLOAK)
                  .identifier(actionTarget.getConsultant().getId())
                  .reason(ERROR_REASON)
                  .timestamp(nowInUtc())
                  .build());
    }
  }
}
