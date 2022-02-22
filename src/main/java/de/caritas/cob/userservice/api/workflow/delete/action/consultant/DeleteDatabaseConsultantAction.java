package de.caritas.cob.userservice.api.workflow.delete.action.consultant;

import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Deletes a {@link Consultant} in database.
 */
@Slf4j
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
      log.error("UserService delete workflow error: ", e);
      actionTarget.getDeletionWorkflowErrors().add(
          DeletionWorkflowError.builder()
              .deletionSourceType(CONSULTANT)
              .deletionTargetType(DeletionTargetType.DATABASE)
              .identifier(actionTarget.getConsultant().getId())
              .reason("Unable to delete consultant in database")
              .timestamp(nowInUtc())
              .build()
      );
    }
  }

}
