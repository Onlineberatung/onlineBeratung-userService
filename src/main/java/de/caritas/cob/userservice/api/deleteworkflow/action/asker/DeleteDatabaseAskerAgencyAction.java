package de.caritas.cob.userservice.api.deleteworkflow.action.asker;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.deleteworkflow.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.useragency.UserAgency;
import de.caritas.cob.userservice.api.repository.useragency.UserAgencyRepository;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deletes a {@link UserAgency} in database.
 */
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
      LogService.logDeleteWorkflowError(e);
      actionTarget.getDeletionWorkflowErrors().add(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(DATABASE)
              .identifier(actionTarget.getUser().getUserId())
              .reason("Could not delete user agency relations")
              .timestamp(nowInUtc())
              .build()
      );
    }
  }
}
