package de.caritas.cob.userservice.api.workflow.deactivate.scheduler;

import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import de.caritas.cob.userservice.api.workflow.deactivate.service.DeactivateGroupChatService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeactivateGroupChatScheduler {

  private final @NonNull DeactivateGroupChatService deactivateGroupChatService;
  private final @NonNull TenantContextProvider tenantContextProvider;

  @Scheduled(cron = "${group.chat.deactivateworkflow.cron}")
  public void performDeactivationWorkflow() {
    try {
      log.info("Started deactivating stale group chats");
      tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();
      deactivateGroupChatService.deactivateStaleGroupChats();
    } finally {
      log.info("Completed deactivating stale group chats");
    }
  }
}
