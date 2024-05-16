package de.caritas.cob.userservice.api.facade;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.archive.SessionArchiveService;
import de.caritas.cob.userservice.api.testConfig.ConsultingTypeManagerTestConfig;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Import({ConsultingTypeManagerTestConfig.class})
public class SessionArchiveServiceIT {

  @Autowired SessionArchiveService sessionArchiveService;
  @Autowired SessionRepository sessionRepository;
  @MockBean AuthenticatedUser authenticatedUser;
  @MockBean RocketChatService rocketChatService;

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {

    when(authenticatedUser.getUserId()).thenReturn("e2f20d3a-1ca7-4cb5-9fac-8e26033416b3");
    when(authenticatedUser.isConsultant()).thenReturn(true);

    sessionArchiveService.archiveSession(2L);

    Optional<Session> session = sessionRepository.findById(2L);
    assert session.isPresent();
    assertThat(session.get().getStatus(), is(SessionStatus.IN_ARCHIVE));
  }

  @Test
  public void archiveSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {
    assertThrows(
        NotFoundException.class,
        () -> {
          sessionArchiveService.archiveSession(99999999L);
        });
  }

  @Test
  public void archiveSession_Should_ThrowForbiddenException_WhenConsultantHasNoPermission() {
    assertThrows(
        ForbiddenException.class,
        () -> {
          when(authenticatedUser.getUserId()).thenReturn("88613f5d-0d40-47e0-b323-e792e7fba3ed");
          when(authenticatedUser.isConsultant()).thenReturn(true);
          sessionArchiveService.archiveSession(1L);

          verify(sessionRepository, times(0)).save(any());
        });
  }

  @Test
  public void archiveSession_Should_ThrowForbiddenException_WhenUserHasNoPermission() {
    assertThrows(
        ForbiddenException.class,
        () -> {
          when(authenticatedUser.getUserId()).thenReturn("88613f5d-0d40-47e0-b323-e792e7fba3ed");
          when(authenticatedUser.isAdviceSeeker()).thenReturn(true);
          sessionArchiveService.archiveSession(1L);

          verify(sessionRepository, times(0)).save(any());
        });
  }

  @Test
  public void archiveSession_Should_ThrowConflictException_WhenSessionIsNotInProgress() {
    assertThrows(
        ConflictException.class,
        () -> {
          when(authenticatedUser.getUserId()).thenReturn("75abe824-fb42-476d-a52a-66660113bdcc");

          sessionArchiveService.archiveSession(1214L);
        });
  }

  @Test
  public void reactivateSession_Should_ChangeStatusOfSession_WhenConsultantHasPermission() {

    when(authenticatedUser.getUserId()).thenReturn("75abe824-fb42-476d-a52a-66660113bdcc");
    when(authenticatedUser.isConsultant()).thenReturn(true);

    sessionArchiveService.dearchiveSession(1209L);

    Optional<Session> session = sessionRepository.findById(1209L);
    assert session.isPresent();
    assertThat(session.get().getStatus(), is(SessionStatus.IN_PROGRESS));
  }

  @Test
  public void reactivateSession_Should_ChangeStatusOfSession_WhenUserHasPermission() {

    when(authenticatedUser.getUserId()).thenReturn("236b97bf-6cd7-434a-83f3-0a0b129dd45a");
    when(authenticatedUser.isAdviceSeeker()).thenReturn(true);

    sessionArchiveService.dearchiveSession(1211L);

    Optional<Session> session = sessionRepository.findById(1211L);
    assert session.isPresent();
    assertThat(session.get().getStatus(), is(SessionStatus.IN_PROGRESS));
  }

  @Test
  public void reactivateSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {
    assertThrows(
        NotFoundException.class,
        () -> {
          sessionArchiveService.dearchiveSession(99999999L);
        });
  }

  @Test
  public void reactivateSession_Should_ThrowForbiddenException_WhenUserHasNoPermission() {
    assertThrows(
        ForbiddenException.class,
        () -> {
          when(authenticatedUser.getUserId()).thenReturn(UUID.randomUUID().toString());
          sessionArchiveService.dearchiveSession(1210L);
        });
  }

  @Test
  public void reactivateSession_Should_ThrowConflictException_WhenSessionIsNotInArchive() {
    assertThrows(
        ConflictException.class,
        () -> {
          when(authenticatedUser.getUserId()).thenReturn("473f7c4b-f011-4fc2-847c-ceb636a5b399");
          when(authenticatedUser.isConsultant()).thenReturn(true);
          sessionArchiveService.dearchiveSession(1L);
        });
  }
}
