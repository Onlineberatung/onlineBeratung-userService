package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteUserAccountService;
import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for deletion of users with delete flag.
 */
@Component
@RequiredArgsConstructor
public class DeleteUserAccountScheduler {

  private final @NonNull DeleteUserAccountService deleteUserAccountService;
  private final @NonNull TenantContextProvider tenantContextProvider;

  /**
   * Entry method to perform deletion workflow.
   */
  @Scheduled(cron = "${user.account.deleteworkflow.cron}")
  public void performDeletionWorkflow() {
    tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();
    this.deleteUserAccountService.deleteUserAccounts();
  }

}
