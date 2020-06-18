package de.caritas.cob.UserService.api.facade;

import static de.caritas.cob.UserService.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.UserService.testHelper.TestConstants.ERROR;
import static de.caritas.cob.UserService.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.UserService.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.UserService.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.ROCKET_CHAT_USER_DTO;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_INFO_RESPONSE_DTO;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_INFO_RESPONSE_DTO_2;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
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
import org.springframework.http.HttpStatus;
import de.caritas.cob.UserService.api.exception.EnquiryMessageException;
import de.caritas.cob.UserService.api.exception.MessageServiceHelperException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatAddUserToGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatCreateGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatDeleteGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatGetUserInfoException;
import de.caritas.cob.UserService.api.helper.RocketChatHelper;
import de.caritas.cob.UserService.api.helper.UserHelper;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.UserService.api.manager.consultingType.SessionDataInitializing;
import de.caritas.cob.UserService.api.model.rocketChat.group.GroupDTO;
import de.caritas.cob.UserService.api.model.rocketChat.group.GroupResponseDTO;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.session.SessionStatus;
import de.caritas.cob.UserService.api.service.ConsultantAgencyService;
import de.caritas.cob.UserService.api.service.LogService;
import de.caritas.cob.UserService.api.service.MonitoringService;
import de.caritas.cob.UserService.api.service.RocketChatService;
import de.caritas.cob.UserService.api.service.SessionService;
import de.caritas.cob.UserService.api.service.UserService;
import de.caritas.cob.UserService.api.service.helper.KeycloakAdminClientHelper;
import de.caritas.cob.UserService.api.service.helper.MessageServiceHelper;

@RunWith(MockitoJUnitRunner.class)
public class CreateEnquiryMessageFacadeTest {

  @InjectMocks
  CreateEnquiryMessageFacade createEnquiryMessageFacade;
  @Mock
  EmailNotificationFacade emailNotificationFacade;
  @Mock
  SessionService sessionService;
  @Mock
  RocketChatService rocketChatService;
  @Mock
  MessageServiceHelper messageServiceHelper;
  @Mock
  ConsultantAgencyService consultantAgencyService;
  @Mock
  MonitoringService monitoringService;
  @Mock
  UserService userService;
  @Mock
  LogService logService;
  @Mock
  ConsultingTypeManager consultingTypeManager;
  @Mock
  KeycloakAdminClientHelper keycloakHelper;
  @Mock
  UserHelper userHelper;
  @Mock
  RocketChatHelper rocketChatHelper;

