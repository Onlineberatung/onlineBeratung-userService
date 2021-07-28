package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_HEADER_AUTH_TOKEN;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteInactiveSessionsAndUserService;
import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteUsersRegisteredOnlyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUsersRegisteredOnlySchedulerTest {

  private final String FIELD_NAME_USER_REGISTERED_ONLY_DELETE_WORKFLOW_ENABLED = "userRegisteredOnlyDeleteWorkflowEnabled";

  @InjectMocks
  DeleteUsersRegisteredOnlyScheduler deleteUsersRegisteredOnlyScheduler;

  @Mock
  DeleteUsersRegisteredOnlyService deleteUsersRegisteredOnlyService;

  @Test
  public void performDeletionWorkflow_Should_executeDeleteInactiveSessionsAndUsers_WhenFeatureIsEnabled() {

    setField(deleteUsersRegisteredOnlyScheduler, FIELD_NAME_USER_REGISTERED_ONLY_DELETE_WORKFLOW_ENABLED,
        true);
    deleteUsersRegisteredOnlyScheduler.performDeletionWorkflow();
    verify(this.deleteUsersRegisteredOnlyService, times(1)).deleteUserAccounts();
  }

  @Test
  public void performDeletionWorkflow_ShouldNot_executeDeleteInactiveSessionsAndUsers_WhenFeatureIsDisabled() {

    setField(deleteUsersRegisteredOnlyScheduler, FIELD_NAME_USER_REGISTERED_ONLY_DELETE_WORKFLOW_ENABLED,
        false);
    deleteUsersRegisteredOnlyScheduler.performDeletionWorkflow();
    verify(this.deleteUsersRegisteredOnlyService, never()).deleteUserAccounts();
  }

}
