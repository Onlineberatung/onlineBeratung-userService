package de.caritas.cob.userservice.api.deleteworkflow.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to trigger deletion of askers with no running sessions.
 */
@Service
@RequiredArgsConstructor
public class DeleteUsersRegisteredOnlyService {

  private final @NonNull UserRepository userRepository;
  private final @NonNull DeleteUserAccountService deleteUserAccountService;
  private final @NonNull WorkflowErrorMailService workflowErrorMailService;

  @Value("${user.registeredonly.deleteWorkflow.check.days}")
  private int userRegisteredOnlyDeleteWorkflowCheckDays;

  /**
   * Deletes all askers without running sessions.
   */
  public void deleteUserAccounts() {

    var workflowErrors = deleteAskersAndCollectPossibleErrors();

    if (isNotEmpty(workflowErrors)) {
      this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);
    }

  }

  private List<DeletionWorkflowError> deleteAskersAndCollectPossibleErrors() {

    LocalDateTime dateTimeToCheck = calculateDateTimeToCheck();
    return userRepository
        .findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(dateTimeToCheck)
        .stream()
        .map(deleteUserAccountService::performUserDeletion)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private LocalDateTime calculateDateTimeToCheck() {
    return LocalDateTime
        .now()
        .with(LocalTime.MIDNIGHT)
        .minusDays(userRegisteredOnlyDeleteWorkflowCheckDays);
  }

}
