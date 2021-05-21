package de.caritas.cob.userservice.api.deactivateworkflow.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.deactivateworkflow.service.DeactivateAnonymousUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeactivateAnonymousUserSchedulerTest {

  @InjectMocks
  private DeactivateAnonymousUserScheduler deactivateAnonymousUserScheduler;

  @Mock
  private DeactivateAnonymousUserService deactivateAnonymousUserService;

  @Test
  public void performDeactivationWorkflow_Should_useService() {
    this.deactivateAnonymousUserScheduler.performDeactivationWorkflow();

    verify(this.deactivateAnonymousUserService, times(1)).deactivateStaleAnonymousUsers();
  }
}
