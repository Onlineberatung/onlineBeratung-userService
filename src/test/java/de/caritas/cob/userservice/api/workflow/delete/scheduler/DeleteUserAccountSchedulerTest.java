package de.caritas.cob.userservice.api.workflow.delete.scheduler;

import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.delete.service.DeleteUserAccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUserAccountSchedulerTest {

  @InjectMocks private DeleteUserAccountScheduler deleteUserAccountScheduler;

  @Mock private DeleteUserAccountService deleteUserAccountService;

  @Mock private TenantContextProvider tenantContextProvider;

  @Test
  public void performDeletionWorkflow_Should_executeDeleteUserAccounts() {
    this.deleteUserAccountScheduler.performDeletionWorkflow();

    verify(tenantContextProvider).setTechnicalContextIfMultiTenancyIsEnabled();
    verify(this.deleteUserAccountService).deleteUserAccounts();
  }
}
