package de.caritas.cob.userservice.api.workflow.delete.action.asker;

import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType.APPOINTMENT_SERVICE;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import de.caritas.cob.userservice.api.workflow.delete.model.AskerDeletionWorkflowDTO;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class DeleteAppointmentServiceAskerActionTest {

  @InjectMocks private DeleteAppointmentServiceAskerAction deleteAppointmentServiceAskerAction;

  @Mock private AppointmentService appointmentService;

  @Mock private Logger logger;

  @BeforeEach
  public void setup() {
    setInternalState(DeleteAppointmentServiceAskerAction.class, "log", logger);
  }

  @Test
  public void execute_Should_returnEmptyList_When_deletionOfAppointmentDataIsSuccessful() {
    AskerDeletionWorkflowDTO workflowDTO = new AskerDeletionWorkflowDTO(new User(), emptyList());

    this.deleteAppointmentServiceAskerAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(0));
    verify(this.appointmentService, times(1)).deleteAsker(any());
    verifyNoMoreInteractions(this.logger);
  }

  @Test
  public void
      execute_Should_returnExpectedWorkflowErrorAndLogError_When_deletionOfAppointmentDataFails() {
    doThrow(new RuntimeException()).when(this.appointmentService).deleteAsker(any());
    User user = new User();
    user.setUserId("user id");
    AskerDeletionWorkflowDTO workflowDTO = new AskerDeletionWorkflowDTO(user, new ArrayList<>());

    this.deleteAppointmentServiceAskerAction.execute(workflowDTO);
    List<DeletionWorkflowError> workflowErrors = workflowDTO.getDeletionWorkflowErrors();

    assertThat(workflowErrors, hasSize(1));
    assertThat(workflowErrors.get(0).getDeletionSourceType(), is(ASKER));
    assertThat(workflowErrors.get(0).getDeletionTargetType(), is(APPOINTMENT_SERVICE));
    assertThat(workflowErrors.get(0).getIdentifier(), is("user id"));
    assertThat(workflowErrors.get(0).getReason(), is("Unable to delete asker"));
    assertThat(workflowErrors.get(0).getTimestamp(), notNullValue());
    verify(logger).error(anyString(), any(RuntimeException.class));
  }
}
