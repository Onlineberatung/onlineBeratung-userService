package de.caritas.cob.userservice.api.workflow.deactivate.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.workflow.deactivate.scheduler.DeactivateAnonymousUserScheduler;
import de.caritas.cob.userservice.api.workflow.deactivate.service.DeactivateAnonymousUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeactivateAnonymousUserSchedulerTest {

  @InjectMocks
  private DeactivateAnonymousUserScheduler deactivateAnonymousUserScheduler;

  @Mock
  private DeactivateAnonymousUserService deactivateAnonymousUserService;

  @Test
  void performDeactivationWorkflow_Should_useService() {
    this.deactivateAnonymousUserScheduler.performDeactivationWorkflow();

    verify(this.deactivateAnonymousUserService, times(1)).deactivateStaleAnonymousUsers();
  }
}
