package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.testHelper.ExceptionConstants.CREATE_MONITORING_EXCEPTION;
import static de.caritas.cob.userservice.api.testHelper.ExceptionConstants.INTERNAL_SERVER_ERROR_EXCEPTION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_DTO_U25;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_CHILDREN;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_PREGNANCY;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_WITH_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_DTO_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_SESSION_RESPONSE_DTO_LIST_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_SESSION_RESPONSE_DTO_LIST_U25;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.SessionDataService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
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
  private ValidatedUserAccountProvider userAccountProvider;
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

    when(sessionService.getSessionsForUserByConsultingTypeId(any(), anyInt()))
        .thenReturn(SESSION_LIST);
    createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_SUCHT);

    verify(sessionService, times(0)).saveSession(any());
  }

  @Test(expected = InternalServerErrorException.class)
  public void createUserSession_Should_ReturnInternalServerErrorAndRollbackUserAccount_When_SessionCouldNotBeSaved() {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, 0))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class)))
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
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, 0))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class)))
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
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, 0))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class)))
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
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, 0)).thenReturn(null);

    createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_SUCHT);
  }

  @Test
  public void createUserSession_Should_ReturnSessionId_OnSuccess() {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, 0))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class)))
        .thenReturn(SESSION_WITHOUT_CONSULTANT);

    Long result = createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_SUCHT);

    assertEquals(SESSION_WITHOUT_CONSULTANT.getId(), result);
  }

  @Test
  public void createUserSession_Should_CreateSessionData() {

    when(sessionService.getSessionsForUserId(USER_ID))
        .thenReturn(USER_SESSION_RESPONSE_DTO_LIST_U25);
    when(agencyVerifier.getVerifiedAgency(AGENCY_ID, 0))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class)))
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
    when(agencyVerifier.getVerifiedAgency(USER_DTO_SUCHT.getAgencyId(), 2))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class)))
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
    when(agencyVerifier.getVerifiedAgency(USER_DTO_SUCHT.getAgencyId(), 14))
        .thenReturn(AGENCY_DTO_U25);
    when(sessionService.initializeSession(any(), any(), any(Boolean.class)))
        .thenReturn(SESSION_WITHOUT_CONSULTANT);

    Long result = createSessionFacade
        .createUserSession(USER_DTO_SUCHT, USER, CONSULTING_TYPE_SETTINGS_CHILDREN);

    assertEquals(SESSION_WITHOUT_CONSULTANT.getId(), result);
    verify(monitoringService, times(0)).updateMonitoring(any(), any());
  }

  @Test
  public void createDirectUserSession_Should_returnConflictWithExistingSession_When_userHasAlreadyASessionWithConsultantInConsultingType() {
    var session = new EasyRandom().nextObject(Session.class);
    when(sessionService.findSessionByConsultantAndUserAndConsultingType(any(), any(), any()))
        .thenReturn(Optional.of(session));
    var consultingTypeResponseDTO = new ExtendedConsultingTypeResponseDTO();
    consultingTypeResponseDTO.id(session.getConsultingTypeId());

    var result = createSessionFacade
        .createDirectUserSession(null, null, null, consultingTypeResponseDTO);

    assertThat(result.getStatus(), is(HttpStatus.CONFLICT));
    assertThat(result.getSessionId(), is(session.getId()));
    assertThat(result.getRcGroupId(), is(session.getGroupId()));
  }

  @Test
  public void createDirectUserSession_Should_returnCreatedWithNewSession_When_userConsultantRelationIsNew() {
    var agencyDTO = new EasyRandom().nextObject(AgencyDTO.class);
    var session = new EasyRandom().nextObject(Session.class);
    when(agencyVerifier.getVerifiedAgency(anyLong(), anyInt())).thenReturn(agencyDTO);
    when(sessionService.findSessionByConsultantAndUserAndConsultingType(any(), any(), any()))
        .thenReturn(Optional.empty());
    when(sessionService.initializeDirectSession(any(), any(), any(), anyBoolean()))
        .thenReturn(session);

    var result = createSessionFacade.createDirectUserSession(null, mock(UserDTO.class), null, mock(
        ExtendedConsultingTypeResponseDTO.class));

    assertThat(result.getStatus(), is(HttpStatus.CREATED));
    assertThat(result.getSessionId(), is(session.getId()));
  }

  @Test
  public void createDirectUserSession_Should_returnCreatedWithNewSession_When_userConsultantRelationIsWithOtherConsultingType() {
    var agencyDTO = new EasyRandom().nextObject(AgencyDTO.class);
    var session = new EasyRandom().nextObject(Session.class);
    when(agencyVerifier.getVerifiedAgency(anyLong(), anyInt())).thenReturn(agencyDTO);
    when(sessionService.findSessionByConsultantAndUserAndConsultingType(any(), any(), any()))
        .thenReturn(Optional.empty());
    when(sessionService.initializeDirectSession(any(), any(), any(), anyBoolean()))
        .thenReturn(session);
    var consultingTypeResponseDTO = new ExtendedConsultingTypeResponseDTO();
    consultingTypeResponseDTO.id(session.getConsultingTypeId() + 1);

    var result = createSessionFacade
        .createDirectUserSession(null, mock(UserDTO.class), null, consultingTypeResponseDTO);

    assertThat(result.getStatus(), is(HttpStatus.CREATED));
    assertThat(result.getSessionId(), is(session.getId()));
  }

}
