package de.caritas.cob.userservice.api.workflow.delete.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.workflow.delete.model.DeletionSourceType.ASKER;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetUserIdException;
import de.caritas.cob.userservice.api.helper.CustomLocalDateTime;
import de.caritas.cob.userservice.api.helper.DateCalculator;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionTargetType;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service to trigger deletion of askers with no running sessions. */
@Service
@RequiredArgsConstructor
public class DeleteUsersRegisteredOnlyService {

  private final @NonNull UserRepository userRepository;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull DeleteUserAccountService deleteUserAccountService;
  private final @NonNull WorkflowErrorMailService workflowErrorMailService;

  @Value("${user.registeredonly.deleteWorkflow.check.days}")
  private int userRegisteredOnlyDeleteWorkflowCheckDays;

  /** Deletes all askers with no running sessions before the set date. */
  public void deleteUserAccountsTimeSensitive() {
    var dateTimeToCheck =
        DateCalculator.calculateDateInThePastAtMidnight(userRegisteredOnlyDeleteWorkflowCheckDays);
    deleteUserAccountsBefore(dateTimeToCheck);
  }

  /** Deletes all askers with no running sessions no matter when created. */
  public void deleteUserAccountsTimeInsensitive() {
    var startOfTomorrow = CustomLocalDateTime.nowInUtc().with(LocalTime.MIDNIGHT).plusDays(1);
    deleteUserAccountsBefore(startOfTomorrow);
  }

  private void deleteUserAccountsBefore(LocalDateTime dateTimeToCheck) {
    var workflowErrors =
        userRepository
            .findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(dateTimeToCheck)
            .stream()
            .map(this::performUserDeletion)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    if (isNotEmpty(workflowErrors)) {
      workflowErrorMailService.buildAndSendErrorMail(workflowErrors);
    }
  }

  private List<DeletionWorkflowError> performUserDeletion(User user) {
    try {
      user.setRcUserId(rocketChatService.getRocketChatUserIdByUsername(user.getUsername()));
    } catch (RocketChatGetUserIdException ex) {
      return Collections.singletonList(
          DeletionWorkflowError.builder()
              .deletionSourceType(ASKER)
              .deletionTargetType(DeletionTargetType.ALL)
              .identifier(user.getUserId() + "/" + user.getUsername())
              .reason(ex.getMessage())
              .timestamp(nowInUtc())
              .build());
    }

    return deleteUserAccountService.performUserDeletion(user);
  }
}
