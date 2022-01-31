package de.caritas.cob.userservice.api.workflow.deactivate.scheduler;

import de.caritas.cob.userservice.api.workflow.deactivate.service.DeactivateAnonymousUserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateAnonymousUserScheduler {

  private final @NonNull DeactivateAnonymousUserService deactivateAnonymousUserService;

  @Scheduled(cron = "${user.anonymous.deactivateworkflow.cron}")
  public void performDeactivationWorkflow() {
    deactivateAnonymousUserService.deactivateStaleAnonymousUsers();
  }
}
