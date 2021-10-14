package de.caritas.cob.userservice.api.service.archive;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID_2;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.archive.SessionArchivePermissionChecker;
import de.caritas.cob.userservice.api.service.archive.SessionArchiveService;
import de.caritas.cob.userservice.api.service.archive.SessionArchiveValidator;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionArchiveServiceTest {

  @InjectMocks
  SessionArchiveService sessionArchiveService;
  @Mock
  SessionRepository sessionRepository;
  @Mock
  ConsultantAgencyRepository consultantAgencyRepository;
  @Mock
  AuthenticatedUser authenticatedUser;
  @Mock
  SessionArchivePermissionChecker sessionArchivePermissionChecker;
  @Mock
  SessionArchiveValidator sessionArchiveValidator;
  @Mock
  RocketChatService rocketChatService;

  @Test(expected = NotFoundException.class)
  public void archiveSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

    sessionArchiveService.archiveSession(SESSION_ID);
  }

  @Test(expected = ConflictException.class)
  public void archiveSession_Should_ThrowConflictException_WhenSessionShouldBeArchivedAndIsNotInProgress() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    doThrow(new ConflictException("Conflict"))
        .when(sessionArchiveValidator)
        .isValidForArchiving(session);

    sessionArchiveService.archiveSession(SESSION_ID);
    verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = ForbiddenException.class)
  public void archiveSession_Should_ThrowForbiddenException_WhenConsultantHasaNoAuthorizationForTheSession() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    doThrow(new ForbiddenException("Forbidden"))
        .when(sessionArchivePermissionChecker)
        .checkPermission(session);

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = ForbiddenException.class)
  public void archiveSession_Should_ThrowForbiddenException_WhenUserHasNoAuthorizationForTheSession() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    doThrow(new ForbiddenException("Forbidden"))
        .when(sessionArchivePermissionChecker)
        .checkPermission(session);

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenUserHasPermission() {

    Session session = Mockito.mock(Session.class);
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
  public void dearchiveSession_Should_ThrowConflictException_WhenSessionShouldBeReactivatedAndIsAlreadyInProgress() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    doThrow(new ConflictException("Conflict"))
        .when(sessionArchiveValidator)
        .isValidForDearchiving(session);

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test(expected = ForbiddenException.class)
  public void dearchiveSession_Should_ThrowForbiddenException_WhenConsultantHasNoAuthorizationForTheSession() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    doThrow(new ForbiddenException("Forbidden"))
        .when(sessionArchivePermissionChecker)
        .checkPermission(session);

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test(expected = ForbiddenException.class)
  public void dearchiveSession_Should_ThrowForbiddenException_WhenSessionIsNotTeamSessionAndConsultantNotAssigned() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    doThrow(new ForbiddenException("Forbidden"))
        .when(sessionArchivePermissionChecker)
        .checkPermission(session);

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = ForbiddenException.class)
  public void dearchiveSession_Should_ThrowForbiddenException_WhenNoConsultantOrUserRole() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    doThrow(new ForbiddenException("Forbidden"))
        .when(sessionArchiveValidator)
        .isValidForDearchiving(session);

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test
  public void dearchiveSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test(expected = ForbiddenException.class)
  public void dearchiveSession_Should_ThrowForbiddenException_WhenUserHasNoAuthorizationForTheSession() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
    doThrow(new ForbiddenException("Forbidden"))
        .when(sessionArchiveValidator)
        .isValidForDearchiving(session);

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test
  public void dearchiveSession_Should_ChangeStatusOfSession_WhenUserHasPermission() {

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.dearchiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_PROGRESS);
  }

}
