package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteUserAccountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteUserAccountScheduler {

  private final @NonNull DeleteUserAccountService deleteUserAccountService;

  @Scheduled(cron = "${user.account.deleteworkflow.cron}")
  public void performDeletionWorkflow() {
    this.deleteUserAccountService.deleteUserAccounts();
  }

}
