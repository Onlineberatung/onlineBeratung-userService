package de.caritas.cob.userservice.api.workflow.delete.scheduler;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.delete.service.CloseInactiveRocketchatSessionsService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Scheduler for deletion of inactive sessions and user. */
@Component
@RequiredArgsConstructor
@Slf4j
public class CloseInactiveRocketchatSessionsScheduler {

  private final @NonNull CloseInactiveRocketchatSessionsService
      closeInactiveRocketchatSessionsService;
  private final @NonNull TenantContextProvider tenantContextProvider;

  @Value("${rocketchat.session.inactive.closeWorkflow.enabled}")
  private boolean rocketchatSessionInactiveCloseWorkflowEnabled;

  /** Entry method to perform deletion workflow. */
  @Scheduled(cron = "${rocketchat.session.inactive.closeWorkflow.cron}")
  public void performDeletionWorkflow() {
    tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();
    if (rocketchatSessionInactiveCloseWorkflowEnabled) {
      log.info("Performing deletion workflow for inactive rocketchat sessions.");
      this.closeInactiveRocketchatSessionsService.closeInactiveRocketchatSessions();
    }
  }
}
