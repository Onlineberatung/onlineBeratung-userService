package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteUsersRegisteredOnlyService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteUsersRegisteredOnlyScheduler {

  private final @NonNull DeleteUsersRegisteredOnlyService deleteUsersRegisteredOnlyService;

  @Value("${user.registeredonly.deleteWorkflow.enabled}")
  private boolean userRegisteredOnlyDeleteWorkflowEnabled;

  @Scheduled(cron = "${user.registeredonly.deleteWorkflow.cron}")
  public void performDeletionWorkflow() {

    if (!userRegisteredOnlyDeleteWorkflowEnabled) {
      return;
    }

    this.deleteUsersRegisteredOnlyService.deleteUserAccounts();

  }

}
