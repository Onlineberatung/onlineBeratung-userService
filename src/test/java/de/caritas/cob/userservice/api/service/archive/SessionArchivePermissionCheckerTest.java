package de.caritas.cob.userservice.api.service.archive;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID_2;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.AuthenticatedUserHelper;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionArchivePermissionCheckerTest {

  @InjectMocks
  SessionArchivePermissionChecker sessionArchivePermissionChecker;
  @Mock
  AuthenticatedUser authenticatedUser;
  @Mock
  AuthenticatedUserHelper authenticatedUserHelper;
  @Mock
  Session session;
  @Mock
  User user;

  @Test
  void checkPermission_Should_ThrowForbiddenException_When_UserHasNeitherConsultantNorUserRole() {

    when(authenticatedUser.isAdviceSeeker()).thenReturn(false);
    when(authenticatedUser.isConsultant()).thenReturn(false);

    assertThrows(ForbiddenException.class,
        () -> sessionArchivePermissionChecker.checkPermission(session));
  }

  @Test
  void checkPermission_Should_ThrowForbiddenException_When_UserHasConsultantRoleButNoPermission() {
    when(authenticatedUser.isConsultant()).thenReturn(true);
    when(authenticatedUser.isAdviceSeeker()).thenReturn(false);
    when(authenticatedUserHelper.hasPermissionForSession(session)).thenReturn(false);
    assertThrows(ForbiddenException.class,
        () -> sessionArchivePermissionChecker.checkPermission(session));
  }

  @Test
  void checkPermission_Should_ThrowForbiddenException_When_UserHasUserRoleButNoPermission() {
    when(authenticatedUser.isConsultant()).thenReturn(false);
    when(authenticatedUser.isAdviceSeeker()).thenReturn(true);
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(user.getUserId()).thenReturn(USER_ID_2);
    when(session.getUser()).thenReturn(user);
    assertThrows(ForbiddenException.class,
        () -> sessionArchivePermissionChecker.checkPermission(session));
  }

  @Test
  void checkPermission_Should_Not_ThrowForbiddenException_WhenUserHasConsultantRoleAndPermissionForSession() {
    when(authenticatedUser.isConsultant()).thenReturn(true);
    when(authenticatedUserHelper.hasPermissionForSession(session)).thenReturn(true);
    assertDoesNotThrow(() -> sessionArchivePermissionChecker.checkPermission(session));
  }

  @Test
  void checkPermission_Should_Not_ThrowForbiddenException_WhenUserHasUserRoleAndPermissionForSession() {
    when(authenticatedUser.isConsultant()).thenReturn(false);
    when(authenticatedUser.isAdviceSeeker()).thenReturn(true);
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(user.getUserId()).thenReturn(USER_ID);
    when(session.getUser()).thenReturn(user);
    assertDoesNotThrow(() -> sessionArchivePermissionChecker.checkPermission(session));
  }

}
