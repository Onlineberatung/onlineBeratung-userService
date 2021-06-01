package de.caritas.cob.userservice.api.deactivateworkflow.service;

import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.actions.session.DeactivateSessionActionCommand;
import de.caritas.cob.userservice.api.actions.user.DeactivateKeycloakUserActionCommand;
import de.caritas.cob.userservice.api.actions.session.SetRocketChatRoomReadOnlyActionCommand;
import de.caritas.cob.userservice.api.repository.session.RegistrationType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
  private final @NonNull ActionsRegistry actionsRegistry;
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

    deactivateAnonymousUsersAndSessions(staleAnonymousSessions);
  }

  private Predicate<Session> isSessionOutsideOfDeactivationTime(LocalDateTime deactivationTime) {
    return session -> session.getUpdateDate().isBefore(deactivationTime);
  }

  private void deactivateAnonymousUsersAndSessions(Set<Session> staleSessions) {
    Set<User> usersToDeactivate = staleSessions.stream()
        .map(Session::getUser)
        .collect(Collectors.toSet());

    this.performUserDeactivationActions(usersToDeactivate);
    this.performSessionDeactivationActions(staleSessions);
  }

  private void performUserDeactivationActions(Set<User> usersToDeactivate) {
    var userDeactivationActions = this.actionsRegistry.buildContainerForType(User.class);
    usersToDeactivate
        .forEach(userToDeactivate -> userDeactivationActions
            .addActionToExecute(DeactivateKeycloakUserActionCommand.class)
            .executeActions(userToDeactivate));
  }

  private void performSessionDeactivationActions(Set<Session> staleAnonymousSessions) {
    var sessionDeactivationActions = this.actionsRegistry.buildContainerForType(Session.class);
    staleAnonymousSessions.forEach(staleSession -> sessionDeactivationActions
        .addActionToExecute(SetRocketChatRoomReadOnlyActionCommand.class)
        .addActionToExecute(DeactivateSessionActionCommand.class)
        .executeActions(staleSession)
    );
  }

}
