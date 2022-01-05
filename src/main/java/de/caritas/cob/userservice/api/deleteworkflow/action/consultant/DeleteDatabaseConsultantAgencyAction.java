package de.caritas.cob.userservice.api.deleteworkflow.action.consultant;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.CONSULTANT;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.DATABASE;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.deleteworkflow.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Deletes a {@link ConsultantAgency} in database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteDatabaseConsultantAgencyAction implements
    ActionCommand<ConsultantDeletionWorkflowDTO> {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;

  /**
   * Deletes all {@link Consultant} regarding {@link ConsultantAgency} relations.
   *
   * @param actionTarget the {@link ConsultantDeletionWorkflowDTO} containing the {@link
   *                     Consultant}
   */
  @Override
  public void execute(ConsultantDeletionWorkflowDTO actionTarget) {
    try {
      var consultantAgencies = this.consultantAgencyRepository
          .findByConsultantId(actionTarget.getConsultant().getId());
      this.consultantAgencyRepository.deleteAll(consultantAgencies);
    } catch (Exception e) {
      log.error("UserService delete workflow error: ", e);
      actionTarget.getDeletionWorkflowErrors().add(
          DeletionWorkflowError.builder()
              .deletionSourceType(CONSULTANT)
              .deletionTargetType(DATABASE)
              .identifier(actionTarget.getConsultant().getId())
              .reason("Could not delete consultant agency relations")
              .timestamp(nowInUtc())
              .build()
      );
    }
  }

}
