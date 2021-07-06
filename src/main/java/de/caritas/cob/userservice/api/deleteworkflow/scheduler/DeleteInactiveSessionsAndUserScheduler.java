package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteInactiveSessionsAndUserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteInactiveSessionsAndUserScheduler {

  private final @NonNull DeleteInactiveSessionsAndUserService deleteInactiveSessionsAndUserService;

  @Scheduled(cron = "${inactive.session.and.user.deleteWorkflow.cron}")
  public void performDeletionWorkflow() {
    this.deleteInactiveSessionsAndUserService.deleteInactiveSessionsAndUsers();
  }

}
