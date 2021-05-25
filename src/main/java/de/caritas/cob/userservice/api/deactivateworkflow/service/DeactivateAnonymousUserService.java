package de.caritas.cob.userservice.api.deactivateworkflow.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.deactivateworkflow.AbstractDeactivateAction;
import de.caritas.cob.userservice.api.deactivateworkflow.model.DeactivateWorkflowError;
import de.caritas.cob.userservice.api.deactivateworkflow.registry.DeactivateActionsRegistry;
import de.caritas.cob.userservice.api.repository.session.RegistrationType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to trigger deletion of anonymous users.
 */
@Service
@RequiredArgsConstructor
public class DeactivateAnonymousUserService {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull DeactivateActionsRegistry deactivateActionsRegistry;
  private final @NonNull DeactivateWorkflowErrorMailService workflowErrorMailService;
  @Value("${user.anonymous.deactivateworkflow.periodMinutes}")
  private long deactivatePeriodMinutes;

  /**
   * Deletes all anonymous users with special constraints.
   */
  @Transactional
  public void deactivateStaleAnonymousUsers() {
    LocalDateTime deactivationTime = LocalDateTime.now().minusMinutes(deactivatePeriodMinutes);
    List<Session> anonymousSessionsInProgress =
        this.sessionRepository.findByStatusAndRegistrationType(
            SessionStatus.IN_PROGRESS, RegistrationType.ANONYMOUS);

    Set<Session> staleAnonymousSessions = anonymousSessionsInProgress.stream()
        .filter(isSessionOutsideOfDeactivationTime(deactivationTime))
        .collect(Collectors.toSet());

    var workflowErrors = deactivateAnonymousUsersAndSessions(staleAnonymousSessions);

    if (isNotEmpty(workflowErrors)) {
      this.workflowErrorMailService.buildAndSendErrorMail(workflowErrors);
    }
  }

  private Predicate<Session> isSessionOutsideOfDeactivationTime(LocalDateTime deactivationTime) {
    return session -> session.getUpdateDate().isBefore(deactivationTime);
  }

  private List<DeactivateWorkflowError> deactivateAnonymousUsersAndSessions(
      Set<Session> staleSessions) {
    Set<User> usersToDeactivate = staleSessions.stream()
        .map(Session::getUser)
        .collect(Collectors.toSet());

    var workflowErrors = this.performUserDeactivationActions(usersToDeactivate);
    workflowErrors.addAll(performSessionDeactivationActions(staleSessions));

    return workflowErrors;
  }

  private List<DeactivateWorkflowError> performUserDeactivationActions(
      Set<User> usersToDeactivate) {
    var userDeactivationActions = this.deactivateActionsRegistry.getUserDeactivateActions();
    return usersToDeactivate.stream()
        .map(this.performUserDeactivationWithActions(userDeactivationActions))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private Function<User, List<DeactivateWorkflowError>> performUserDeactivationWithActions(
      List<AbstractDeactivateAction<User>> userDeactivationActions) {
    return user -> userDeactivationActions.stream()
        .map(action -> action.execute(user))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private List<DeactivateWorkflowError> performSessionDeactivationActions(
      Set<Session> staleAnonymousSessions) {
    var sessionDeactivationActions = this.deactivateActionsRegistry.getSessionDeactivateActions();
    return staleAnonymousSessions.stream()
        .map(this.performSessionDeactivationWithActions(sessionDeactivationActions))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private Function<Session, List<DeactivateWorkflowError>> performSessionDeactivationWithActions(
      List<AbstractDeactivateAction<Session>> sessionDeactivationActions) {
    return session -> sessionDeactivationActions.stream()
        .map(deactivationAction -> deactivationAction.execute(session))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

}
