package de.caritas.cob.userservice.api.workflow.delete.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.workflow.delete.service.DeleteUsersRegisteredOnlyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUsersRegisteredOnlySchedulerTest {

  @InjectMocks
  DeleteUsersRegisteredOnlyScheduler deleteUsersRegisteredOnlyScheduler;

  @Mock
  DeleteUsersRegisteredOnlyService deleteUsersRegisteredOnlyService;

  @Test
  public void performDeletionWorkflow_Should_executeDeleteUserAccountsTimeSensitive_WhenFeatureIsEnabled() {
    setField(deleteUsersRegisteredOnlyScheduler, "userRegisteredOnlyDeleteWorkflowEnabled", true);
    deleteUsersRegisteredOnlyScheduler.performDeletionWorkflow();
    verify(deleteUsersRegisteredOnlyService).deleteUserAccountsTimeSensitive();
  }

  @Test
  public void performDeletionWorkflow_ShouldNot_executeDeleteUserAccountsTimeSensitive_WhenFeatureIsDisabled() {
    setField(deleteUsersRegisteredOnlyScheduler, "userRegisteredOnlyDeleteWorkflowEnabled", false);
    deleteUsersRegisteredOnlyScheduler.performDeletionWorkflow();
    verify(deleteUsersRegisteredOnlyService, never()).deleteUserAccountsTimeSensitive();
  }

  @Test
  public void performDeletionWorkflow_Should_executeDeleteUserAccountsTimeInsensitive_WhenFeatureIsEnabled() {
    setField(deleteUsersRegisteredOnlyScheduler,
        "userRegisteredOnlyDeleteWorkflowAfterSessionPurgeEnabled", true);
    deleteUsersRegisteredOnlyScheduler.performDeletionWorkflow();
    verify(deleteUsersRegisteredOnlyService).deleteUserAccountsTimeInsensitive();
  }

  @Test
  public void performDeletionWorkflow_ShouldNot_executeDeleteUserAccountsTimeInsensitive_WhenFeatureIsDisabled() {
    setField(deleteUsersRegisteredOnlyScheduler,
        "userRegisteredOnlyDeleteWorkflowAfterSessionPurgeEnabled", false);
    deleteUsersRegisteredOnlyScheduler.performDeletionWorkflow();
    verify(deleteUsersRegisteredOnlyService, never()).deleteUserAccountsTimeInsensitive();
  }
}
