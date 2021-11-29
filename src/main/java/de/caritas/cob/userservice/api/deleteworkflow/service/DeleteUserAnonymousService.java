package de.caritas.cob.userservice.api.deleteworkflow.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.deleteworkflow.model.DeletionWorkflowError;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to trigger deletion of anonymous users.
 */
@Service
@RequiredArgsConstructor
public class DeleteUserAnonymousService {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull DeleteUserAccountService deleteUserAccountService;
  private final @NonNull WorkflowErrorMailService workflowErrorMailService;
  @Value("${user.anonymous.deleteworkflow.periodMinutes}")
  private int deletionPeriodMinutes;

  /**
   * Deletes all anonymous users with special constraints.
   */
  @Transactional
  public void deleteInactiveAnonymousUsers() {
    List<DeletionWorkflowError> workflowErrors = deleteAnonymousUsersWithOverdueSessions();

    if (isNotEmpty(workflowErrors)) {
      this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);
    }
  }

  private List<DeletionWorkflowError> deleteAnonymousUsersWithOverdueSessions() {
    List<Session> doneSessions = this.sessionRepository.findByStatus(SessionStatus.DONE);
    LocalDateTime deletionTime = LocalDateTime.now().minusMinutes(deletionPeriodMinutes);

    Set<User> usersWithoutOpenSessions = doneSessions.stream()
        .filter(sessionUsersHavingAllSessionsDoneAndOverdue(deletionTime))
        .map(Session::getUser)
        .collect(Collectors.toSet());

    return usersWithoutOpenSessions.stream()
        .map(deleteUserAccountService::performUserDeletion)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private Predicate<Session> sessionUsersHavingAllSessionsDoneAndOverdue(
      LocalDateTime deletionTime) {
    return session -> {
      Set<Session> userSessions = session.getUser().getSessions();
      return CollectionUtils.isEmpty(userSessions)
          || (allSessionsAreDone(userSessions)
          && allSessionsAreBeforeDeletionTime(deletionTime, userSessions));
    };
  }

  private boolean allSessionsAreDone(Set<Session> sessions) {
    return sessions.stream()
        .map(Session::getStatus)
        .allMatch(SessionStatus.DONE::equals);
  }

  private boolean allSessionsAreBeforeDeletionTime(LocalDateTime deletionTime,
      Set<Session> sessions) {
    return sessions.stream()
        .map(Session::getUpdateDate)
        .allMatch(updateDate -> updateDate.isBefore(deletionTime));
  }
}
