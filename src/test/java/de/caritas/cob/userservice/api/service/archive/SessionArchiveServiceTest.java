package de.caritas.cob.userservice.api.service.archive;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.AccountManager;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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

  @Test(expected = NotFoundException.class)
  public void archiveSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

    sessionArchiveService.archiveSession(SESSION_ID);
  }

  @Test(expected = ConflictException.class)
  public void
      archiveSession_Should_ThrowConflictException_WhenSessionShouldBeArchivedAndIsNotInProgress() {

    Session session = Mockito.mock(Session.class);
    when(session.isAdvised(any())).thenReturn(true);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    doThrow(new ConflictException("Conflict"))
        .when(sessionArchiveValidator)
        .isValidForArchiving(session);

    sessionArchiveService.archiveSession(SESSION_ID);
    verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = ForbiddenException.class)
  public void
      archiveSession_Should_ThrowForbiddenException_WhenConsultantHasaNoAuthorizationForTheSession() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {

    Session session = Mockito.mock(Session.class);
    when(session.isAdvised(any())).thenReturn(true);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = ForbiddenException.class)
  public void
      archiveSession_Should_ThrowForbiddenException_WhenUserHasNoAuthorizationForTheSession() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenUserHasPermission() {

    Session session = Mockito.mock(Session.class);
    when(session.isAdvised(any())).thenReturn(true);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = NotFoundException.class)
  public void dearchiveSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
    sessionArchiveService.dearchiveSession(SESSION_ID);
  }

  @Test(expected = ConflictException.class)
  public void
      dearchiveSession_Should_ThrowConflictException_WhenSessionShouldBeReactivatedAndIsAlreadyInProgress() {

    Session session = Mockito.mock(Session.class);
    when(session.isAdvised(any())).thenReturn(true);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    doThrow(new ConflictException("Conflict"))
        .when(sessionArchiveValidator)
        .isValidForDearchiving(session);

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test(expected = ForbiddenException.class)
  public void
      dearchiveSession_Should_ThrowForbiddenException_WhenConsultantHasNoAuthorizationForTheSession() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test(expected = ForbiddenException.class)
  public void
      dearchiveSession_Should_ThrowForbiddenException_WhenSessionIsNotTeamSessionAndConsultantNotAssigned() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = ForbiddenException.class)
  public void dearchiveSession_Should_ThrowForbiddenException_WhenNoConsultantOrUserRole() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test
  public void dearchiveSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {
    Session session = Mockito.mock(Session.class);
    when(session.isAdvised(any())).thenReturn(true);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test(expected = ForbiddenException.class)
  public void
      dearchiveSession_Should_ThrowForbiddenException_WhenUserHasNoAuthorizationForTheSession() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
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
