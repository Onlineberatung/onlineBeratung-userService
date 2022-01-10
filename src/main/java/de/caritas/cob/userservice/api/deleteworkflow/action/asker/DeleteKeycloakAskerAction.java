package de.caritas.cob.userservice.api.deleteworkflow.action.asker;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.KEYCLOAK;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.deleteworkflow.action.DeleteKeycloakUserAction;
import de.caritas.cob.userservice.api.deleteworkflow.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Action to delete a user account in keycloak.
 */
@Slf4j
@Component
public class DeleteKeycloakAskerAction extends DeleteKeycloakUserAction implements
    ActionCommand<AskerDeletionWorkflowDTO> {

  public DeleteKeycloakAskerAction(@NonNull KeycloakAdminClientService keycloakAdminClientService) {
    super(keycloakAdminClientService);
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
      actionTarget.getDeletionWorkflowErrors().add(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(KEYCLOAK)
              .identifier(actionTarget.getUser().getUserId())
              .reason(ERROR_REASON)
              .timestamp(nowInUtc())
              .build()
      );
    }
  }
}
