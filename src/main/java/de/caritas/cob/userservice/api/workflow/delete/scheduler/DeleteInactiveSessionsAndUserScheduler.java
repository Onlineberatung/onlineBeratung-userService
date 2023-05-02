package de.caritas.cob.userservice.api.workflow.delete.scheduler;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.delete.service.DeleteInactiveSessionsAndUserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Scheduler for deletion of inactive sessions and user. */
@Component
@RequiredArgsConstructor
public class DeleteInactiveSessionsAndUserScheduler {

  private final @NonNull DeleteInactiveSessionsAndUserService deleteInactiveSessionsAndUserService;
  private final @NonNull TenantContextProvider tenantContextProvider;

  @Value("${session.inactive.deleteWorkflow.enabled}")
  private boolean sessionInactiveDeleteWorkflowEnabled;

  /** Entry method to perform deletion workflow. */
  @Scheduled(cron = "${session.inactive.deleteWorkflow.cron}")
  public void performDeletionWorkflow() {
    tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();
    if (sessionInactiveDeleteWorkflowEnabled) {
      this.deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();
    }
  }
}
