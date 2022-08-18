package de.caritas.cob.userservice.api.workflow.delete.action.consultant;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.CONSULTANT;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Deletes a {@link ConsultantAgency} in database. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteDatabaseConsultantAgencyAction
    implements ActionCommand<ConsultantDeletionWorkflowDTO> {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;

  /**
   * Deletes all {@link Consultant} regarding {@link ConsultantAgency} relations.
   *
   * @param actionTarget the {@link ConsultantDeletionWorkflowDTO} containing the {@link Consultant}
   */
  @Override
  public void execute(ConsultantDeletionWorkflowDTO actionTarget) {
    try {
      var consultantAgencies =
          this.consultantAgencyRepository.findByConsultantId(actionTarget.getConsultant().getId());
      this.consultantAgencyRepository.deleteAll(consultantAgencies);
    } catch (Exception e) {
      log.error("UserService delete workflow error: ", e);
      actionTarget
          .getDeletionWorkflowErrors()
          .add(
              DeletionWorkflowError.builder()
                  .deletionSourceType(CONSULTANT)
                  .deletionTargetType(DeletionTargetType.DATABASE)
                  .identifier(actionTarget.getConsultant().getId())
                  .reason("Could not delete consultant agency relations")
                  .timestamp(nowInUtc())
                  .build());
    }
  }
}
