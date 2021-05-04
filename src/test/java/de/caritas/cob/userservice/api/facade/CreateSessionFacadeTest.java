package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.ExceptionConstants.CREATE_MONITORING_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.INTERNAL_SERVER_ERROR_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_U25;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_CHILDREN;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_PREGNANCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_CHILDREN;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_PREGNANCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_WITH_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_SESSION_RESPONSE_DTO_LIST_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_SESSION_RESPONSE_DTO_LIST_U25;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.CreateMonitoringException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.helper.AgencyVerifier;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.SessionDataService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CreateSessionFacadeTest {

  @InjectMocks
  private CreateSessionFacade createSessionFacade;
  @Mock
  private SessionService sessionService;
  @Mock
  private AgencyVerifier agencyVerifier;
  @Mock
  private MonitoringService monitoringService;
  @Mock
  private SessionDataService sessionDataService;
  @Mock
  private RollbackFacade rollbackFacade;
  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  /**
   * Method: createUserSession
   */

  @Test(expected = ConflictException.class)
  public void createUserSession_Should_ReturnConflict_When_AlreadyRegisteredToConsultingType() {

    when(sessionService.getSessionsForUserByConsultingType(any(), any()))
        .thenReturn(SESSION_LIST);

    createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_SUCHT);

    verify(sessionService, times(0)).saveSession(any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void createUserSession_Should_ReturnInternalServerErrorAndRollbackUserAccount_When_SessionCouldNotBeSaved() {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class), any()))
        .thenThrow(new InternalServerErrorException(MESSAGE));

    createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_SUCHT);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verify(rollbackFacade, times(1)).rollBackUserAccount(any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void createUserSession_Should_ReturnInternalServerErrorAndRollbackUserAccount_When_SessionDataCouldNotBeSaved() {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class), any()))
        .thenThrow(new InternalServerErrorException(MESSAGE));
    doThrow(INTERNAL_SERVER_ERROR_EXCEPTION).when(sessionDataService)
        .saveSessionData(any(Session.class), any());

    createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_SUCHT);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verify(sessionService, times(1)).deleteSession(any());
    verify(rollbackFacade, times(1)).rollBackUserAccount(any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void createUserSession_Should_ReturnInternalServerErrorAndRollbackUserAccount_When_MonitoringCouldNotBeSaved()
      throws CreateMonitoringException {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class), any()))
        .thenReturn(SESSION_WITH_CONSULTANT);
    doThrow(CREATE_MONITORING_EXCEPTION).when(monitoringService).createMonitoringIfConfigured(any(),
        any());

    createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_SUCHT);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verify(monitoringService, times(1)).rollbackInitializeMonitoring(any());
    verify(rollbackFacade, times(1)).rollBackUserAccount(any());
  }

  @Test(expected = BadRequestException.class)
  public void createUserSession_Should_ReturnBadRequest_When_AgencyForConsultingTypeCouldNotBeFound() {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT)).thenReturn(null);

    createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_SUCHT);
  }

  @Test
  public void createUserSession_Should_ReturnSessionId_OnSuccess() {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class), any()))
        .thenReturn(SESSION_WITHOUT_CONSULTANT);

    Long result = createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_SUCHT);

    assertEquals(SESSION_WITHOUT_CONSULTANT.getId(), result);
  }

  @Test
  public void createUserSession_Should_CreateSessionData() {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class), any()))
        .thenReturn(SESSION_WITHOUT_CONSULTANT);

    Long result = createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_SUCHT);

    assertEquals(SESSION_WITHOUT_CONSULTANT.getId(), result);
    verify(sessionDataService, times(1)).saveSessionData(any(Session.class),
        any());
  }

  @Test
  public void createUserSession_Should_CreateMonitoring_When_ConsultingTypeContainsMonitoring()
      throws CreateMonitoringException {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(agencyVerifier.getVerifiedAgency(USER_DTO_SUCHT.getAgencyId(), CONSULTING_TYPE_PREGNANCY))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class), any()))
        .thenReturn(SESSION_WITHOUT_CONSULTANT);

    Long result = createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_PREGNANCY);

    assertEquals(SESSION_WITHOUT_CONSULTANT.getId(), result);
    verify(monitoringService, times(1)).createMonitoringIfConfigured(any(), any());
  }

  @Test
  public void createUserSession_ShouldNot_CreateMonitoring_When_ConsultingTypeDoesNotContainMonitoring() {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_SUCHT);
    when(agencyVerifier.getVerifiedAgency(USER_DTO_SUCHT.getAgencyId(), CONSULTING_TYPE_CHILDREN))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class), any()))
        .thenReturn(SESSION_WITHOUT_CONSULTANT);

    Long result = createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_CHILDREN);

    assertEquals(SESSION_WITHOUT_CONSULTANT.getId(), result);
    verify(monitoringService, times(0)).updateMonitoring(any(), any());
  }
}
