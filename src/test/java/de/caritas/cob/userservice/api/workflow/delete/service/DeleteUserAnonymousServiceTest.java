package de.caritas.cob.userservice.api.workflow.delete.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.workflow.delete.model.DeletionWorkflowError;
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
class DeleteUserAnonymousServiceTest {

  private static final int DELETION_PERIOD_MINUTES = 1200;

  @InjectMocks private DeleteUserAnonymousService deleteUserAnonymousService;

  @Mock private SessionRepository sessionRepository;

  @Mock private DeleteUserAccountService deleteUserAccountService;

  @Mock private WorkflowErrorMailService workflowErrorMailService;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(
        deleteUserAnonymousService, "deletionPeriodMinutes", DELETION_PERIOD_MINUTES);
  }

  @Test
  void deleteInactiveAnonymousUsers_Should_notPerformAnyDeletion_When_noSessionIsAvailable() {
    this.deleteUserAnonymousService.deleteInactiveAnonymousUsers();

    verifyNoMoreInteractions(this.workflowErrorMailService);
    verifyNoMoreInteractions(this.deleteUserAccountService);
  }

  @Test
  void deleteInactiveAnonymousUsers_Should_notPerformAnyDeletion_When_noSessionIsDone() {
    whenSessionRepositoryFindByStatus_ThenReturnUserSessionsWithStatus(
        getAnyStatusWhichIsNotDone());

    this.deleteUserAnonymousService.deleteInactiveAnonymousUsers();

    verifyNoMoreInteractions(this.workflowErrorMailService);
    verifyNoMoreInteractions(this.deleteUserAccountService);
  }

  private SessionStatus[] getAnyStatusWhichIsNotDone() {
    List<SessionStatus> anyStatusNotDone = new ArrayList<>(List.of(SessionStatus.values()));
    anyStatusNotDone.remove(SessionStatus.DONE);
    return anyStatusNotDone.toArray(SessionStatus[]::new);
  }

  private void whenSessionRepositoryFindByStatus_ThenReturnUserSessionsWithStatus(
      SessionStatus... sessionStatus) {
    User user = new User();
    Set<Session> userSessions =
        Stream.of(sessionStatus)
            .map(createSessionForUserWithUpdateDateNow(user))
            .collect(Collectors.toSet());
    user.setSessions(userSessions);

    when(this.sessionRepository.findByStatus(any())).thenReturn(new ArrayList<>(userSessions));
  }

  private Function<SessionStatus, Session> createSessionForUserWithUpdateDateNow(User user) {
    return createSessionForUserWithUpdateDate(user, LocalDateTime.now());
  }

  private Function<SessionStatus, Session> createSessionForUserWithUpdateDate(
      User user, LocalDateTime sessionUpdateDate) {
    return (sessionStatus) -> createSessionForUser(user, sessionUpdateDate, sessionStatus);
  }

  private Session createSessionForUser(
      User user, LocalDateTime updateDate, SessionStatus sessionStatus) {
    Session session = new Session();
    session.setId((long) sessionStatus.getValue());
    session.setUpdateDate(updateDate);
    session.setStatus(sessionStatus);
    session.setUser(user);
    return session;
  }

  @Test
  void deleteInactiveAnonymousUsers_Should_notPerformAnyDeletion_When_notAllSessionsAreDone() {
    whenSessionRepositoryFindByStatus_ThenReturnUserSessionsWithStatus(
        SessionStatus.IN_PROGRESS, SessionStatus.DONE);

    this.deleteUserAnonymousService.deleteInactiveAnonymousUsers();

    verifyNoMoreInteractions(this.workflowErrorMailService);
    verifyNoMoreInteractions(this.deleteUserAccountService);
  }

  @ParameterizedTest
  @MethodSource("createUpdateDatesWithinDeletionPeriod")
  void
      deleteInactiveAnonymousUsers_Should_notPerformAnyDeletion_When_sessionsAreDoneWithinDeletionPeriod(
          LocalDateTime updateDate) {
    User user = new User();
    Set<Session> userSessions = Set.of(createSessionForUser(user, updateDate, SessionStatus.DONE));
    user.setSessions(userSessions);

    when(this.sessionRepository.findByStatus(any())).thenReturn(new ArrayList<>(userSessions));

    this.deleteUserAnonymousService.deleteInactiveAnonymousUsers();

    verifyNoMoreInteractions(this.workflowErrorMailService);
    verifyNoMoreInteractions(this.deleteUserAccountService);
  }

  private static List<LocalDateTime> createUpdateDatesWithinDeletionPeriod() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneSecondWithinDeletionPeriod =
        now.minusMinutes(DELETION_PERIOD_MINUTES).plusSeconds(1);
    LocalDateTime timeInTheFuture = now.plusSeconds(20);

    return List.of(now, oneSecondWithinDeletionPeriod, timeInTheFuture);
  }

  @ParameterizedTest
  @MethodSource("createOverdueUpdateDates")
  void deleteInactiveAnonymousUsers_Should_performAskerDeletion_When_userSessionsAreDoneAndOverdue(
      LocalDateTime overdueUpdateDate) {
    User user = new User();
    Set<Session> userSessions =
        Set.of(createSessionForUser(user, overdueUpdateDate, SessionStatus.DONE));
    user.setSessions(userSessions);

    when(this.sessionRepository.findByStatus(any())).thenReturn(new ArrayList<>(userSessions));

    this.deleteUserAnonymousService.deleteInactiveAnonymousUsers();

    verifyNoMoreInteractions(this.workflowErrorMailService);
    verify(this.deleteUserAccountService, times(1)).performUserDeletion(user);
  }

  private static List<LocalDateTime> createOverdueUpdateDates() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime oneDeletionPeriodAgo = now.minusMinutes(DELETION_PERIOD_MINUTES);
    LocalDateTime timeLongInThePast = oneDeletionPeriodAgo.minusMinutes(10);

    return List.of(oneDeletionPeriodAgo, timeLongInThePast);
  }

  @Test
  void deleteInactiveAnonymousUsers_Should_sendErrorMails_When_someActionsFail() {
    User user = new User();
    Set<Session> userSessions =
        Set.of(createSessionForUser(user, createOverdueUpdateDates().get(0), SessionStatus.DONE));
    user.setSessions(userSessions);

    when(this.sessionRepository.findByStatus(any())).thenReturn(new ArrayList<>(userSessions));

    DeletionWorkflowError error = mock(DeletionWorkflowError.class);
    when(this.deleteUserAccountService.performUserDeletion(any())).thenReturn(List.of(error));

    this.deleteUserAnonymousService.deleteInactiveAnonymousUsers();

    verify(this.workflowErrorMailService, times(1)).buildAndSendErrorMail(List.of(error));
    verify(this.deleteUserAccountService, times(1)).performUserDeletion(user);
  }
}
