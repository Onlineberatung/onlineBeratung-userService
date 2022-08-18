package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Deletes a {@link UserAgency} in database. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteDatabaseAskerAgencyAction implements ActionCommand<AskerDeletionWorkflowDTO> {

  private final @NonNull UserAgencyRepository userAgencyRepository;

  /**
   * Deletes all {@link User} regarding {@link UserAgency} relations.
   *
   * @param actionTarget the {@link AskerDeletionWorkflowDTO} containing the user
   */
  @Override
  public void execute(AskerDeletionWorkflowDTO actionTarget) {
    try {
      var userAgencies = this.userAgencyRepository.findByUser(actionTarget.getUser());
      this.userAgencyRepository.deleteAll(userAgencies);
    } catch (Exception e) {
      log.error("UserService delete workflow error: ", e);
      actionTarget
          .getDeletionWorkflowErrors()
          .add(
              DeletionWorkflowError.builder()
                  .deletionSourceType(ASKER)
                  .deletionTargetType(DeletionTargetType.DATABASE)
                  .identifier(actionTarget.getUser().getUserId())
                  .reason("Could not delete user agency relations")
                  .timestamp(nowInUtc())
                  .build());
    }
  }
}
