package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.ExceptionConstants.CREATE_MONITORING_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.ENQUIRY_MESSAGE_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.RC_CREATE_GROUP_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.RC_POST_MESSAGE_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKET_CHAT_USER_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_INFO_RESPONSE_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_INFO_RESPONSE_DTO_2;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.CreateMonitoringException;
import de.caritas.cob.userservice.api.exception.EnquiryMessageException;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatGetUserInfoException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatPostMessageException;
import de.caritas.cob.userservice.api.helper.MonitoringHelper;
import de.caritas.cob.userservice.api.helper.RocketChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.manager.consultingType.SessionDataInitializing;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.UserService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import de.caritas.cob.userservice.api.service.helper.MessageServiceHelper;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class CreateEnquiryMessageFacadeTest {

  @InjectMocks
  private CreateEnquiryMessageFacade createEnquiryMessageFacade;
  @Mock
  private EmailNotificationFacade emailNotificationFacade;
  @Mock
  private SessionService sessionService;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  private MessageServiceHelper messageServiceHelper;
  @Mock
  private ConsultantAgencyService consultantAgencyService;
  @Mock
  private MonitoringService monitoringService;
  @Mock
  private UserService userService;
  @Mock
  private Logger logger;
  @Mock
  private ConsultingTypeManager consultingTypeManager;
  @Mock
  private KeycloakAdminClientHelper keycloakHelper;
  @Mock
  private UserHelper userHelper;
  @Mock
  private RocketChatHelper rocketChatHelper;
  @Mock
  private RestTemplate restTemplate;
  @Mock
  private MonitoringHelper monitoringHelper;

  private final GroupDTO GROUP_DTO = new GroupDTO(RC_GROUP_ID, USERNAME, null, null, 0, 0,
      ROCKET_CHAT_USER_DTO, null, true, false, null);
  private final GroupResponseDTO GROUP_RESPONSE_DTO =
      new GroupResponseDTO(GROUP_DTO, true, null, null);
  private final Session SESSION_WITHOUT_ENQUIRY_MESSAGE = new Session(1L, USER, CONSULTANT,
      ConsultingType.SUCHT, "99999", AGENCY_ID, SessionStatus.INITIAL, null, null);
  private final Session SESSION_WITH_ENQUIRY_MESSAGE = new Session(1L, USER, CONSULTANT,
      ConsultingType.SUCHT, "99999", AGENCY_ID, SessionStatus.INITIAL, new Date(), null);
  private final ConsultantAgency CONSULTANT_AGENCY =
      new ConsultantAgency(1L, CONSULTANT, AGENCY_ID);
  private final List<ConsultantAgency> CONSULTANT_AGENCY_LIST = Arrays.asList(CONSULTANT_AGENCY);
  private final String FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID = "ROCKET_CHAT_SYSTEM_USER_ID";
  private final String ROCKET_CHAT_SYSTEM_USER_ID = "xN3Msb3ksnfxda7gEk";
  private final String CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH = "/monitoring/test.json";
  private final SessionDataInitializing SESSION_DATA_INITIALIZING =
      new SessionDataInitializing(true, true, true, true, true);
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null, SESSION_DATA_INITIALIZING, true,
          CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH, false, null, false, null, null);
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_NO_MONITORING =
      new ConsultingTypeSettings(ConsultingType.U25, false, null, SESSION_DATA_INITIALIZING, false,
          null, false, null, false, null, null);
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_MONITORING =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null, SESSION_DATA_INITIALIZING, true,
          CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH, false, null, false, null, null);

  @Before
  public void setUp() throws NoSuchFieldException, SecurityException {

    FieldSetter.setField(createEnquiryMessageFacade, createEnquiryMessageFacade.getClass()
        .getDeclaredField(FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID), ROCKET_CHAT_SYSTEM_USER_ID);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnHttpStatusCreated_When_Successful()
      throws CreateMonitoringException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    Mockito.doNothing().when(monitoringService).createMonitoring(SESSION_WITHOUT_CONSULTANT,
        CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.CREATED, result);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnHttpStatusConflict_When_EnquiryMessageAlreadySaved() {

    when(sessionService.getSession(SESSION_ID))
        .thenReturn(Optional.of(SESSION_WITH_ENQUIRY_MESSAGE));
    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    assertEquals(HttpStatus.CONFLICT, result);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnBadRequest_When_SessionNotFoundForUser() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.empty());
    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    assertEquals(HttpStatus.BAD_REQUEST, result);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnInteralServerError_When_GetSessionCallFails() {

    when(sessionService.getSession(SESSION_ID)).thenThrow(new ServiceException(MESSAGE))
        .thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_When_CreationOfRocketChatGroupFailsWithAnException() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenThrow(RC_CREATE_GROUP_EXCEPTION);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_When_CreationOfRocketChatGroupFails() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.empty());
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_When_UpdateMonitoringFails()
      throws CreateMonitoringException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(CREATE_MONITORING_EXCEPTION).when(monitoringService)
        .createMonitoring(SESSION_WITHOUT_CONSULTANT, CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_When_PostMessageFailsWithAnException()
      throws RocketChatPostMessageException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    doThrow(RC_POST_MESSAGE_EXCEPTION).when(messageServiceHelper).postMessage(Mockito.anyString(),
        Mockito.any(), Mockito.anyString(), Mockito.any());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_When_ConsultantsOfAgencyCanNotBeReadFromDB() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenThrow(new ServiceException(MESSAGE));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
    verify(rocketChatService, atLeast(1)).deleteGroup(RC_GROUP_ID, RC_CREDENTIALS);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_When_AddConsultantToRocketChatGroupFails() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    Mockito.doThrow(new RocketChatAddUserToGroupException(MESSAGE)).when(rocketChatService)
        .addUserToGroup(Mockito.anyString(), Mockito.anyString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRocketChatGroupAndMonitoringData_When_PostMessageFails()
      throws CreateMonitoringException, RocketChatPostMessageException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(RC_POST_MESSAGE_EXCEPTION).when(messageServiceHelper).postMessage(Mockito.anyString(),
        Mockito.any(), Mockito.anyString(), Mockito.any());
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_CREDENTIALS);
    verify(monitoringService, times(1)).rollbackInitializeMonitoring(Mockito.any());
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRocketChatGroup_When_FindConsultantsByAgencyIdFails() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenThrow(new ServiceException(MESSAGE));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_CREDENTIALS);
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRocketChatGroup_When_AddConsultantToRocketChatGroupFails() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    Mockito.doThrow(new RocketChatAddUserToGroupException(MESSAGE)).when(rocketChatService)
        .addUserToGroup(CONSULTANT_AGENCY_LIST.get(0).getConsultant().getRocketChatId(),
            GROUP_RESPONSE_DTO.getGroup().getId());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_CREDENTIALS);
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRocketChatGroup_When_PostMessageFails()
      throws RocketChatPostMessageException, CreateMonitoringException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(RC_POST_MESSAGE_EXCEPTION).when(messageServiceHelper).postMessage(Mockito.anyString(),
        Mockito.any(), Mockito.anyString(), Mockito.any());
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_CREDENTIALS);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_When_SaveSessionInfoFails()
      throws EnquiryMessageException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(ENQUIRY_MESSAGE_EXCEPTION).when(sessionService)
        .saveEnquiryMessageDateAndRocketChatGroupId(SESSION_WITHOUT_CONSULTANT, RC_GROUP_ID);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(null);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_CREDENTIALS);
    verify(monitoringService, times(1)).rollbackInitializeMonitoring(Mockito.any());
  }

  @Test
  public void createEnquiryMessage_Should_LogException_When_DeletionOfRocketChatGroupFails()
      throws EnquiryMessageException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(ENQUIRY_MESSAGE_EXCEPTION).when(sessionService)
        .saveEnquiryMessageDateAndRocketChatGroupId(SESSION_WITHOUT_CONSULTANT, RC_GROUP_ID);
    when(rocketChatService.deleteGroup(RC_GROUP_ID, RC_CREDENTIALS)).thenReturn(false);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void createEnquiryMessage_Should_CreateInitialMonitoring_When_MonitoringIsActivatedInConsultingTypeSettings()
      throws CreateMonitoringException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_MONITORING);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.CREATED, result);
    verify(monitoringService, times(1)).createMonitoring(SESSION_WITHOUT_CONSULTANT,
        CONSULTING_TYPE_SETTINGS_WITH_MONITORING);
  }

  @Test
  public void createEnquiryMessage_ShouldNot_CreateInitialMonitoring_When_MonitoringIsDeactivatedInConsultingTypeSettings()
      throws CreateMonitoringException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_MONITORING);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq((RC_CREDENTIALS))))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.CREATED, result);
    verify(monitoringService, times(0)).createMonitoring(SESSION_WITHOUT_CONSULTANT,
        CONSULTING_TYPE_SETTINGS_WITH_MONITORING);
  }

  @Test
  public void createEnquiryMessage_Should_ThrowBadRequest_When_KeycloakAndRocketChatUsersDontMatch() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO_2);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.BAD_REQUEST, result);
  }

  @Test
  public void createEnquiryMessage_Should_ThrowInternalServerError_When_GetUserInfoThrowsException() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID))
        .thenThrow(new RocketChatGetUserInfoException(ERROR));

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
  }

}
