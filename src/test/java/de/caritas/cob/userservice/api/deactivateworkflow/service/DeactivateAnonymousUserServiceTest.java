package de.caritas.cob.userservice.api.deactivateworkflow.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.actions.registry.ActionContainer;
import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.actions.session.DeactivateSessionActionCommand;
import de.caritas.cob.userservice.api.actions.session.SetRocketChatRoomReadOnlyActionCommand;
import de.caritas.cob.userservice.api.actions.user.DeactivateKeycloakUserActionCommand;
import de.caritas.cob.userservice.api.repository.session.RegistrationType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DeactivateAnonymousUserServiceTest {

  private static final int DEACTIVATE_PERIOD_MINUTES = 360;

  @InjectMocks
  private DeactivateAnonymousUserService deactivateAnonymousUserService;

  @Mock
  private SessionRepository sessionRepository;

  @Mock
  private ActionsRegistry actionsRegistry;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(deactivateAnonymousUserService, "deactivatePeriodMinutes",
        DEACTIVATE_PERIOD_MINUTES);
  }

  @Test
  void deactivateStaleAnonymousUsers_Should_notUseServices_When_noSessionIsAvailable() {
    this.deactivateAnonymousUserService.deactivateStaleAnonymousUsers();

    verify(this.actionsRegistry, atLeastOnce()).buildContainerForType(User.class);
    verify(this.actionsRegistry, atLeastOnce()).buildContainerForType(Session.class);
  }

  @Test
  void deactivateStaleAnonymousUsers_Should_notPerformAnyDeactivation_When_noSessionIsInProgress() {
    whenSessionRepositoryFindByStatus_ThenReturnUserSessionsWithStatus(
        getAnyStatusWhichIsNotInProgress());
    var deactivateUserAction = mock(DeactivateKeycloakUserActionCommand.class);

    when(this.actionsRegistry.buildContainerForType(User.class))
        .thenReturn(new ActionContainer<>(Set.of(deactivateUserAction)));
    var deactivateSessionAction = mock(DeactivateSessionActionCommand.class);
    when(this.actionsRegistry.buildContainerForType(Session.class))
        .thenReturn(new ActionContainer<>(Set.of(deactivateSessionAction)));

    this.deactivateAnonymousUserService.deactivateStaleAnonymousUsers();

    verify(this.sessionRepository, times(1))
        .findByStatusAndRegistrationType(SessionStatus.IN_PROGRESS, RegistrationType.ANONYMOUS);
    verify(this.actionsRegistry, atLeastOnce()).buildContainerForType(User.class);
    verify(this.actionsRegistry, atLeastOnce()).buildContainerForType(Session.class);
    verifyNoMoreInteractions(deactivateUserAction, deactivateSessionAction);
  }

  private SessionStatus[] getAnyStatusWhichIsNotInProgress() {
    List<SessionStatus> anyStatusNotInProgress = new ArrayList<>(List.of(SessionStatus.values()));
    anyStatusNotInProgress.remove(SessionStatus.IN_PROGRESS);
    return anyStatusNotInProgress.toArray(SessionStatus[]::new);
  }

  private void whenSessionRepositoryFindByStatus_ThenReturnUserSessionsWithStatus(
      SessionStatus... sessionStatus) {
    var user = new User();
    var userSessions = Stream.of(sessionStatus)
        .map(createSessionForUserWithUpdateDateNow(user))
        .collect(Collectors.toSet());
    user.setSessions(userSessions);

    when(this.sessionRepository.findByStatusAndRegistrationType(any(), any()))
        .thenReturn(new ArrayList<>(userSessions));
  }

  private Function<SessionStatus, Session> createSessionForUserWithUpdateDateNow(User user) {
    return createSessionForUserWithUpdateDate(user, LocalDateTime.now());
  }

  private Function<SessionStatus, Session> createSessionForUserWithUpdateDate(User user,
      LocalDateTime sessionUpdateDate) {
    return (sessionStatus) -> createSessionForUser(user, sessionUpdateDate, sessionStatus);
  }

  private Session createSessionForUser(User user, LocalDateTime updateDate,
      SessionStatus sessionStatus) {
    Session session = new Session();
    session.setId((long) sessionStatus.getValue());
    session.setUpdateDate(updateDate);
    session.setStatus(sessionStatus);
    session.setUser(user);
    session.setRegistrationType(RegistrationType.ANONYMOUS);
    return session;
  }

  @ParameterizedTest
  @MethodSource("createUpdateDatesWithinDeactivationPeriod")
  void deactivateStaleAnonymousUsers_Should_notPerformAnyDeactivation_When_sessionsAreInProgressWithinDeactivatePeriod(
      LocalDateTime updateDate) {
    var user = createUserWithSingleSession(updateDate);
    when(this.sessionRepository.findByStatusAndRegistrationType(any(), any()))
        .thenReturn(new ArrayList<>(user.getSessions()));
    var deactivateUserAction = mock(DeactivateKeycloakUserActionCommand.class);
    when(this.actionsRegistry.buildContainerForType(User.class))
        .thenReturn(new ActionContainer<>(Set.of(deactivateUserAction)));
    var deactivateSessionAction = mock(DeactivateSessionActionCommand.class);
    when(this.actionsRegistry.buildContainerForType(Session.class))
        .thenReturn(new ActionContainer<>(Set.of(deactivateSessionAction)));

    this.deactivateAnonymousUserService.deactivateStaleAnonymousUsers();

    verify(this.sessionRepository, times(1))
        .findByStatusAndRegistrationType(SessionStatus.IN_PROGRESS, RegistrationType.ANONYMOUS);
    verify(this.actionsRegistry, atLeastOnce()).buildContainerForType(User.class);
    verify(this.actionsRegistry, atLeastOnce()).buildContainerForType(Session.class);
    verifyNoMoreInteractions(deactivateUserAction, deactivateSessionAction);
  }

  private static List<LocalDateTime> createUpdateDatesWithinDeactivationPeriod() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneSecondWithinDeletionPeriod = now
        .minusMinutes(DEACTIVATE_PERIOD_MINUTES)
        .plusSeconds(10);
    LocalDateTime timeInTheFuture = now.plusSeconds(20);

    return List.of(now, oneSecondWithinDeletionPeriod, timeInTheFuture);
  }

  private User createUserWithSingleSession(LocalDateTime updateDate) {
    var user = new User();
    user.setUserId("user id");
    var userSessions = Set.of(createSessionForUser(user, updateDate, SessionStatus.IN_PROGRESS));
    user.setSessions(userSessions);
    return user;
  }

  @ParameterizedTest
  @MethodSource("createOverdueUpdateDates")
  void deactivateStaleAnonymousUsers_Should_callUserAndSessionDeactivateActions_When_userSessionsAreInProgressForTooLong(
      LocalDateTime overdueUpdateDate) {
    var user = createUserWithSingleSession(overdueUpdateDate);

    when(this.sessionRepository.findByStatusAndRegistrationType(any(), any()))
        .thenReturn(new ArrayList<>(user.getSessions()));

    var deactivateUserAction = mock(DeactivateKeycloakUserActionCommand.class);
    when(this.actionsRegistry.buildContainerForType(User.class))
        .thenReturn(new ActionContainer<>(Set.of(deactivateUserAction)));
    var deactivateSessionAction = mock(DeactivateSessionActionCommand.class);
    var setRocketChatRoomReadOnlyAction = mock(SetRocketChatRoomReadOnlyActionCommand.class);
    when(this.actionsRegistry.buildContainerForType(Session.class))
        .thenReturn(new ActionContainer<>(Set.of(deactivateSessionAction, setRocketChatRoomReadOnlyAction)));

    this.deactivateAnonymousUserService.deactivateStaleAnonymousUsers();

    verify(this.sessionRepository, times(1))
        .findByStatusAndRegistrationType(SessionStatus.IN_PROGRESS, RegistrationType.ANONYMOUS);
    verify(this.actionsRegistry, atLeastOnce()).buildContainerForType(User.class);
    verify(this.actionsRegistry, atLeastOnce()).buildContainerForType(Session.class);
    verify(deactivateUserAction, times(1)).execute(user);
    user.getSessions().forEach(session -> {
        verify(deactivateSessionAction, times(1)).execute(session);
        verify(setRocketChatRoomReadOnlyAction, times(1)).execute(session);
    });
  }

  private static List<LocalDateTime> createOverdueUpdateDates() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneDeletionPeriodAgo = now
        .minusMinutes(DEACTIVATE_PERIOD_MINUTES);
    LocalDateTime timeLongInThePast = oneDeletionPeriodAgo.minusMinutes(10);

    return List.of(oneDeletionPeriodAgo, timeLongInThePast);
  }

}
