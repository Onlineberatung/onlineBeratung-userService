package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import de.caritas.cob.userservice.api.workflow.delete.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAppointmentServiceAskerAction
    implements ActionCommand<AskerDeletionWorkflowDTO> {

  private final @NonNull AppointmentService appointmentService;

  @Override
  public void execute(AskerDeletionWorkflowDTO actionTarget) {
    try {
      this.appointmentService.deleteAsker(actionTarget.getUser().getUserId());
    } catch (Exception e) {
      log.error("Appointment service delete workflow error: ", e);
      actionTarget
          .getDeletionWorkflowErrors()
          .add(
              DeletionWorkflowError.builder()
                  .deletionSourceType(ASKER)
                  .deletionTargetType(DeletionTargetType.APPOINTMENT_SERVICE)
                  .identifier(actionTarget.getUser().getUserId())
                  .reason("Unable to delete asker")
                  .timestamp(nowInUtc())
                  .build());
    }
  }
}
