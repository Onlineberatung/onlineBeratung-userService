package de.caritas.cob.userservice.api.deleteworkflow.service;

import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionSourceType.ASKER;
import static de.caritas.cob.userservice.api.deleteworkflow.model.DeletionTargetType.ALL;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.deleteworkflow.service.provider.InactivePrivateGroupsProvider;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service to trigger deletion of inactive sessions and asker accounts.
 */
@Service
@RequiredArgsConstructor
public class DeleteInactiveSessionsAndUserService {

  private final @NonNull UserRepository userRepository;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull DeleteUserAccountService deleteUserAccountService;
  private final @NonNull WorkflowErrorMailService workflowErrorMailService;
  private final @NonNull DeleteSessionService deleteSessionService;
  private final @NonNull InactivePrivateGroupsProvider inactivePrivateGroupsProvider;

  /**
   * Deletes all inactive sessions and even the asker accounts, if there are no more active
   * sessions.
   */
  public void deleteInactiveSessionsAndUsers() {

    Map<String, List<String>> userWithInactiveGroupsMap =
        inactivePrivateGroupsProvider.retrieveUserWithInactiveGroupsMap();

    List<DeletionWorkflowError> workflowErrors = userWithInactiveGroupsMap
        .entrySet()
        .stream()
        .map(this::performDeletionWorkflow)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    sendWorkflowErrorsMail(workflowErrors);
  }

  private void sendWorkflowErrorsMail(List<DeletionWorkflowError> workflowErrors) {
    if (isNotEmpty(workflowErrors)) {
      this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);
    }
  }

  private List<DeletionWorkflowError> performDeletionWorkflow(
      Entry<String, List<String>> userInactiveGroupEntry) {

    List<DeletionWorkflowError> workflowErrors = new ArrayList<>();

    Optional<User> user = userRepository
        .findByRcUserIdAndDeleteDateIsNull(userInactiveGroupEntry.getKey());
    user.ifPresentOrElse(u -> workflowErrors.addAll(
            deleteInactiveGroupsOrUser(userInactiveGroupEntry, u)),
        () -> workflowErrors.add(DeletionWorkflowError.builder()
            .deletionSourceType(ASKER)
            .deletionTargetType(ALL)
            .identifier(userInactiveGroupEntry.getKey())
            .reason("User could not be found.")
            .timestamp(nowInUtc())
            .build()));

    return workflowErrors;
  }

  private List<DeletionWorkflowError> deleteInactiveGroupsOrUser(
      Entry<String, List<String>> userInactiveGroupEntry, User user) {

    List<Session> userSessionList = sessionRepository.findByUser(user);

    if (allSessionsOfUserAreInactive(userInactiveGroupEntry, userSessionList)) {
      return deleteUserAccountService.performUserDeletion(user);
    }

    return userInactiveGroupEntry.getValue()
        .stream()
        .map(rcGroupId -> performSessionDeletion(rcGroupId, userSessionList))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private boolean allSessionsOfUserAreInactive(Entry<String, List<String>> userInactiveGroupEntry,
      List<Session> userSessionList) {
    return userInactiveGroupEntry.getValue().size() == userSessionList.size();
  }

  private List<DeletionWorkflowError> performSessionDeletion(String rcGroupId,
      List<Session> userSessionList) {

    List<DeletionWorkflowError> workflowErrors = new ArrayList<>();

    Optional<Session> session = findSessionInUserSessionList(rcGroupId, userSessionList);

    session
        .ifPresentOrElse(s ->
                workflowErrors.addAll(deleteSessionService.performSessionDeletion(s)),
            () -> workflowErrors.add(DeletionWorkflowError.builder()
                .deletionSourceType(ASKER)
                .deletionTargetType(ALL)
                .identifier(rcGroupId)
                .reason("Session with rc group id could not be found.")
                .timestamp(nowInUtc())
                .build()));

    return workflowErrors;
  }

  private Optional<Session> findSessionInUserSessionList(String rcGroupId,
      List<Session> userSessionList) {
    return userSessionList
        .stream()
        .filter(s -> s.getGroupId().equals(rcGroupId))
        .findFirst();
  }
}
