package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.AuthenticatedUserHelper;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.testConfig.ConsultingTypeManagerTestConfig;
import java.util.Optional;
import org.commonmark.renderer.text.TextContentRenderer.TextContentRendererExtension;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Import({ConsultingTypeManagerTestConfig.class})
public class SessionArchiveServiceIT {

  @Autowired
  SessionArchiveService sessionArchiveService;
  @Autowired
  SessionRepository sessionRepository;
  @MockBean
  AuthenticatedUser authenticatedUser;
  @MockBean
  AuthenticatedUserHelper authenticatedUserHelper;

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {

    when(authenticatedUser.getUserId()).thenReturn("e2f20d3a-1ca7-4cb5-9fac-8e26033416b3");
    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(true);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);

    sessionArchiveService.archiveSession(2L);

    Optional<Session> session = sessionRepository.findById(2L);
    assert session.isPresent();
    assertThat(session.get().getStatus(), is(SessionStatus.IN_ARCHIVE));

  }

  @Test(expected = NotFoundException.class)
  public void archiveSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {
    sessionArchiveService.archiveSession(99999999L);
  }

  @Test(expected = ForbiddenException.class)
  public void archiveSession_Should_ThrowForbiddenException_WhenConsultantHasNoPermission() {

    when(authenticatedUser.getUserId()).thenReturn("88613f5d-0d40-47e0-b323-e792e7fba3ed");
    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(false);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);
    sessionArchiveService.archiveSession(1L);

    verify(sessionRepository, times(0)).save(any());
  }

  @Test(expected = ForbiddenException.class)
  public void archiveSession_Should_ThrowForbiddenException_WhenUserHasNoPermission() {

    when(authenticatedUser.getUserId()).thenReturn("88613f5d-0d40-47e0-b323-e792e7fba3ed");
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.USER.getValue())).thenReturn(true);
    sessionArchiveService.archiveSession(1L);

    verify(sessionRepository, times(0)).save(any());
  }

  @Test(expected = ConflictException.class)
  public void archiveSession_Should_ThrowConflictException_WhenSessionIsNotInProgress() {

    when(authenticatedUser.getUserId()).thenReturn("88613f5d-0d40-47e0-b323-e792e7fba3ed");
    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(true);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);

    sessionArchiveService.archiveSession(200L);
  }

  @Test
  public void reactivateSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {

    when(authenticatedUser.getUserId()).thenReturn("75abe824-fb42-476d-a52a-66660113bdcc");
    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(true);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);

    sessionArchiveService.reactivateSession(1209L);

    Optional<Session> session = sessionRepository.findById(1209L);
    assert session.isPresent();
    assertThat(session.get().getStatus(), is(SessionStatus.IN_PROGRESS));
  }

  @Test
  public void reactivateSession_Should_ChangeStatusOfSession_WhenUserHasPermission() {

    when(authenticatedUser.getUserId()).thenReturn("236b97bf-6cd7-434a-83f3-0a0b129dd45a");
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.USER.getValue())).thenReturn(true);

    sessionArchiveService.reactivateSession(1211L);

    Optional<Session> session = sessionRepository.findById(1211L);
    assert session.isPresent();
    assertThat(session.get().getStatus(), is(SessionStatus.IN_PROGRESS));
  }

  @Test(expected = NotFoundException.class)
  public void reactivateSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {
    sessionArchiveService.reactivateSession(99999999L);
  }

  @Test(expected = ForbiddenException.class)
  public void reactivateSession_Should_ThrowForbiddenException_WhenConsultantHasNoPermission() {

    when(authenticatedUser.getUserId()).thenReturn("94c3e0b1-0677-4fd2-a7ea-56a71aefd0e8");
    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(false);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);
    sessionArchiveService.reactivateSession(1210L);

    verify(sessionRepository, times(0)).save(any());
  }

  @Test(expected = ForbiddenException.class)
  public void reactivateSession_Should_ThrowForbiddenException_WhenUserHasNoPermission() {

    when(authenticatedUser.getUserId()).thenReturn("94c3e0b1-0677-4fd2-a7ea-56a71aefd0e8");
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.USER.getValue())).thenReturn(true);
    sessionArchiveService.reactivateSession(1210L);

    verify(sessionRepository, times(0)).save(any());
  }

  @Test(expected = ConflictException.class)
  public void reactivateSession_Should_ThrowConflictException_WhenSessionIsNotInArchive() {
    when(authenticatedUser.getUserId()).thenReturn("473f7c4b-f011-4fc2-847c-ceb636a5b399");
    when(authenticatedUserHelper.hasPermissionForSession(any())).thenReturn(true);
    when(authenticatedUserHelper.authenticatedUserRolesContainAnyRoleOf(UserRole.CONSULTANT.getValue())).thenReturn(true);
    sessionArchiveService.reactivateSession(1L);
  }

}
