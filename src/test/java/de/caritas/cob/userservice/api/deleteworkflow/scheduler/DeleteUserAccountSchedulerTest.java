package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteUserAccountService;
import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteUserAccountSchedulerTest {

  @InjectMocks
  private DeleteUserAccountScheduler deleteUserAccountScheduler;

  @Mock
  private DeleteUserAccountService deleteUserAccountService;

  @Mock
  private TenantContextProvider tenantContextProvider;

  @Test
  public void performDeletionWorkflow_Should_executeDeleteUserAccounts() {
    this.deleteUserAccountScheduler.performDeletionWorkflow();

    verify(tenantContextProvider).setTechnicalContextIfMultiTenancyIsEnabled();
    verify(this.deleteUserAccountService).deleteUserAccounts();
  }

}