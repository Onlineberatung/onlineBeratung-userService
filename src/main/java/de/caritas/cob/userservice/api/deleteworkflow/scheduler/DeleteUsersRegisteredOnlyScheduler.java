package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import de.caritas.cob.userservice.api.deleteworkflow.service.DeleteUsersRegisteredOnlyService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for deletion of only registered users without sessions.
 */
@Component
@RequiredArgsConstructor
public class DeleteUsersRegisteredOnlyScheduler {

  private final @NonNull DeleteUsersRegisteredOnlyService deleteUsersRegisteredOnlyService;

  @Value("${user.registeredonly.deleteWorkflow.enabled}")
  private boolean userRegisteredOnlyDeleteWorkflowEnabled;

  /**
   * Entry method to perform deletion workflow.
   */
  @Scheduled(cron = "${user.registeredonly.deleteWorkflow.cron}")
  public void performDeletionWorkflow() {

    if (userRegisteredOnlyDeleteWorkflowEnabled) {
      this.deleteUsersRegisteredOnlyService.deleteUserAccounts();
    }
  }

}