package de.caritas.cob.userservice.api.service.archive;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.AccountManager;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.statistics.event.ArchiveOrDeleteSessionStatisticsEvent;
import de.caritas.cob.userservice.api.service.statistics.event.StatisticsEvent;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class SessionArchiveServiceTest {

  @InjectMocks SessionArchiveService sessionArchiveService;
  @Mock SessionRepository sessionRepository;

  @Mock
  @SuppressWarnings("unused")
  AuthenticatedUser authenticatedUser;

  @Mock SessionArchiveValidator sessionArchiveValidator;

  @Mock
  @SuppressWarnings("unused")
  RocketChatService rocketChatService;

  @Mock
  @SuppressWarnings("unused")
  ConsultantAgencyService consultantAgencyService;

  @Mock
  @SuppressWarnings("unused")
  AccountManager accountManager;

  @Mock StatisticsService statisticsService;

  @Mock private Logger logger;

  @BeforeEach
  public void setUp() {
    setInternalState(SessionArchiveService.class, "log", logger);
  }

  @Test
  public void archiveSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {
    assertThrows(
        NotFoundException.class,
        () -> {
          when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

          sessionArchiveService.archiveSession(SESSION_ID);
        });
  }

  @Test
  public void
      archiveSession_Should_ThrowConflictException_WhenSessionShouldBeArchivedAndIsNotInProgress() {
    assertThrows(
        ConflictException.class,
        () -> {
          Session session = Mockito.mock(Session.class);
          when(session.isAdvised(any())).thenReturn(true);
          when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
          doThrow(new ConflictException("Conflict"))
              .when(sessionArchiveValidator)
              .isValidForArchiving(session);

          sessionArchiveService.archiveSession(SESSION_ID);
          verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
        });
  }

  @Test
  public void
      archiveSession_Should_ThrowForbiddenException_WhenConsultantHasaNoAuthorizationForTheSession() {
    assertThrows(
        ForbiddenException.class,
        () -> {
          Session session = Mockito.mock(Session.class);
          when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

          sessionArchiveService.archiveSession(SESSION_ID);

          verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
        });
  }

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {

    Session session = Mockito.mock(Session.class);
    when(session.isAdvised(any())).thenReturn(true);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test
  public void
      archiveSession_Should_ThrowForbiddenException_WhenUserHasNoAuthorizationForTheSession() {
    assertThrows(
        ForbiddenException.class,
        () -> {
          Session session = Mockito.mock(Session.class);
          when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

          sessionArchiveService.archiveSession(SESSION_ID);

          verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
        });
  }

  @Test
  public void
      archiveSession_Should_ChangeStatusOfSessionAndFireArchiveStatisticsEvent_WhenUserHasPermission() {

    Session session = Mockito.mock(Session.class);
    when(session.isAdvised(any())).thenReturn(true);
    when(session.getUser()).thenReturn(new User());
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
    ArgumentCaptor<ArchiveOrDeleteSessionStatisticsEvent> statisticsEventArgumentCaptor =
        ArgumentCaptor.forClass(ArchiveOrDeleteSessionStatisticsEvent.class);
    verify(statisticsService).fireEvent(statisticsEventArgumentCaptor.capture());
    ArchiveOrDeleteSessionStatisticsEvent event = statisticsEventArgumentCaptor.getValue();
    assertThat(event.getEventType()).isEqualTo(EventType.ARCHIVE_SESSION);
    assertThat(event.getPayload().get()).isNotEmpty();
  }

  @Test
  public void archiveSession_Should_LogButNotFail_WhenErrorDuringStatisticsArchiveOccurs() {

    Session session = Mockito.mock(Session.class);
    when(session.isAdvised(any())).thenReturn(true);
    when(session.getUser()).thenReturn(new User());
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    IllegalStateException illegalStateException = new IllegalStateException();
    doThrow(illegalStateException)
        .when(statisticsService)
        .fireEvent(Mockito.any(StatisticsEvent.class));
    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
    ArgumentCaptor<ArchiveOrDeleteSessionStatisticsEvent> statisticsEventArgumentCaptor =
        ArgumentCaptor.forClass(ArchiveOrDeleteSessionStatisticsEvent.class);
    verify(statisticsService).fireEvent(statisticsEventArgumentCaptor.capture());
    ArchiveOrDeleteSessionStatisticsEvent event = statisticsEventArgumentCaptor.getValue();
    assertThat(event.getEventType()).isEqualTo(EventType.ARCHIVE_SESSION);
    assertThat(event.getPayload().get()).isNotEmpty();
    verify(logger)
        .error("Could not create session archive statistics event", illegalStateException);
  }

  @Test
  public void dearchiveSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {
    assertThrows(
        NotFoundException.class,
        () -> {
          when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
          sessionArchiveService.dearchiveSession(SESSION_ID);
        });
  }

  @Test
  public void
      dearchiveSession_Should_ThrowConflictException_WhenSessionShouldBeReactivatedAndIsAlreadyInProgress() {
    assertThrows(
        ConflictException.class,
        () -> {
          Session session = Mockito.mock(Session.class);
          when(session.isAdvised(any())).thenReturn(true);
          when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
          doThrow(new ConflictException("Conflict"))
              .when(sessionArchiveValidator)
              .isValidForDearchiving(session);

          sessionArchiveService.dearchiveSession(SESSION_ID);

          verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
        });
  }

  @Test
  public void
      dearchiveSession_Should_ThrowForbiddenException_WhenConsultantHasNoAuthorizationForTheSession() {
    assertThrows(
        ForbiddenException.class,
        () -> {
          Session session = Mockito.mock(Session.class);
          when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

          sessionArchiveService.dearchiveSession(SESSION_ID);

          verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
        });
  }

  @Test
  public void
      dearchiveSession_Should_ThrowForbiddenException_WhenSessionIsNotTeamSessionAndConsultantNotAssigned() {
    assertThrows(
        ForbiddenException.class,
        () -> {
          Session session = Mockito.mock(Session.class);
          when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

          sessionArchiveService.dearchiveSession(SESSION_ID);

          verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
        });
  }

  @Test
  public void dearchiveSession_Should_ThrowForbiddenException_WhenNoConsultantOrUserRole() {
    assertThrows(
        ForbiddenException.class,
        () -> {
          Session session = Mockito.mock(Session.class);
          when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

          sessionArchiveService.dearchiveSession(SESSION_ID);

          verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
        });
  }

  @Test
  public void dearchiveSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {
    Session session = Mockito.mock(Session.class);
    when(session.isAdvised(any())).thenReturn(true);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test
  public void
      dearchiveSession_Should_ThrowForbiddenException_WhenUserHasNoAuthorizationForTheSession() {
    assertThrows(
        ForbiddenException.class,
        () -> {
          Session session = Mockito.mock(Session.class);
          when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

          sessionArchiveService.dearchiveSession(SESSION_ID);

          verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
        });
  }

  @Test
  public void dearchiveSession_Should_ChangeStatusOfSession_WhenUserHasPermission() {

    Session session = Mockito.mock(Session.class);
    when(session.isAdvised(any())).thenReturn(true);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_PROGRESS);
  }
}
