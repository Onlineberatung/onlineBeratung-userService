package de.caritas.cob.userservice.api.workflow.delete.scheduler;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.delete.service.DeleteUsersRegisteredOnlyService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Scheduler for deletion of only registered users without sessions. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteUsersRegisteredOnlyScheduler {

  private final @NonNull DeleteUsersRegisteredOnlyService deleteUsersRegisteredOnlyService;
  private final @NonNull TenantContextProvider tenantContextProvider;

  @Value("${user.registeredonly.deleteWorkflow.enabled}")
  private boolean userRegisteredOnlyDeleteWorkflowEnabled;

  @Value("${user.registeredonly.deleteWorkflow.afterSessionPurge.enabled}")
  private boolean userRegisteredOnlyDeleteWorkflowAfterSessionPurgeEnabled;

  /** Entry method to perform deletion workflow. */
  @Scheduled(cron = "${user.registeredonly.deleteWorkflow.cron}")
  public void performDeletionWorkflow() {
    try {
      log.info("Started deleting registered users without sessions");
      tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();
      if (userRegisteredOnlyDeleteWorkflowEnabled) {
        deleteUsersRegisteredOnlyService.deleteUserAccountsTimeSensitive();
      }

      if (userRegisteredOnlyDeleteWorkflowAfterSessionPurgeEnabled) {
        deleteUsersRegisteredOnlyService.deleteUserAccountsTimeInsensitive();
      }
    } finally {
      log.info("Completed deleting registered users without sessions");
    }
  }
}