  private final GroupDTO GROUP_DTO = new GroupDTO(RC_GROUP_ID, USERNAME, null, null, 0, 0,
      ROCKET_CHAT_USER_DTO, null, true, false, null);
  private final GroupDTO FEEDBACK_GROUP_DTO = new GroupDTO(RC_FEEDBACK_GROUP_ID, USERNAME, null,
      null, 0, 0, ROCKET_CHAT_USER_DTO, null, true, false, null);
  private final GroupResponseDTO GROUP_RESPONSE_DTO =
      new GroupResponseDTO(GROUP_DTO, true, null, null);
  private final GroupResponseDTO FEEDBACK_GROUP_RESPONSE_DTO =
      new GroupResponseDTO(FEEDBACK_GROUP_DTO, true, null, null);
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
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE_WITH_FEEDBACK_CHAT =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null, SESSION_DATA_INITIALIZING, true,
          CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH, true, null, false, null, null);
  private final String WELCOME_MESSAGE_WITH_PLACEHOLDER = "Hallo ${username}";
  private final String WELCOME_MESSAGE_WITH_REPLACED_PLACEHOLDER = "Hallo " + USER.getUsername();
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE =
      new ConsultingTypeSettings(ConsultingType.U25, true, WELCOME_MESSAGE_WITH_PLACEHOLDER,
          SESSION_DATA_INITIALIZING, true, CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH, false, null,
          false, null, null);
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
  }

  @Test
  public void createEnquiryMessage_Should_ReturnHttpStatusCreated_WhenSuccessfully() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    Mockito.doNothing().when(monitoringService).createMonitoring(sessions.get(0));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.CREATED, result);

  }

  @Test
  public void createEnquiryMessage_Should_ReturnHttpStatusConflict_WhenEnquiryMessageAlreadySaved() {


    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITH_ENQUIRY_MESSAGE);
    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);
    assertEquals(HttpStatus.CONFLICT, result);

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_WhenUserSessionListIsNull() {

    when(sessionService.getSessionsForUser(USER)).thenReturn(null);
    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInteralServerError_WhenUserSessionListIsEmpty() {

    when(sessionService.getSessionsForUser(USER)).thenReturn(new ArrayList<Session>());
    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);
    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);


  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_WhenCreationOfRocketChatGroupFailsWithAnException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);
    RocketChatCreateGroupException rocketChatCreateGroupException =
        new RocketChatCreateGroupException(new Exception());

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenThrow(rocketChatCreateGroupException);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_WhenCreationOfRocketChatGroupFails() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.empty());
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_WhenMonitoringUpdateThroughServiceFailsWithAnException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(new ServiceException(MESSAGE)).when(monitoringService)
        .createMonitoring(sessions.get(0));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_WhenPostMessageFailsWithAnException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    MessageServiceHelperException messageServiceHelperException =
        new MessageServiceHelperException(new Exception());
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenThrow(messageServiceHelperException);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);

  }


  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_WhenPostMessageFails() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(false);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerErrorAndDeleteRocketChatGroup_WhenConsultantsOfAgencyCanNotBeReadFromDB() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenThrow(new ServiceException(MESSAGE));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
    verify(rocketChatService, atLeast(1)).deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID);

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_WhenAddConsultantToRocketChatGroupFails() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);
    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);

    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));

    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);

    Mockito.doThrow(new RocketChatAddUserToGroupException(MESSAGE)).when(rocketChatService)
        .addUserToGroup(Mockito.anyString(), Mockito.anyString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);

  }

  @Test
  public void createEnquiryMessage_Should_DeleteRocketChatGroupAndMonitoringData_WhenPostMessageFails() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(false);
    Mockito.doNothing().when(monitoringService).createMonitoring(sessions.get(0));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID);
    verify(monitoringService, times(1)).deleteInitialMonitoring(sessions.get(0));

  }

  @Test
  public void createEnquiryMessage_Should_DeleteRocketChatGroup_WhenFindConsultantsByAgencyIdFailsWithAnException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));

    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenThrow(new ServiceException(MESSAGE));

    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID);

  }

  @Test
  public void createEnquiryMessage_Should_DeleteRocketChatGroup_WhenAddConsultantToRocketChatGroupFailsWithAnException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));

    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);

    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    Mockito.doThrow(new RocketChatAddUserToGroupException(MESSAGE)).when(rocketChatService)
        .addUserToGroup(CONSULTANT_AGENCY_LIST.get(0).getConsultant().getRocketChatId(),
            GROUP_RESPONSE_DTO.getGroup().getId());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID);
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRocketChatGroup_WhenPostMessageFailsWithAnException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    MessageServiceHelperException messageServiceHelperException =
        new MessageServiceHelperException(new Exception());
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenThrow(messageServiceHelperException);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID);
  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerError_WhenSaveEnquiryMessageDateAndRocketChatGroupIdFailsWithException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    EnquiryMessageException enquiryMessageException = new EnquiryMessageException(new Exception());
    when(sessionService.saveEnquiryMessageDateAndRocketChatGroupId(SESSION_WITHOUT_ENQUIRY_MESSAGE,
        RC_GROUP_ID)).thenThrow(enquiryMessageException);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(null);
    Mockito.doNothing().when(monitoringService).deleteInitialMonitoring(sessions.get(0));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);

  }

  @Test
  public void createEnquiryMessage_Should_DeleteRocketChatGroupAndMonitoringData_WhenSaveEnquiryMessageDateAndRocketChatGroupIdFailsWithException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    EnquiryMessageException enquiryMessageException = new EnquiryMessageException(new Exception());
    when(sessionService.saveEnquiryMessageDateAndRocketChatGroupId(SESSION_WITHOUT_ENQUIRY_MESSAGE,
        RC_GROUP_ID)).thenThrow(enquiryMessageException);
    Mockito.doNothing().when(monitoringService).createMonitoring(sessions.get(0));
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID);
    verify(monitoringService, times(1)).deleteInitialMonitoring(sessions.get(0));
  }

  @Test
  public void createEnquiryMessage_Should_LogException_WhenDeletionOfRocketChatGroupFailsWithException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    EnquiryMessageException enquiryMessageException = new EnquiryMessageException(new Exception());
    when(sessionService.saveEnquiryMessageDateAndRocketChatGroupId(SESSION_WITHOUT_ENQUIRY_MESSAGE,
        RC_GROUP_ID)).thenThrow(enquiryMessageException);
    RocketChatDeleteGroupException rocketChatDeleteGroupException =
        new RocketChatDeleteGroupException(new Exception());
    when(rocketChatService.deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID))
        .thenThrow(rocketChatDeleteGroupException);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(logService, times(1)).logInternalServerError(Mockito.anyString(),
        Mockito.eq(rocketChatDeleteGroupException));

  }

  @Test
  public void createEnquiryMessage_Should_LogException_WhenDeletionOfRocketChatGroupFails() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    EnquiryMessageException enquiryMessageException = new EnquiryMessageException(new Exception());
    when(sessionService.saveEnquiryMessageDateAndRocketChatGroupId(SESSION_WITHOUT_ENQUIRY_MESSAGE,
        RC_GROUP_ID)).thenThrow(enquiryMessageException);
    when(rocketChatService.deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID)).thenReturn(false);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(logService, times(1)).logInternalServerError(Mockito.anyString());

  }

  @Test
  public void createEnquiryMessage_Should_LogException_WhenDeletionOfMonitoringDataFailsWithException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    EnquiryMessageException enquiryMessageException = new EnquiryMessageException(new Exception());
    when(sessionService.saveEnquiryMessageDateAndRocketChatGroupId(SESSION_WITHOUT_ENQUIRY_MESSAGE,
        RC_GROUP_ID)).thenThrow(enquiryMessageException);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    ServiceException serviceException = new ServiceException(MESSAGE);
    doThrow(serviceException).when(monitoringService).deleteInitialMonitoring(sessions.get(0));

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(logService, times(1)).logInternalServerError(Mockito.anyString(),
        Mockito.eq(serviceException));

  }

  @Test
  public void createEnquiryMessage_Should_AddSystemUserToRocketChatGroup() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(rocketChatService, times(1)).addUserToGroup(ROCKET_CHAT_SYSTEM_USER_ID, RC_GROUP_ID);

  }

  @Test
  public void createEnquiryMessage_Should_PostWelcomeMessageWithReplacedUsernamePlaceholderIfRequiredForConsultingType() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.decodeUsername(USER.getUsername())).thenReturn(USER.getUsername());
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(messageServiceHelper, times(1))
        .postMessageAsSystemUser(WELCOME_MESSAGE_WITH_REPLACED_PLACEHOLDER, RC_GROUP_ID);

  }

  @Test
  public void createEnquiryMessage_Should_DecodeUsernameForWelcomeMessage() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.decodeUsername(USER.getUsername())).thenReturn(USER.getUsername());
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(userHelper, times(1)).decodeUsername(USER.getUsername());

  }

  @Test
  public void createEnquiryMessage_Should_NotPostWelcomeMessageIfNotRequiredForConsultingType() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    verify(messageServiceHelper, times(0)).postMessageAsSystemUser(Mockito.anyString(),
        Mockito.anyString());

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerErrorAndRollbackCreateGroupAndMonitoring_When_CreationOfRocketChatFeedbackGroupFails() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO))
            .thenReturn(Optional.empty());
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID);
    verify(monitoringService, times(1)).deleteInitialMonitoring(sessions.get(0));

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerErrorAndRollbackCreateGroupAndFeedbackGroupAndMonitoring_When_AddSystemUserToFeedbackGroupFails() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.anyString()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);

    Mockito.doNothing().when(rocketChatService).addUserToGroup(ROCKET_CHAT_SYSTEM_USER_ID,
        RC_GROUP_ID);
    Mockito.doThrow(new RocketChatAddUserToGroupException(MESSAGE)).when(rocketChatService)
        .addUserToGroup(ROCKET_CHAT_SYSTEM_USER_ID, RC_FEEDBACK_GROUP_ID);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatHelper.generateFeedbackGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID);
    verify(rocketChatService, times(1)).deleteGroup(RC_FEEDBACK_GROUP_ID, RC_TOKEN, RC_USER_ID);
    verify(monitoringService, times(1)).deleteInitialMonitoring(sessions.get(0));

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerErrorAndRollbackCreateGroupAndFeedbackGroupAndMonitoring_When_UpdatingFeedbackGroupIdInDBFails() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.anyString()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE_WITH_FEEDBACK_CHAT);
    Mockito.doNothing().when(rocketChatService).addUserToGroup(Mockito.anyString(),
        Mockito.anyString());
    when(rocketChatService.removeSystemMessages(Mockito.anyString(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatHelper.generateFeedbackGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    doThrow(new UpdateFeedbackGroupIdException(null)).when(sessionService)
        .updateFeedbackGroupId(Mockito.any(), Mockito.any());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID);
    verify(rocketChatService, times(1)).deleteGroup(RC_FEEDBACK_GROUP_ID, RC_TOKEN, RC_USER_ID);
    verify(monitoringService, times(1)).deleteInitialMonitoring(sessions.get(0));

  }

  @Test
  public void createEnquiryMessage_Should_AddUserToFeedbackGroup_When_HeHasTheRightToViewAllFeedbackSessions() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);
    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);

    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.anyString()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE_WITH_FEEDBACK_CHAT);
    Mockito.doNothing().when(rocketChatService).addUserToGroup(Mockito.anyString(),
        Mockito.anyString());
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(keycloakHelper.userHasAuthority(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(true);
    when(rocketChatService.removeSystemMessages(Mockito.anyString(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatHelper.generateFeedbackGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.CREATED, result);
    verify(rocketChatService, times(1)).addUserToGroup(ROCKETCHAT_ID, RC_FEEDBACK_GROUP_ID);

  }

  @Test
  public void createEnquiryMessage_Should_UpdateFeedbackGroupIdInDB_When_FeedbackChatIsEnabled() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.anyString()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE_WITH_FEEDBACK_CHAT);
    Mockito.doNothing().when(rocketChatService).addUserToGroup(Mockito.anyString(),
        Mockito.anyString());
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatService.removeSystemMessages(Mockito.anyString(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatHelper.generateFeedbackGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.CREATED, result);
    verify(sessionService, times(1))
        .updateFeedbackGroupId(Optional.of(SESSION_WITHOUT_ENQUIRY_MESSAGE), RC_FEEDBACK_GROUP_ID);

  }

  @Test
  public void createEnquiryMessage_Should_ReturnInternalServerErrorAndDoAllRollbacks_When_RemovingSystemmessagesFromFeedbackGroupFails() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.anyString()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE_WITH_FEEDBACK_CHAT);
    Mockito.doNothing().when(rocketChatService).addUserToGroup(Mockito.anyString(),
        Mockito.anyString());
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatService.removeSystemMessages(Mockito.anyString(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatHelper.generateFeedbackGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
    verify(rocketChatService, times(1)).deleteGroup(RC_GROUP_ID, RC_TOKEN, RC_USER_ID);
    verify(rocketChatService, times(1)).deleteGroup(RC_FEEDBACK_GROUP_ID, RC_TOKEN, RC_USER_ID);
    verify(monitoringService, times(1)).deleteInitialMonitoring(sessions.get(0));
    verify(sessionService, times(1)).saveSession(SESSION_WITHOUT_ENQUIRY_MESSAGE);

  }

  @Test
  public void createEnquiryMessage_Should_CreateInitialMonitoring_When_MonitoringIsActivatedInConsultingTypeSettings() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);
    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);

    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_MONITORING);
    Mockito.doNothing().when(rocketChatService).addUserToGroup(Mockito.anyString(),
        Mockito.anyString());
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.CREATED, result);
    verify(monitoringService, times(1)).createMonitoring(SESSION_WITHOUT_ENQUIRY_MESSAGE);;
  }

  @Test
  public void createEnquiryMessage_ShouldNot_CreateInitialMonitoring_When_MonitoringIsDeactivatedInConsultingTypeSettings() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_TOKEN),
        Mockito.eq(RC_USER_ID))).thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(messageServiceHelper.postMessage(MESSAGE, RC_USER_ID, RC_TOKEN, RC_GROUP_ID))
        .thenReturn(true);
    when(consultingTypeManager
        .getConsultantTypeSettings(SESSION_WITHOUT_ENQUIRY_MESSAGE.getConsultingType()))
            .thenReturn(CONSULTING_TYPE_SETTINGS_NO_MONITORING);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    Mockito.doNothing().when(rocketChatService).addUserToGroup(Mockito.anyString(),
        Mockito.anyString());
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.CREATED, result);
    verify(monitoringService, times(0)).createMonitoring(SESSION_WITHOUT_ENQUIRY_MESSAGE);
  }

  @Test
  public void createEnquiryMessage_Should_ThrowBadRequest_When_KeycloakAndRocketChatUsersDontMatch() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO_2);

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.BAD_REQUEST, result);
  }

  @Test
  public void createEnquiryMessage_Should_ThrowInternalServerError_When_GetUserInfoThrowsException() {

    List<Session> sessions = new ArrayList<Session>();
    sessions.add(SESSION_WITHOUT_ENQUIRY_MESSAGE);

    when(sessionService.getSessionsForUser(USER)).thenReturn(sessions);
    when(rocketChatService.getUserInfo(RC_USER_ID))
        .thenThrow(new RocketChatGetUserInfoException(ERROR));

    HttpStatus result =
        createEnquiryMessageFacade.createEnquiryMessage(USER, MESSAGE, RC_TOKEN, RC_USER_ID);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
  }

}
