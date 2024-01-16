package de.caritas.cob.userservice.api.workflow.delete.scheduler;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.delete.service.CloseInactiveRocketchatSessionsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CloseInactiveRocketchatSessionsSchedulerTest {

  @Mock CloseInactiveRocketchatSessionsService closeInactiveRocketchatSessionsService;
  @Mock TenantContextProvider tenantContextProvider;

  @Test
  void performDeletionWorkflow_Should_CallDeleteInactiveRocketchatSessionsService() {
    // given
    CloseInactiveRocketchatSessionsScheduler deleteInactiveRocketchatSessionsScheduler =
        new CloseInactiveRocketchatSessionsScheduler(
            closeInactiveRocketchatSessionsService, tenantContextProvider);
    ReflectionTestUtils.setField(
        deleteInactiveRocketchatSessionsScheduler,
        "rocketchatSessionInactiveDeleteWorkflowEnabled",
        true);
    // when
    deleteInactiveRocketchatSessionsScheduler.performDeletionWorkflow();
    // then
    verify(closeInactiveRocketchatSessionsService).closeInactiveRocketchatSessions();
  }

  @Test
  void
      performDeletionWorkflow_Should_NotCallDeleteInactiveRocketchatSessionsService_If_WorkflowNotEnabled() {
    // given
    CloseInactiveRocketchatSessionsScheduler closeInactiveRocketchatSessionsScheduler =
        new CloseInactiveRocketchatSessionsScheduler(
            closeInactiveRocketchatSessionsService, tenantContextProvider);
    ReflectionTestUtils.setField(
        closeInactiveRocketchatSessionsScheduler,
        "rocketchatSessionInactiveDeleteWorkflowEnabled",
        false);
    // when
    closeInactiveRocketchatSessionsScheduler.performDeletionWorkflow();
    // then
    verify(closeInactiveRocketchatSessionsService, never()).closeInactiveRocketchatSessions();
  }
}
