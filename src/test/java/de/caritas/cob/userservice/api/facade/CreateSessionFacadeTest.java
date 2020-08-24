package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.ExceptionConstants.CREATE_MONITORING_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.SERVICE_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_DTO_U25;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_U25;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_U25;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.NEW_REGISTRATION_DTO_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.NEW_REGISTRATION_DTO_U25;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_WITH_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_SESSION_RESPONSE_DTO_LIST_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_SESSION_RESPONSE_DTO_LIST_U25;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.CreateMonitoringException;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.helper.AgencyHelper;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.NewRegistrationResponseDto;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.SessionDataService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.UserService;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class CreateSessionFacadeTest {

  @InjectMocks
  private CreateSessionFacade createSessionFacade;
  @Mock
  private UserService userService;
  @Mock
  private SessionService sessionService;
  @Mock
  private ConsultingTypeManager consultingTypeManager;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private AgencyHelper agencyHelper;
  @Mock
  private MonitoringService monitoringService;
  @Mock
  private SessionDataService sessionDataService;
  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  /**
   * Method: createSession
   */

  @Test
  public void createSession_Should_ReturnConflict_When_AlreadyRegisteredToConsultingType() {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(sessionService.getSessionsForUserByConsultingType(Mockito.any(), Mockito.any()))
        .thenReturn(SESSION_LIST);

    NewRegistrationResponseDto result =
        createSessionFacade.createSession(NEW_REGISTRATION_DTO_SUCHT, USER);

    assertEquals(HttpStatus.CONFLICT, result.getStatus());
    verify(sessionService, times(0)).saveSession(Mockito.any());
  }

  @Test
  public void createSession_Should_ReturnInternalServerError_When_SessionCouldNotBeSaved() {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(userService.getUserViaAuthenticatedUser(Mockito.any())).thenReturn(Optional.of(USER));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);
    when(agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.saveSession(Mockito.any())).thenThrow(new ServiceException(MESSAGE));

    NewRegistrationResponseDto result =
        createSessionFacade.createSession(NEW_REGISTRATION_DTO_SUCHT, USER);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void createSession_Should_ReturnInternalServerError_When_SessionDataCouldNotBeSaved() {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(userService.getUserViaAuthenticatedUser(Mockito.any())).thenReturn(Optional.of(USER));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);
    when(agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.saveSession(Mockito.any())).thenReturn(SESSION_WITH_CONSULTANT);
    when(sessionDataService.saveSessionDataFromRegistration(Mockito.any(), Mockito.any()))
        .thenThrow(SERVICE_EXCEPTION);

    NewRegistrationResponseDto result =
        createSessionFacade.createSession(NEW_REGISTRATION_DTO_SUCHT, USER);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verify(sessionService, times(1)).deleteSession(Mockito.any());
  }

  @Test
  public void createSession_Should_ReturnInternalServerError_When_MonitoringCouldNotBeSaved()
      throws CreateMonitoringException {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(userService.getUserViaAuthenticatedUser(Mockito.any())).thenReturn(Optional.of(USER));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);
    when(agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.saveSession(Mockito.any())).thenReturn(SESSION_WITH_CONSULTANT);
    doThrow(CREATE_MONITORING_EXCEPTION).when(monitoringService).createMonitoring(Mockito.any(),
        Mockito.any());

    NewRegistrationResponseDto result =
        createSessionFacade.createSession(NEW_REGISTRATION_DTO_SUCHT, USER);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verify(monitoringService, times(1)).rollbackInitializeMonitoring(Mockito.any());
  }

  @Test
  public void createSession_Should_ReturnBadRequest_When_AgencyForConsultingTypeCouldNotBeFound()
      throws CreateMonitoringException {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(userService.getUserViaAuthenticatedUser(Mockito.any())).thenReturn(Optional.of(USER));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);
    when(agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT)).thenReturn(null);

    NewRegistrationResponseDto result =
        createSessionFacade.createSession(NEW_REGISTRATION_DTO_SUCHT, USER);

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  public void createSession_Should_ReturnCreatedAndSessionId_OnSuccess() {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(userService.getUserViaAuthenticatedUser(Mockito.any())).thenReturn(Optional.of(USER));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);
    when(agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.saveSession(Mockito.any())).thenReturn(SESSION_WITH_CONSULTANT);

    NewRegistrationResponseDto result =
        createSessionFacade.createSession(NEW_REGISTRATION_DTO_SUCHT, USER);

    assertEquals(HttpStatus.CREATED, result.getStatus());
    assertNotNull(result.getSessionId());
  }

  @Test
  public void createSession_Should_CreateSessionData() throws CreateMonitoringException {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(userService.getUserViaAuthenticatedUser(Mockito.any())).thenReturn(Optional.of(USER));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);
    when(agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.saveSession(Mockito.any())).thenReturn(SESSION_WITH_CONSULTANT);

    NewRegistrationResponseDto result =
        createSessionFacade.createSession(NEW_REGISTRATION_DTO_SUCHT, USER);

    assertEquals(HttpStatus.CREATED, result.getStatus());
    verify(sessionDataService, times(1)).saveSessionDataFromRegistration(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void createSession_Should_CreateMonitoring_When_ConsultingTypeContainsMonitoring()
      throws CreateMonitoringException {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(userService.getUserViaAuthenticatedUser(Mockito.any())).thenReturn(Optional.of(USER));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);
    when(agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_SUCHT))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.saveSession(Mockito.any())).thenReturn(SESSION_WITH_CONSULTANT);

    NewRegistrationResponseDto result =
        createSessionFacade.createSession(NEW_REGISTRATION_DTO_SUCHT, USER);

    assertEquals(HttpStatus.CREATED, result.getStatus());
    verify(monitoringService, times(1)).createMonitoring(Mockito.any(), Mockito.any());
  }

  @Test
  public void createSession_ShouldNot_CreateMonitoring_When_ConsultingTypeDoesNotContainsMonitoring()
      throws CreateMonitoringException {

    when(authenticatedUser.getUserId()).thenReturn(USER_ID);
    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_SUCHT);
    when(userService.getUserViaAuthenticatedUser(Mockito.any())).thenReturn(Optional.of(USER));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);
    when(agencyHelper.getVerifiedAgency(AGENCY_ID, CONSULTING_TYPE_U25)).thenReturn(AGENCY_DTO_U25);
    when(sessionService.saveSession(Mockito.any())).thenReturn(SESSION_WITH_CONSULTANT);

    NewRegistrationResponseDto result =
        createSessionFacade.createSession(NEW_REGISTRATION_DTO_U25, USER);

    assertEquals(HttpStatus.CREATED, result.getStatus());
    verify(monitoringService, times(0)).updateMonitoring(Mockito.any(), Mockito.any());
  }
}
