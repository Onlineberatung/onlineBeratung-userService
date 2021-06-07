package de.caritas.cob.userservice.api.deleteworkflow.action.asker;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.deleteworkflow.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Action to delete a {@link User} in database.
 */
@Component
@RequiredArgsConstructor
public class DeleteDatabaseAskerAction implements ActionCommand<AskerDeletionWorkflowDTO> {

  private final @NonNull UserRepository userRepository;

  /**
   * Deletes the given {@link User} in database.
   *
   * @param actionTarget the {@link AskerDeletionWorkflowDTO} with the {@link User} to delete
   */
  @Override
  public void execute(AskerDeletionWorkflowDTO actionTarget) {
    try {
      this.userRepository.delete(actionTarget.getUser());
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      actionTarget.getDeletionWorkflowErrors().add(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(DATABASE)
              .identifier(actionTarget.getUser().getUserId())
              .reason("Unable to delete user")
              .timestamp(nowInUtc())
              .build()
      );
    }
  }
}
