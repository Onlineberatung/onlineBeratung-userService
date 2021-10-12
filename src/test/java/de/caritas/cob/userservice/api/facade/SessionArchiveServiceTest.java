package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID_2;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.AuthenticatedUserHelper;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
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
  AuthenticatedUserHelper authenticatedUserHelper;

  @Test(expected = NotFoundException.class)
  public void archiveSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

    sessionArchiveService.archiveSession(SESSION_ID);
  }

  @Test(expected = ConflictException.class)
  public void archiveSession_Should_ThrowConflictException_WhenSessionShouldBeArchivedAndIsNotInProgress() {

    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(true);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);
    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.NEW);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);
    verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = ForbiddenException.class)
  public void archiveSession_Should_ThrowForbiddenException_WhenConsultantHasNoAuthorizationForTheSession() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(false);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);
    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = ForbiddenException.class)
  public void archiveSession_Should_ThrowForbiddenException_WhenNoConsultantRole() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(false);
    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {

    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(true);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = ForbiddenException.class)
  public void archiveSession_Should_ThrowForbiddenException_WhenUserHasNoAuthorizationForTheSession() {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.USER.getValue())).thenReturn(true);
    Session session = Mockito.mock(Session.class);
    User user = new User();
    user.setUserId(USER_ID_2);
    when(session.getUser()).thenReturn(user);

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenUserHasPermission() {

    User user = new User();
    user.setUserId(USER_ID);
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.USER.getValue())).thenReturn(true);

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(session.getUser()).thenReturn(user);

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.archiveSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = NotFoundException.class)
  public void reactivateSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
    sessionArchiveService.reactivateSession(SESSION_ID);
  }

  @Test(expected = ConflictException.class)
  public void reactivateSession_Should_ThrowConflictException_WhenSessionShouldBeReactivatedAndIsAlreadyInProgress() {

    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(true);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);
    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.reactivateSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);

  }

  @Test(expected = ForbiddenException.class)
  public void reactivateSession_Should_ThrowForbiddenException_WhenConsultantHasNoAuthorizationForTheSession() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(false);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);
    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.reactivateSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test(expected = ForbiddenException.class)
  public void reactivateSession_Should_ThrowForbiddenException_WhenSessionIsNotTeamSessionAndConsultantNotAssigned() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(false);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);

    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.reactivateSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test(expected = ForbiddenException.class)
  public void reactivateSession_Should_ThrowForbiddenException_WhenNoConsultantOrUserRole() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.reactivateSession(SESSION_ID);

    verify(session, times(0)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test
  public void reactivateSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {

    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(true);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_ARCHIVE);

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.reactivateSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_PROGRESS);
  }

  @Test(expected = ForbiddenException.class)
  public void reactivateSession_Should_ThrowForbiddenException_WhenUserHasNoAuthorizationForTheSession() {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.USER.getValue())).thenReturn(true);
    Session session = Mockito.mock(Session.class);
    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.reactivateSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_ARCHIVE);
  }

  @Test
  public void reactivateSession_Should_ChangeStatusOfSession_WhenUserHasPermission() {

    User user = new User();
    user.setUserId(USER_ID);
    when(authenticatedUser.getUserId()).thenReturn(user.getUserId());
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.USER.getValue())).thenReturn(true);

    Session session = Mockito.mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.IN_ARCHIVE);
    when(session.getUser()).thenReturn(user);

    when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

    sessionArchiveService.reactivateSession(SESSION_ID);

    verify(session, times(1)).setStatus(SessionStatus.IN_PROGRESS);
  }

}
