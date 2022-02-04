package de.caritas.cob.userservice.api.workflow.delete.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.workflow.delete.service.DeleteInactiveSessionsAndUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteInactiveSessionsAndUserSchedulerTest {

  private final String FIELD_NAME_SESSION_INACTIVE_DELETE_WORKFLOW_ENABLED = "sessionInactiveDeleteWorkflowEnabled";

  @InjectMocks
  DeleteInactiveSessionsAndUserScheduler deleteInactiveSessionsAndUserScheduler;

  @Mock
  DeleteInactiveSessionsAndUserService deleteInactiveSessionsAndUserService;

  @Test
  public void performDeletionWorkflow_Should_executeDeleteInactiveSessionsAndUsers_WhenFeatureIsEnabled() {

    setField(deleteInactiveSessionsAndUserScheduler,
        FIELD_NAME_SESSION_INACTIVE_DELETE_WORKFLOW_ENABLED,
        true);
    deleteInactiveSessionsAndUserScheduler.performDeletionWorkflow();
    verify(this.deleteInactiveSessionsAndUserService, times(1)).deleteInactiveSessionsAndUsers();
  }

  @Test
  public void performDeletionWorkflow_ShouldNot_executeDeleteInactiveSessionsAndUsers_WhenFeatureIsDisabled() {

    setField(deleteInactiveSessionsAndUserScheduler,
        FIELD_NAME_SESSION_INACTIVE_DELETE_WORKFLOW_ENABLED,
        false);
    deleteInactiveSessionsAndUserScheduler.performDeletionWorkflow();
    verify(this.deleteInactiveSessionsAndUserService, never()).deleteInactiveSessionsAndUsers();
  }

}
