package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteUsersRegisteredOnlyService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteUsersRegisteredOnlyScheduler {

  private final @NonNull DeleteUsersRegisteredOnlyService deleteUsersRegisteredOnlyService;

  @Scheduled(cron = "${user.registeredonly.deleteWorkflow.cron}")
  public void performDeletionWorkflow() {
    this.deleteUsersRegisteredOnlyService.deleteUserAccounts();
  }

}
