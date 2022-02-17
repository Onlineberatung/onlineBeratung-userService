package de.caritas.cob.userservice.api.deactivateworkflow.scheduler;

import de.caritas.cob.userservice.api.deactivateworkflow.service.DeactivateGroupChatService;
import de.caritas.cob.userservice.api.tenant.TenantContextProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateGroupChatScheduler {

  private final @NonNull DeactivateGroupChatService deactivateGroupChatService;
  private final @NonNull TenantContextProvider tenantContextProvider;

  @Scheduled(cron = "${group.chat.deactivateworkflow.cron}")
  public void performDeactivationWorkflow() {
    tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();
    deactivateGroupChatService.deactivateStaleGroupChats();
  }
}
