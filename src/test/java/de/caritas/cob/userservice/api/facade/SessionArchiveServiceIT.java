package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
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

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenConsultantIsAssigned() {

    when(authenticatedUser.getUserId()).thenReturn("473f7c4b-f011-4fc2-847c-ceb636a5b399");

    sessionArchiveService.archiveSession(1L);

    Optional<Session> session = sessionRepository.findById(1L);
    assertThat(session.get().getStatus(), is(SessionStatus.IN_ARCHIVE));

  }

  @Test
  public void archiveSession_Should_ChangeStatusOfSession_WhenIsTeamSessionAndConsultantIsInAgency() {

    when(authenticatedUser.getUserId()).thenReturn("e2f20d3a-1ca7-4cb5-9fac-8e26033416b3");

    sessionArchiveService.archiveSession(2L);

    Optional<Session> session = sessionRepository.findById(2L);
    assertThat(session.get().getStatus(), is(SessionStatus.IN_ARCHIVE));

  }

  @Test(expected = NotFoundException.class)
  public void archiveSession_Should_ThrowNotFoundException_WhenSessionIsNotFound() {
    sessionArchiveService.archiveSession(99999999L);
  }

  @Test(expected = ForbiddenException.class)
  public void archiveSession_Should_ThrowForbiddenException_WhenSessionIsNotTeamSessionAndConsultantNotAssigned() {

    when(authenticatedUser.getUserId()).thenReturn("88613f5d-0d40-47e0-b323-e792e7fba3ed");
    sessionArchiveService.archiveSession(1L);

    verify(sessionRepository, times(0)).save(any());
  }

  @Test(expected = ConflictException.class)
  public void archiveSession_Should_ThrowConflictException_WhenSessionIsNotInProgress() {
    when(authenticatedUser.getUserId()).thenReturn("88613f5d-0d40-47e0-b323-e792e7fba3ed");
    sessionArchiveService.archiveSession(200L);
  }

}
