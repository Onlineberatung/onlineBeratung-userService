package de.caritas.cob.userservice.api.deleteworkflow.action.consultant;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.KEYCLOAK;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.deleteworkflow.action.DeleteKeycloakUserAction;
import de.caritas.cob.userservice.api.deleteworkflow.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import lombok.NonNull;
import org.springframework.stereotype.Component;

/**
 * Action to delete a user account in keycloak.
 */
@Component
public class DeleteKeycloakConsultantAction extends DeleteKeycloakUserAction implements
    ActionCommand<ConsultantDeletionWorkflowDTO> {

  public DeleteKeycloakConsultantAction(
      @NonNull KeycloakAdminClientService keycloakAdminClientService) {
    super(keycloakAdminClientService);
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
      LogService.logDeleteWorkflowError(e);
      actionTarget.getDeletionWorkflowErrors().add(
          DeletionWorkflowError.builder()
              .deletionSourceType(CONSULTANT)
              .deletionTargetType(KEYCLOAK)
              .identifier(actionTarget.getConsultant().getId())
              .reason(ERROR_REASON)
              .timestamp(nowInUtc())
              .build()
      );
    }
  }
}
