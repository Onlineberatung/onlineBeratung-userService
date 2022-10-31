package de.caritas.cob.userservice.api.workflow.delete.action.consultant;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.CONSULTANT;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import de.caritas.cob.userservice.api.workflow.delete.model.ConsultantDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAppointmentServiceConsultantAction
    implements ActionCommand<ConsultantDeletionWorkflowDTO> {

  private final @NonNull AppointmentService appointmentService;

  @Override
  public void execute(ConsultantDeletionWorkflowDTO actionTarget) {
    try {
      this.appointmentService.deleteConsultant(actionTarget.getConsultant().getId());
    } catch (Exception e) {
      log.error("Appointment service delete workflow error: ", e);
      actionTarget
          .getDeletionWorkflowErrors()
          .add(
              DeletionWorkflowError.builder()
                  .deletionSourceType(CONSULTANT)
                  .deletionTargetType(DeletionTargetType.APPOINTMENT_SERVICE)
                  .identifier(actionTarget.getConsultant().getId())
                  .reason("Unable to delete consultant")
                  .timestamp(nowInUtc())
                  .build());
    }
  }
}
