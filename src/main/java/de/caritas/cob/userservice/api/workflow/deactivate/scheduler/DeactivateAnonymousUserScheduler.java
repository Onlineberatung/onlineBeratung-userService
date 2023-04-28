package de.caritas.cob.userservice.api.workflow.deactivate.scheduler;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.deactivate.service.DeactivateAnonymousUserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeactivateAnonymousUserScheduler {

  private final @NonNull DeactivateAnonymousUserService deactivateAnonymousUserService;
  private final @NonNull TenantContextProvider tenantContextProvider;

  @Scheduled(cron = "${user.anonymous.deactivateworkflow.cron}")
  public void performDeactivationWorkflow() {
    try {
      log.info("Started deactivating stale anonymous users");
      tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();
      deactivateAnonymousUserService.deactivateStaleAnonymousUsers();
    } finally {
      log.info("Completed deactivating stale anonymous users");
    }
  }
}
