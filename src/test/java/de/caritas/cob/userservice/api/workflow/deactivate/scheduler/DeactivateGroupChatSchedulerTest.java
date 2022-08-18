package de.caritas.cob.userservice.api.workflow.deactivate.scheduler;

import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.deactivate.service.DeactivateGroupChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeactivateGroupChatSchedulerTest {

  @InjectMocks private DeactivateGroupChatScheduler deactivateGroupChatScheduler;

  @Mock private DeactivateGroupChatService deactivateGroupChatService;

  @Mock private TenantContextProvider tenantContextProvider;

  @Test
  void performDeactivationWorkflow_Should_useService() {
    this.deactivateGroupChatScheduler.performDeactivationWorkflow();

    verify(tenantContextProvider).setTechnicalContextIfMultiTenancyIsEnabled();
    verify(this.deactivateGroupChatService).deactivateStaleGroupChats();
  }
}
