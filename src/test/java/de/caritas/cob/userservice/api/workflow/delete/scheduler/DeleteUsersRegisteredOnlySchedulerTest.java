package de.caritas.cob.userservice.api.workflow.delete.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.delete.service.DeleteUsersRegisteredOnlyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteUsersRegisteredOnlySchedulerTest {

  @InjectMocks DeleteUsersRegisteredOnlyScheduler deleteUsersRegisteredOnlyScheduler;

  @Mock DeleteUsersRegisteredOnlyService deleteUsersRegisteredOnlyService;

  @Mock TenantContextProvider tenantContextProvider;

  @Test
  public void
      performDeletionWorkflow_Should_executeDeleteUserAccountsTimeSensitive_WhenFeatureIsEnabled() {
    setField(deleteUsersRegisteredOnlyScheduler, "userRegisteredOnlyDeleteWorkflowEnabled", true);
    deleteUsersRegisteredOnlyScheduler.performDeletionWorkflow();

    verify(tenantContextProvider).setTechnicalContextIfMultiTenancyIsEnabled();
    verify(deleteUsersRegisteredOnlyService).deleteUserAccountsTimeSensitive();
  }

  @Test
  public void
      performDeletionWorkflow_ShouldNot_executeDeleteUserAccountsTimeSensitive_WhenFeatureIsDisabled() {
    setField(deleteUsersRegisteredOnlyScheduler, "userRegisteredOnlyDeleteWorkflowEnabled", false);
    deleteUsersRegisteredOnlyScheduler.performDeletionWorkflow();

    verify(tenantContextProvider).setTechnicalContextIfMultiTenancyIsEnabled();
    verify(deleteUsersRegisteredOnlyService, never()).deleteUserAccountsTimeSensitive();
  }

  @Test
  public void
      performDeletionWorkflow_Should_executeDeleteUserAccountsTimeInsensitive_WhenFeatureIsEnabled() {
    setField(
        deleteUsersRegisteredOnlyScheduler,
        "userRegisteredOnlyDeleteWorkflowAfterSessionPurgeEnabled",
        true);
    deleteUsersRegisteredOnlyScheduler.performDeletionWorkflow();

    verify(tenantContextProvider).setTechnicalContextIfMultiTenancyIsEnabled();
    verify(deleteUsersRegisteredOnlyService).deleteUserAccountsTimeInsensitive();
  }

  @Test
  public void
      performDeletionWorkflow_ShouldNot_executeDeleteUserAccountsTimeInsensitive_WhenFeatureIsDisabled() {
    setField(
        deleteUsersRegisteredOnlyScheduler,
        "userRegisteredOnlyDeleteWorkflowAfterSessionPurgeEnabled",
        false);
    deleteUsersRegisteredOnlyScheduler.performDeletionWorkflow();

    verify(tenantContextProvider).setTechnicalContextIfMultiTenancyIsEnabled();
    verify(deleteUsersRegisteredOnlyService, never()).deleteUserAccountsTimeInsensitive();
  }
}
