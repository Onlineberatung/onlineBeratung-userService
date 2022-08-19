package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.conversation.service.user.anonymous.AnonymousUsernameRegistry;
import de.caritas.cob.userservice.api.workflow.delete.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Deletes a registry id by username. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAnonymousRegistryIdAction implements ActionCommand<AskerDeletionWorkflowDTO> {

  private final @NonNull AnonymousUsernameRegistry anonymousUsernameRegistry;

  /**
   * Deletes a registry id by username.
   *
   * @param actionTarget the {@link AskerDeletionWorkflowDTO}
   */
  @Override
  public void execute(AskerDeletionWorkflowDTO actionTarget) {
    try {
      anonymousUsernameRegistry.removeRegistryIdByUsername(actionTarget.getUser().getUsername());
    } catch (Exception e) {
      log.error("UserService delete workflow error: ", e);
      actionTarget
          .getDeletionWorkflowErrors()
          .add(
              DeletionWorkflowError.builder()
                  .deletionSourceType(ASKER)
                  .deletionTargetType(DeletionTargetType.ANONYMOUS_REGISTRY_IDS)
                  .identifier(actionTarget.getUser().getUserId())
                  .reason("Could not delete registry id for anonymous users by username")
                  .timestamp(nowInUtc())
                  .build());
    }
  }
}
