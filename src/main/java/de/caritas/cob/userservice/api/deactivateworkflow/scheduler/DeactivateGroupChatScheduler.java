package de.caritas.cob.userservice.api.deactivateworkflow.scheduler;

import de.caritas.cob.userservice.api.deactivateworkflow.service.DeactivateGroupChatService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateGroupChatScheduler {

  private final @NonNull DeactivateGroupChatService deactivateGroupChatService;

  @Scheduled(cron = "${group.chat.deactivateworkflow.cron}")
  public void performDeactivationWorkflow() {
    deactivateGroupChatService.deactivateStaleGroupChats();
  }
}
