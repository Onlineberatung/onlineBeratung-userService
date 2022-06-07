package de.caritas.cob.userservice.api.service.archive;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
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
  ConsultantAgencyService consultantAgencyService;
  @Mock
  Session session;

  @Test
  void checkPermission_Should_ThrowForbiddenException_When_UserHasNeitherConsultantNorUserRole() {
    assertThrows(ForbiddenException.class,
        () -> sessionArchivePermissionChecker.checkPermission(session));
  }

  @Test
  void checkPermission_Should_ThrowForbiddenException_When_UserHasConsultantRoleButNoPermission() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(session.getAgencyId()).thenReturn(AGENCY_ID);
    when(session.isAdvised(USER_ID)).thenReturn(false);
    when(session.isTeamSession()).thenReturn(true);
    when(consultantAgencyService.isConsultantInAgency(anyString(), anyLong())).thenReturn(false);
    assertThrows(ForbiddenException.class,
        () -> sessionArchivePermissionChecker.checkPermission(session));
  }

  @Test
  void checkPermission_Should_ThrowForbiddenException_When_UserHasUserRoleButNoPermission() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    assertThrows(ForbiddenException.class,
        () -> sessionArchivePermissionChecker.checkPermission(session));
  }

  @Test
  void checkPermission_Should_Not_ThrowForbiddenException_WhenUserHasConsultantRoleAndPermissionForSession() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(session.getAgencyId()).thenReturn(AGENCY_ID);
    when(session.isTeamSession()).thenReturn(true);
    when(consultantAgencyService.isConsultantInAgency(anyString(), anyLong())).thenReturn(true);
    assertDoesNotThrow(() -> sessionArchivePermissionChecker.checkPermission(session));
  }

  @Test
  void checkPermission_Should_Not_ThrowForbiddenException_WhenUserHasUserRoleAndPermissionForSession() {
    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(session.isAdvised(USER_ID)).thenReturn(true);
    assertDoesNotThrow(() -> sessionArchivePermissionChecker.checkPermission(session));
  }

}
