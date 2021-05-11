package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteUserAccountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteUserAnonymousScheduler {

  @Scheduled(cron = "${user.anonymous.deleteworkflow.cron}")
  public void performDeletionWorkflow() {

  }
}
