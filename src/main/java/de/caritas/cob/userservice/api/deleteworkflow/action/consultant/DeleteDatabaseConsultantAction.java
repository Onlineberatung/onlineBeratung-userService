package de.caritas.cob.userservice.api.deleteworkflow.action.consultant;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.deleteworkflow.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.service.LogService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deletes a {@link Consultant} in database.
 */
@Component
@RequiredArgsConstructor
public class DeleteDatabaseConsultantAction implements
    ActionCommand<ConsultantDeletionWorkflowDTO> {

  private final @NonNull ConsultantRepository consultantRepository;

  /**
   * Deletes the given {@link Consultant} in database.
   *
   * @param actionTarget the {@link ConsultantDeletionWorkflowDTO} with the {@link Consultant} to
   *                     delete
   */
  @Override
  public void execute(ConsultantDeletionWorkflowDTO actionTarget) {
    try {
      this.consultantRepository.delete(actionTarget.getConsultant());
    } catch (Exception e) {
      LogService.logDeleteWorkflowError(e);
      actionTarget.getDeletionWorkflowErrors().add(
          DeletionWorkflowError.builder()
              .deletionSourceType(CONSULTANT)
              .deletionTargetType(DATABASE)
              .identifier(actionTarget.getConsultant().getId())
              .reason("Unable to delete consultant in database")
              .timestamp(nowInUtc())
              .build()
      );
    }
  }

}
