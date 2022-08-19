package de.caritas.cob.userservice.api.workflow.deactivate.scheduler;

import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.deactivate.service.DeactivateAnonymousUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeactivateAnonymousUserSchedulerTest {

  @InjectMocks private DeactivateAnonymousUserScheduler deactivateAnonymousUserScheduler;

  @Mock private DeactivateAnonymousUserService deactivateAnonymousUserService;

  @Mock private TenantContextProvider tenantContextProvider;

  @Test
  void performDeactivationWorkflow_Should_useService() {
    this.deactivateAnonymousUserScheduler.performDeactivationWorkflow();

    verify(tenantContextProvider).setTechnicalContextIfMultiTenancyIsEnabled();
    verify(this.deactivateAnonymousUserService).deactivateStaleAnonymousUsers();
  }
}
