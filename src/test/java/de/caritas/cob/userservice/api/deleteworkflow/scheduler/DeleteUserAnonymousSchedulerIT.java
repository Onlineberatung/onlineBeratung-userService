package de.caritas.cob.userservice.api.deleteworkflow.scheduler;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_ID_AIDS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.conversation.facade.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetUserIdException;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.testConfig.ApiControllerTestConfig;
import de.caritas.cob.userservice.testConfig.ConsultingTypeManagerTestConfig;
import de.caritas.cob.userservice.testConfig.KeycloakTestConfig;
import de.caritas.cob.userservice.testConfig.RocketChatTestConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Import({KeycloakTestConfig.class, RocketChatTestConfig.class, ApiControllerTestConfig.class,
    ConsultingTypeManagerTestConfig.class})
class DeleteUserAnonymousSchedulerIT {

  @Autowired
  private DeleteUserAnonymousScheduler deleteUserAnonymousScheduler;

  @Autowired
  private CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;

  @Autowired
  private SessionRepository sessionRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @Value("${user.anonymous.deleteworkflow.periodMinutes}")
  private long deletionPeriodInMinutes;

  private Session currentSession;

  @BeforeEach
  public void setup() throws RocketChatGetUserIdException {
    var createAnonymousEnquiryDTO = new CreateAnonymousEnquiryDTO()
        .consultingType(CONSULTING_TYPE_ID_AIDS);
    var responseDTO =
        createAnonymousEnquiryFacade.createAnonymousEnquiry(createAnonymousEnquiryDTO);

    var sessionOptional = sessionRepository.findById(responseDTO.getSessionId());
    currentSession = sessionOptional.get();
  }

  @AfterEach
  public void cleanDatabase() {
    this.sessionRepository.deleteAll();
  }

  @Test
  void performDeletionWorkflow_Should_notDeleteUser_When_SessionIsNotDone() {

    deleteUserAnonymousScheduler.performDeletionWorkflow();

    assertSessionAndUserArePresent(currentSession.getId());
  }

  private void assertSessionAndUserArePresent(long sessionId) {
    var sessionOptional = sessionRepository.findById(sessionId);
    assertTrue(sessionOptional.isPresent());

    var userOptional = userService.getUser(sessionOptional.get().getUser().getUserId());
    assertTrue(userOptional.isPresent());
  }

  @Test
  void performDeletionWorkflow_Should_notDeleteUser_When_SessionAreDoneWithinDeletionPeriod() {
    currentSession.setStatus(SessionStatus.DONE);
    var oneMinuteBeforeDeletionPeriodIsOver = LocalDateTime
        .now()
        .minusMinutes(deletionPeriodInMinutes)
        .plusMinutes(1L);
    currentSession.setUpdateDate(oneMinuteBeforeDeletionPeriodIsOver);
    sessionRepository.save(currentSession);

    deleteUserAnonymousScheduler.performDeletionWorkflow();

    assertSessionAndUserArePresent(currentSession.getId());
  }

  @Test
  void performDeletionWorkflow_Should_deleteUser_When_UserSessionIsDoneAndOutsideOfDeletionPeriod() {
    prepareCurrentSessionForDeletion();

    deleteUserAnonymousScheduler.performDeletionWorkflow();

    assertSessionAndUserDoNotExistInDatabase(currentSession.getId(),
        currentSession.getUser().getUserId());
  }

  private void prepareCurrentSessionForDeletion() {
    currentSession.setStatus(SessionStatus.DONE);
    var oneMinuteBeforeDeletionPeriodIsOver = LocalDateTime
        .now()
        .minusMinutes(deletionPeriodInMinutes);
    currentSession.setUpdateDate(oneMinuteBeforeDeletionPeriodIsOver);
    sessionRepository.save(currentSession);
  }

  private void assertSessionAndUserDoNotExistInDatabase(Long sessionId, String userId) {
    var sessionOptional = sessionRepository.findById(sessionId);
    assertFalse(sessionOptional.isPresent());

    var userOptional = userService.getUser(userId);
    assertFalse(userOptional.isPresent());
  }

  @Test
  void performDeletionWorkflow_Should_deleteUser_When_UserSessionsAreNull() {
    currentSession.getUser().setSessions(null);
    userRepository.save(currentSession.getUser());
    prepareCurrentSessionForDeletion();

    deleteUserAnonymousScheduler.performDeletionWorkflow();

    assertSessionAndUserDoNotExistInDatabase(currentSession.getId(),
        currentSession.getUser().getUserId());
  }
}
