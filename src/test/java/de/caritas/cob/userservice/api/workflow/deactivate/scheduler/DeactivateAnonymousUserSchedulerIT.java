package de.caritas.cob.userservice.api.workflow.deactivate.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.conversation.facade.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.api.testConfig.ApiControllerTestConfig;
import de.caritas.cob.userservice.api.testConfig.KeycloakTestConfig;
import de.caritas.cob.userservice.api.testConfig.RocketChatTestConfig;
import de.caritas.cob.userservice.api.testConfig.TestAgencyControllerApi;
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
@Import({KeycloakTestConfig.class, RocketChatTestConfig.class, ApiControllerTestConfig.class})
class DeactivateAnonymousUserSchedulerIT {

  @Autowired private DeactivateAnonymousUserScheduler deactivateAnonymousUserScheduler;

  @Autowired private CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;

  @Autowired private SessionRepository sessionRepository;

  @Autowired private UserService userService;

  @Autowired private ActionsRegistry actionsRegistry;

  @MockBean AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @Value("${user.anonymous.deactivateworkflow.periodMinutes}")
  private long deactivatePeriodInMinutes;

  private Session currentSession;

  @BeforeEach
  public void setup() {
    when(agencyServiceApiControllerFactory.createControllerApi())
        .thenReturn(
            new TestAgencyControllerApi(
                new de.caritas.cob.userservice.agencyserivce.generated.ApiClient()));
    var createAnonymousEnquiryDTO = new CreateAnonymousEnquiryDTO().consultingType(12);
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
  void performDeactivationWorkflow_Should_notDeleteUser_When_SessionIsNotInProgress() {
    var currentStatus = currentSession.getStatus();
    deactivateAnonymousUserScheduler.performDeactivationWorkflow();

    assertSessionAndUserArePresent(currentSession.getId());
    var sessionFromDb = sessionRepository.findById(currentSession.getId());
    assertEquals(currentSession, sessionFromDb.get());
  }

  private void assertSessionAndUserArePresent(long sessionId) {
    var sessionOptional = sessionRepository.findById(sessionId);
    assertTrue(sessionOptional.isPresent());

    var userOptional = userService.getUser(sessionOptional.get().getUser().getUserId());
    assertTrue(userOptional.isPresent());
  }

  @Test
  void performDeletionWorkflow_Should_putSessionToDone_When_SessionAreDoneWithinDeletionPeriod() {
    currentSession.setStatus(SessionStatus.IN_PROGRESS);
    var oneMinuteBeforeDeletionPeriodIsOver =
        LocalDateTime.now().minusMinutes(deactivatePeriodInMinutes).plusMinutes(1L);
    currentSession.setUpdateDate(oneMinuteBeforeDeletionPeriodIsOver);
    sessionRepository.save(currentSession);

    deactivateAnonymousUserScheduler.performDeactivationWorkflow();

    assertSessionAndUserArePresent(currentSession.getId());
    var sessionFromDb = sessionRepository.findById(currentSession.getId());
    assertEquals(currentSession, sessionFromDb.get());
  }

  @Test
  void
      performDeactivationWorkflow_Should_deleteUser_When_UserSessionIsDoneAndOutsideOfDeletionPeriod() {
    prepareCurrentSessionForDeactivation();

    deactivateAnonymousUserScheduler.performDeactivationWorkflow();

    assertSessionAndUserArePresent(currentSession.getId());
    var sessionFromDb = sessionRepository.findById(currentSession.getId());
    assertEquals(SessionStatus.DONE, sessionFromDb.get().getStatus());
  }

  private void prepareCurrentSessionForDeactivation() {
    currentSession.setStatus(SessionStatus.IN_PROGRESS);
    var timeToDeactivation = LocalDateTime.now().minusMinutes(deactivatePeriodInMinutes);
    currentSession.setUpdateDate(timeToDeactivation);
    sessionRepository.save(currentSession);
  }
}
