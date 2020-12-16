package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.ExceptionConstants.INTERNAL_SERVER_ERROR_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.RC_ADD_USER_TO_GROUP_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.RC_CHAT_REMOVE_SYSTEM_MESSAGES_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.RC_POST_MESSAGE_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.ExceptionConstants.SAVE_USER_EXCEPTION;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.testHelper.TestConstants.EXCEPTION;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_LANGUAGE_FORMAL;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.NOW;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKET_CHAT_USER_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_INFO_RESPONSE_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_INFO_RESPONSE_DTO_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.SaveUserException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostWelcomeMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.Now;
import de.caritas.cob.userservice.api.helper.RocketChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.manager.consultingtype.SessionDataInitializing;
import de.caritas.cob.userservice.api.model.rocketchat.RocketChatUserDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import de.caritas.cob.userservice.api.service.helper.MessageServiceHelper;
import java.time.LocalDateTime;
import java.util.Collections;
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

@RunWith(MockitoJUnitRunner.class)
public class CreateEnquiryMessageFacadeTest {

  private final GroupDTO GROUP_DTO = new GroupDTO(RC_GROUP_ID, USERNAME, null, null, 0, 0,
      ROCKET_CHAT_USER_DTO, null, true, false, null);
  private final GroupResponseDTO GROUP_RESPONSE_DTO =
      new GroupResponseDTO(GROUP_DTO, true, null, null);
  private final GroupDTO FEEDBACK_GROUP_DTO_2 = new GroupDTO(RC_FEEDBACK_GROUP_ID, USERNAME, null,
      null, 0, 0,
      ROCKET_CHAT_USER_DTO, null, true, false, null);
  private final GroupResponseDTO FEEDBACK_GROUP_RESPONSE_DTO_2 =
      new GroupResponseDTO(FEEDBACK_GROUP_DTO_2, true, null, null);
  private final Session SESSION_WITHOUT_ENQUIRY_MESSAGE = new Session(1L, USER, CONSULTANT,
      ConsultingType.SUCHT, "99999", AGENCY_ID, SessionStatus.INITIAL, null, null);
  private final Session SESSION_WITH_ENQUIRY_MESSAGE = new Session(1L, USER, CONSULTANT,
      ConsultingType.SUCHT, "99999", AGENCY_ID, SessionStatus.INITIAL, new Date(), null);
  private final ConsultantAgency CONSULTANT_AGENCY =
      new ConsultantAgency(1L, CONSULTANT, AGENCY_ID, LocalDateTime.now(), LocalDateTime.now());
  private final List<ConsultantAgency> CONSULTANT_AGENCY_LIST = Collections
      .singletonList(CONSULTANT_AGENCY);
  private final String FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID = "rocketChatSystemUserId";
  private final String ROCKET_CHAT_SYSTEM_USER_ID = "xN3Msb3ksnfxda7gEk";
  private final String CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH = "/monitoring/test.json";
  private final SessionDataInitializing SESSION_DATA_INITIALIZING =
      new SessionDataInitializing(true, true, true, true, true);
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null, SESSION_DATA_INITIALIZING, true,
          CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH, false, null, false, null, null);
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null, SESSION_DATA_INITIALIZING, true,
          CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH, true, null, false, null, null);
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT_AND_WELCOME_MESSAGE =
      new ConsultingTypeSettings(ConsultingType.SUCHT, true, MESSAGE, SESSION_DATA_INITIALIZING,
          true,
          CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH, true, null, false, null, null);

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
  private Now now;

  private Session session;
  private Consultant consultant;
  private User user;
  private ConsultingTypeSettings consultingTypeSettings;
  private UserInfoResponseDTO userInfoResponseDTO;
  private RocketChatUserDTO rocketChatUserDTO;
  private GroupResponseDTO groupResponseDTO;
  private GroupDTO groupDTO;
  private RocketChatCredentials rocketChatCredentials;

  @Before
  public void setUp() throws NoSuchFieldException, SecurityException {

    FieldSetter.setField(createEnquiryMessageFacade, createEnquiryMessageFacade.getClass()
        .getDeclaredField(FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID), ROCKET_CHAT_SYSTEM_USER_ID);
    setInternalState(LogService.class, "LOGGER", logger);

    this.session = new Session();
    session.setId(SESSION_ID);
    this.consultant = new Consultant();
    consultant.setId(USER_ID);
    consultant.setRocketChatId(RC_USER_ID);
    this.user = new User(USER_ID, null, USERNAME, EMAIL, RC_USER_ID, IS_LANGUAGE_FORMAL, null,
        null);
    this.consultingTypeSettings = new ConsultingTypeSettings();
    this.userInfoResponseDTO = new UserInfoResponseDTO();
    this.rocketChatUserDTO = new RocketChatUserDTO();
    this.groupResponseDTO = new GroupResponseDTO();
    this.groupDTO = new GroupDTO();
    this.rocketChatCredentials = RocketChatCredentials.builder().build();
  }

  @Test
  public void createEnquiryMessage_ShouldNot_ThrowException_When_Successful()
      throws Exception {

    session.setUser(user);
    session.setConsultingType(CONSULTING_TYPE_SUCHT);
    session.setConsultant(null);
    session.setEnquiryMessageDate(null);
    session.setAgencyId(AGENCY_ID);
    consultingTypeSettings.setSendWelcomeMessage(false);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager
        .getConsultingTypeSettings(session.getConsultingType()))
        .thenReturn(consultingTypeSettings);

    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());

    createEnquiryMessageFacade
        .createEnquiryMessage(user, SESSION_ID, MESSAGE, rocketChatCredentials);

    verify(userHelper, atLeastOnce()).updateRocketChatIdInDatabase(any(), anyString());
    verify(consultantAgencyService, atLeastOnce()).findConsultantsByAgencyId(anyLong());
    verify(consultingTypeManager, atLeastOnce()).getConsultingTypeSettings(any());
    verify(messageServiceHelper, atLeastOnce()).postMessage(any(), any(), any(), any());
    verify(messageServiceHelper, atLeastOnce())
        .postWelcomeMessageIfConfigured(any(), any(), any(), any());
    verify(sessionService, atLeastOnce()).saveSession(any());
    verify(emailNotificationFacade, atLeastOnce()).sendNewEnquiryEmailNotification(any());
  }

  @Test(expected = ConflictException.class)
  public void createEnquiryMessage_Should_ThrowConflictException_When_EnquiryMessageAlreadySaved() {

    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(sessionService.getSession(SESSION_ID))
        .thenReturn(Optional.of(SESSION_WITH_ENQUIRY_MESSAGE));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
  }

  @Test(expected = BadRequestException.class)
  public void createEnquiryMessage_Should_ThrowBadRequestException_When_SessionNotFoundForUser() {

    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createEnquiryMessage_Should_ThrowInternalServerErrorException_When_GetSessionCallFails() {

    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(sessionService.getSession(SESSION_ID)).thenThrow(new InternalServerErrorException(MESSAGE))
        .thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createEnquiryMessage_Should_ThrowInternalServerErrorException_When_CreationOfRocketChatGroupFailsWithAnException() {

    session.setUser(user);
    session.setConsultingType(CONSULTING_TYPE_SUCHT);
    session.setConsultant(null);
    session.setEnquiryMessageDate(null);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());

    createEnquiryMessageFacade
        .createEnquiryMessage(user, SESSION_ID, MESSAGE, rocketChatCredentials);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createEnquiryMessage_Should_ThrowInternalServerErrorException_When_PostMessageFailsWithAnException()
      throws Exception {

    session.setUser(user);
    session.setConsultingType(CONSULTING_TYPE_SUCHT);
    session.setConsultant(null);
    session.setEnquiryMessageDate(null);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager
        .getConsultingTypeSettings(session.getConsultingType()))
        .thenReturn(consultingTypeSettings);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatHelper.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    doThrow(RC_POST_MESSAGE_EXCEPTION).when(messageServiceHelper)
        .postMessage(anyString(), any(), anyString(), any());

    createEnquiryMessageFacade
        .createEnquiryMessage(user, SESSION_ID, MESSAGE, rocketChatCredentials);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createEnquiryMessage_Should_ThrowInternalServerErrorException_When_ConsultantsOfAgencyCanNotBeReadFromDB() {

    session.setUser(user);
    session.setConsultingType(CONSULTING_TYPE_SUCHT);
    session.setConsultant(null);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);

    createEnquiryMessageFacade
        .createEnquiryMessage(user, SESSION_ID, MESSAGE, rocketChatCredentials);
    verify(rocketChatService, atLeast(1)).rollbackGroup(RC_GROUP_ID, rocketChatCredentials);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createEnquiryMessage_Should_ThrowInternalServerErrorException_When_AddConsultantToRocketChatGroupFails()
      throws Exception {

    session.setUser(user);
    session.setConsultingType(CONSULTING_TYPE_SUCHT);
    session.setConsultant(null);
    session.setEnquiryMessageDate(null);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatHelper.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());

    Mockito.doThrow(new RocketChatAddUserToGroupException(MESSAGE)).when(rocketChatService)
        .addUserToGroup(anyString(), anyString());

    createEnquiryMessageFacade
        .createEnquiryMessage(user, SESSION_ID, MESSAGE, rocketChatCredentials);
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroup_When_PostMessageFails()
      throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultingTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(RC_POST_MESSAGE_EXCEPTION).when(messageServiceHelper).postMessage(Mockito.anyString(),
        Mockito.any(), Mockito.anyString(), Mockito.any());
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1)).rollbackGroup(RC_GROUP_ID, RC_CREDENTIALS);
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroup_When_AddConsultantToRocketChatGroupFails()
      throws Exception {

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

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1)).rollbackGroup(RC_GROUP_ID, RC_CREDENTIALS);
  }

  @Test(expected = BadRequestException.class)
  public void createEnquiryMessage_Should_ThrowBadRequest_When_KeycloakAndRocketChatUsersDontMatch() {

    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO_2);

    createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
  }

  @Test
  public void createEnquiryMessage_Should_UpdateCorrectSessionInformation_When_Successful()
      throws Exception {

    session.setUser(user);
    session.setConsultingType(CONSULTING_TYPE_SUCHT);
    session.setConsultant(null);
    consultingTypeSettings.setFeedbackChat(true);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);
    Session spySession = Mockito.spy(session);

    when(now.getDate()).thenReturn(NOW);
    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(spySession));
    when(consultingTypeManager
        .getConsultingTypeSettings(session.getConsultingType()))
        .thenReturn(consultingTypeSettings);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));

    createEnquiryMessageFacade
        .createEnquiryMessage(user, SESSION_ID, MESSAGE, rocketChatCredentials);

    verify(spySession, times(1)).setGroupId(groupResponseDTO.getGroup().getId());
    verify(spySession, times(1))
        .setFeedbackGroupId(groupResponseDTO.getGroup().getId());
    verify(spySession, times(1)).setEnquiryMessageDate(NOW);
    verify(spySession, times(1)).setStatus(SessionStatus.NEW);
    verify(now, times(1)).getDate();
    verify(sessionService, times(1)).saveSession(spySession);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createEnquiryMessage_Should_ThrowInternalServerErrorException_When_CreatePrivateGroupWithSystemUserFails()
      throws RocketChatCreateGroupException {

    session.setUser(user);
    session.setConsultingType(CONSULTING_TYPE_SUCHT);
    session.setConsultant(null);
    consultingTypeSettings.setFeedbackChat(true);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager
        .getConsultingTypeSettings(session.getConsultingType()))
        .thenReturn(consultingTypeSettings);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));

    createEnquiryMessageFacade
        .createEnquiryMessage(user, SESSION_ID, MESSAGE, rocketChatCredentials);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createEnquiryMessage_Should_ThrowInternalServerErrorException_When_UpdateRocketChatIdForUserFails()
      throws RocketChatCreateGroupException, SaveUserException {

    session.setUser(user);
    session.setConsultingType(CONSULTING_TYPE_SUCHT);
    session.setConsultant(null);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);
    SaveUserException saveUserException = new SaveUserException(MESSAGE, null);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager
        .getConsultingTypeSettings(session.getConsultingType()))
        .thenReturn(consultingTypeSettings);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    doThrow(saveUserException).when(userHelper)
        .updateRocketChatIdInDatabase(user, rocketChatCredentials.getRocketChatUserId());

    createEnquiryMessageFacade
        .createEnquiryMessage(user, SESSION_ID, MESSAGE, rocketChatCredentials);

  }

  @Test(expected = InternalServerErrorException.class)
  public void createEnquiryMessage_Should_ThrowInternalServerErrorException_When_UpdateOfSessionFails()
      throws RocketChatCreateGroupException {

    session.setUser(user);
    session.setConsultingType(CONSULTING_TYPE_SUCHT);
    session.setConsultant(null);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);
    InternalServerErrorException internalServerErrorException = new InternalServerErrorException(
        MESSAGE);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager
        .getConsultingTypeSettings(session.getConsultingType()))
        .thenReturn(consultingTypeSettings);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    doThrow(internalServerErrorException).when(sessionService)
        .saveSession(session);

    createEnquiryMessageFacade
        .createEnquiryMessage(user, SESSION_ID, MESSAGE, rocketChatCredentials);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createEnquiryMessage_Should_ThrowInternalServerErrorException_When_PrivateGroupWithSystemUserIsNotPresent()
      throws RocketChatCreateGroupException {

    session.setUser(user);
    session.setConsultingType(CONSULTING_TYPE_SUCHT);
    session.setConsultant(null);
    consultingTypeSettings.setFeedbackChat(true);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager
        .getConsultingTypeSettings(session.getConsultingType()))
        .thenReturn(consultingTypeSettings);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.empty());

    createEnquiryMessageFacade
        .createEnquiryMessage(user, SESSION_ID, MESSAGE, rocketChatCredentials);
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroup_WhenAddSystemUserToGroupFails()
      throws RocketChatCreateGroupException, RocketChatAddUserToGroupException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultingTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(RC_ADD_USER_TO_GROUP_EXCEPTION).when(rocketChatService)
        .addUserToGroup(ROCKET_CHAT_SYSTEM_USER_ID, GROUP_RESPONSE_DTO.getGroup().getId());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, atMost(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);

  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroup_WhenRemoveSystemMessagesFails()
      throws RocketChatCreateGroupException, RocketChatRemoveSystemMessagesException, RocketChatUserNotInitializedException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultingTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(RC_CHAT_REMOVE_SYSTEM_MESSAGES_EXCEPTION).when(rocketChatService)
        .removeSystemMessages(Mockito.eq(GROUP_RESPONSE_DTO.getGroup().getId()), Mockito.any(),
            Mockito.any());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, atMost(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);

  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroupAndFeedbackGroup_WhenAddSystemUserToFeedbackGroupFails()
      throws RocketChatCreateGroupException, RocketChatAddUserToGroupException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultingTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    doThrow(RC_ADD_USER_TO_GROUP_EXCEPTION).when(rocketChatService)
        .addUserToGroup(ROCKET_CHAT_SYSTEM_USER_ID,
            FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, atMost(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, atMost(1))
        .rollbackGroup(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId(), RC_CREDENTIALS);

  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroupAndFeedbackGroup_When_WhenRemoveSystemMessagesFailsForFeedbackGroup()
      throws Exception {

    when(consultingTypeManager
        .getConsultingTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    when(keycloakHelper.userHasAuthority(CONSULTANT_AGENCY_LIST.get(0).getConsultant().getId(),
        Authority.VIEW_ALL_FEEDBACK_SESSIONS)).thenReturn(true);
    doThrow(RC_CHAT_REMOVE_SYSTEM_MESSAGES_EXCEPTION).when(rocketChatService)
        .removeSystemMessages(Mockito.eq(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId()),
            Mockito.any(), Mockito.any());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, atMost(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, atMost(1))
        .rollbackGroup(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId(), RC_CREDENTIALS);
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroupAndFeedbackGroup_When_AddConsultantToRocketChatFeedbackGroupFails()
      throws RocketChatCreateGroupException, RocketChatAddUserToGroupException {

    when(consultingTypeManager
        .getConsultingTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    when(keycloakHelper.userHasAuthority(CONSULTANT_AGENCY_LIST.get(0).getConsultant().getId(),
        Authority.VIEW_ALL_FEEDBACK_SESSIONS)).thenReturn(true);
    Mockito.doThrow(RC_ADD_USER_TO_GROUP_EXCEPTION).when(rocketChatService)
        .addUserToGroup(Mockito.eq(CONSULTANT_AGENCY_LIST.get(0).getConsultant().getRocketChatId()),
            Mockito.eq(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId()));

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, times(1))
        .deleteGroupAsSystemUser(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroupFeedbackGroup_When_UpdateOfSessionFails()
      throws Exception {

    Session spySession = Mockito.spy(SESSION_WITHOUT_CONSULTANT);

    when(now.getDate()).thenReturn(NOW);
    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(spySession));
    when(consultingTypeManager
        .getConsultingTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    when(sessionService.saveSession(spySession)).thenThrow(INTERNAL_SERVER_ERROR_EXCEPTION);

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, times(1))
        .deleteGroupAsSystemUser(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroupFeedbackGroup_When_PostWelcomeMessageFailsWithAnException()
      throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultingTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT_AND_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    CreateEnquiryExceptionInformation createEnquiryExceptionInformation = CreateEnquiryExceptionInformation
        .builder().rcGroupId(GROUP_RESPONSE_DTO.getGroup().getId())
        .rcFeedbackGroupId(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId())
        .session(SESSION_WITHOUT_CONSULTANT)
        .build();
    RocketChatPostWelcomeMessageException rocketChatPostWelcomeMessageException =
        new RocketChatPostWelcomeMessageException(MESSAGE, EXCEPTION,
            createEnquiryExceptionInformation);
    doThrow(rocketChatPostWelcomeMessageException).when(messageServiceHelper)
        .postWelcomeMessageIfConfigured(Mockito.anyString(),
            Mockito.any(), Mockito.any(), Mockito.any());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, times(1))
        .deleteGroupAsSystemUser(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroupFeedbackGroup_When_PostMessageFailsWithAnException()
      throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultingTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    CreateEnquiryExceptionInformation createEnquiryExceptionInformation = CreateEnquiryExceptionInformation
        .builder().rcGroupId(GROUP_RESPONSE_DTO.getGroup().getId())
        .rcFeedbackGroupId(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId())
        .session(SESSION_WITHOUT_CONSULTANT)
        .build();
    RocketChatPostMessageException rocketChatPostMessageException =
        new RocketChatPostMessageException(MESSAGE, createEnquiryExceptionInformation);
    doThrow(rocketChatPostMessageException).when(messageServiceHelper)
        .postMessage(Mockito.anyString(),
            Mockito.any(), Mockito.anyString(), Mockito.any());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, times(1))
        .deleteGroupAsSystemUser(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroupFeedback_When_UpdateOfUserFailsWithAnException()
      throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager
        .getConsultingTypeSettings(SESSION_WITHOUT_CONSULTANT.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT_AND_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    when(rocketChatHelper.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    doThrow(SAVE_USER_EXCEPTION).when(userHelper)
        .updateRocketChatIdInDatabase(USER, RC_CREDENTIALS.getRocketChatUserId());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(USER, SESSION_ID, MESSAGE, RC_CREDENTIALS);
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, times(1))
        .deleteGroupAsSystemUser(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());
  }

}
