package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteUserAnonymousService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteUserAnonymousScheduler {

  private final @NonNull DeleteUserAnonymousService deleteUserAnonymousService;

  @Scheduled(cron = "${user.anonymous.deleteworkflow.cron}")
  public void performDeletionWorkflow() {
    deleteUserAnonymousService.deleteInactiveAnonymousUsers();
  }
}
