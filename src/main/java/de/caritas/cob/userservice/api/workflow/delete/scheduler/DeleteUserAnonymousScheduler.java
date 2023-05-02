package de.caritas.cob.userservice.api.workflow.delete.scheduler;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.delete.service.DeleteUserAnonymousService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Scheduler for deletion of anonymous users. */
@Component
@RequiredArgsConstructor
public class DeleteUserAnonymousScheduler {

  private final @NonNull DeleteUserAnonymousService deleteUserAnonymousService;
  private final @NonNull TenantContextProvider tenantContextProvider;

  /** Entry method to perform deletion workflow. */
  @Scheduled(cron = "${user.anonymous.deleteworkflow.cron}")
  public void performDeletionWorkflow() {
    tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();
    deleteUserAnonymousService.deleteInactiveAnonymousUsers();
  }
}
