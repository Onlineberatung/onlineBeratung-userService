package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.ANONYMOUS;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.REGISTERED;
import static de.caritas.cob.userservice.api.testHelper.ExceptionConstants.INTERNAL_SERVER_ERROR_EXCEPTION;
import static de.caritas.cob.userservice.api.testHelper.ExceptionConstants.RC_ADD_USER_TO_GROUP_EXCEPTION;
import static de.caritas.cob.userservice.api.testHelper.ExceptionConstants.RC_CHAT_REMOVE_SYSTEM_MESSAGES_EXCEPTION;
import static de.caritas.cob.userservice.api.testHelper.ExceptionConstants.RC_POST_MESSAGE_EXCEPTION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.EXCEPTION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.IS_LANGUAGE_FORMAL;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USERNAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKET_CHAT_USER_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_WITHOUT_CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_INFO_RESPONSE_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_INFO_RESPONSE_DTO_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.RocketChatUserDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.CreateEnquiryMessageException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostWelcomeMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.RocketChatRoomNameGenerator;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.EnquiryData;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.liveevents.LiveEventNotificationService;
import de.caritas.cob.userservice.api.service.message.MessageServiceProvider;
import de.caritas.cob.userservice.api.service.message.RocketChatData;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.GroupChatDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.SessionDataInitializingDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.WelcomeMessageDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessageResponseDTO;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RunWith(MockitoJUnitRunner.class)
public class CreateEnquiryMessageFacadeTest {

  private final GroupDTO GROUP_DTO =
      new GroupDTO(
          RC_GROUP_ID, USERNAME, null, null, 0, 0, ROCKET_CHAT_USER_DTO, null, true, false, null);
  private final GroupResponseDTO GROUP_RESPONSE_DTO =
      new GroupResponseDTO(GROUP_DTO, true, null, null);
  private final GroupDTO FEEDBACK_GROUP_DTO_2 =
      new GroupDTO(
          RC_FEEDBACK_GROUP_ID,
          USERNAME,
          null,
          null,
          0,
          0,
          ROCKET_CHAT_USER_DTO,
          null,
          true,
          false,
          null);
  private final GroupResponseDTO FEEDBACK_GROUP_RESPONSE_DTO_2 =
      new GroupResponseDTO(FEEDBACK_GROUP_DTO_2, true, null, null);
  private final Session SESSION_WITHOUT_ENQUIRY_MESSAGE =
      Session.builder()
          .id(1L)
          .user(USER)
          .consultant(CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .postcode("99999")
          .agencyId(AGENCY_ID)
          .status(SessionStatus.INITIAL)
          .createDate(nowInUtc())
          .build();

  private final Session SESSION_WITH_ENQUIRY_MESSAGE =
      Session.builder()
          .id(1L)
          .user(USER)
          .consultant(CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .postcode("99999")
          .agencyId(AGENCY_ID)
          .status(SessionStatus.INITIAL)
          .enquiryMessageDate(nowInUtc())
          .createDate(nowInUtc())
          .build();

  private final ConsultantAgency CONSULTANT_AGENCY =
      new ConsultantAgency(
          1L, CONSULTANT, AGENCY_ID, nowInUtc(), nowInUtc(), nowInUtc(), null, null);
  private final List<ConsultantAgency> CONSULTANT_AGENCY_LIST =
      Collections.singletonList(CONSULTANT_AGENCY);
  private final String FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID = "rocketChatSystemUserId";
  private final String ROCKET_CHAT_SYSTEM_USER_ID = "xN3Msb3ksnfxda7gEk";
  private SessionDataInitializingDTO SESSION_DATA_INITIALIZING =
      new SessionDataInitializingDTO().age(true).state(true);
  private final ExtendedConsultingTypeResponseDTO CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE =
      new ExtendedConsultingTypeResponseDTO()
          .id(CONSULTING_TYPE_ID_SUCHT)
          .slug("suchtberatung")
          .excludeNonMainConsultantsFromTeamSessions(true)
          .groupChat(new GroupChatDTO().isGroupChat(false))
          .consultantBoundedToConsultingType(false)
          .welcomeMessage(
              new WelcomeMessageDTO().sendWelcomeMessage(false).welcomeMessageText(null))
          .sendFurtherStepsMessage(false)
          .sessionDataInitializing(SESSION_DATA_INITIALIZING)
          .initializeFeedbackChat(false)
          .notifications(null)
          .languageFormal(false)
          .roles(null)
          .registration(null);
  private final ExtendedConsultingTypeResponseDTO CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT =
      new ExtendedConsultingTypeResponseDTO()
          .id(CONSULTING_TYPE_ID_SUCHT)
          .slug("suchtberatung")
          .excludeNonMainConsultantsFromTeamSessions(true)
          .groupChat(new GroupChatDTO().isGroupChat(false))
          .consultantBoundedToConsultingType(false)
          .welcomeMessage(
              new WelcomeMessageDTO().sendWelcomeMessage(false).welcomeMessageText(null))
          .sendFurtherStepsMessage(false)
          .sessionDataInitializing(SESSION_DATA_INITIALIZING)
          .initializeFeedbackChat(true)
          .notifications(null)
          .languageFormal(false)
          .roles(null)
          .registration(null);
  private final ExtendedConsultingTypeResponseDTO
      CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT_AND_WELCOME_MESSAGE =
          new ExtendedConsultingTypeResponseDTO()
              .id(CONSULTING_TYPE_ID_SUCHT)
              .slug("suchtberatung")
              .excludeNonMainConsultantsFromTeamSessions(true)
              .groupChat(new GroupChatDTO().isGroupChat(false))
              .consultantBoundedToConsultingType(false)
              .welcomeMessage(
                  new WelcomeMessageDTO().sendWelcomeMessage(true).welcomeMessageText(MESSAGE))
              .sendFurtherStepsMessage(false)
              .sessionDataInitializing(SESSION_DATA_INITIALIZING)
              .initializeFeedbackChat(true)
              .notifications(null)
              .languageFormal(false)
              .roles(null)
              .registration(null);

  private final EasyRandom easyRandom = new EasyRandom();

  @InjectMocks private CreateEnquiryMessageFacade createEnquiryMessageFacade;

  @Mock private EmailNotificationFacade emailNotificationFacade;

  @Mock private SessionService sessionService;

  @Mock private RocketChatService rocketChatService;

  @Mock private MessageServiceProvider messageServiceProvider;

  @Mock private ConsultantAgencyService consultantAgencyService;

  @Mock private Logger logger;

  @Mock private ConsultingTypeManager consultingTypeManager;

  @Mock private UserHelper userHelper;

  @Mock private RocketChatRoomNameGenerator rocketChatRoomNameGenerator;

  @Mock private UserService userService;

  @Mock private AgencyService agencyService;

  @Mock private LiveEventNotificationService liveEventNotificationService;

  @Mock HttpServletRequest servletRequest;

  private Session session;
  private User user;
  private ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO;
  private UserInfoResponseDTO userInfoResponseDTO;
  private RocketChatUserDTO rocketChatUserDTO;
  private GroupResponseDTO groupResponseDTO;
  private GroupDTO groupDTO;
  private RocketChatCredentials rocketChatCredentials;

  @Before
  public void setUp() throws NoSuchFieldException, SecurityException {
    setField(
        createEnquiryMessageFacade,
        FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID,
        ROCKET_CHAT_SYSTEM_USER_ID);
    setField(
        createEnquiryMessageFacade, "rocketChatRoomNameGenerator", rocketChatRoomNameGenerator);
    setInternalState(LogService.class, "LOGGER", logger);

    this.session = new Session();
    session.setId(SESSION_ID);
    session.setRegistrationType(REGISTERED);
    Consultant consultant = new Consultant();
    consultant.setId(USER_ID);
    consultant.setRocketChatId(RC_USER_ID);
    this.user =
        new User(
            USER_ID,
            null,
            USERNAME,
            EMAIL,
            RC_USER_ID,
            IS_LANGUAGE_FORMAL,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            true,
            LanguageCode.de,
            null,
            null,
            false,
            null);
    this.extendedConsultingTypeResponseDTO = new ExtendedConsultingTypeResponseDTO();
    this.extendedConsultingTypeResponseDTO.setWelcomeMessage(new WelcomeMessageDTO());
    this.userInfoResponseDTO = new UserInfoResponseDTO();
    this.rocketChatUserDTO = new RocketChatUserDTO();
    this.groupResponseDTO = new GroupResponseDTO();
    this.groupDTO = new GroupDTO();
    this.rocketChatCredentials = RocketChatCredentials.builder().build();
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(Mockito.mock(HttpServletRequest.class)));
    resetRequestAttributes();
  }

  @Test
  public void createEnquiryMessage_ShouldNot_ThrowException_When_Successful() throws Exception {

    session.setUser(user);
    session.setConsultingTypeId(0);
    session.setConsultant(null);
    session.setEnquiryMessageDate(null);
    session.setAgencyId(AGENCY_ID);
    session.setIsConsultantDirectlySet(false);
    extendedConsultingTypeResponseDTO.getWelcomeMessage().sendWelcomeMessage(false);
    extendedConsultingTypeResponseDTO.initializeFeedbackChat(false);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    var messageResponse = createMessageResponse();
    when(messageServiceProvider.postEnquiryMessage(
            any(RocketChatData.class), any(CreateEnquiryExceptionInformation.class)))
        .thenReturn(messageResponse);

    final var response =
        createEnquiryMessageFacade.createEnquiryMessage(
            new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));

    verify(userService, atLeastOnce()).updateRocketChatIdInDatabase(any(), anyString());
    verify(consultantAgencyService, atLeastOnce()).findConsultantsByAgencyId(anyLong());
    verify(consultingTypeManager, atLeastOnce()).getConsultingTypeSettings(anyInt());
    verify(messageServiceProvider, atLeastOnce())
        .postEnquiryMessage(
            any(RocketChatData.class), any(CreateEnquiryExceptionInformation.class));
    verify(messageServiceProvider, atLeastOnce())
        .postWelcomeMessageIfConfigured(any(), any(), any(), any());
    verify(sessionService, atLeastOnce()).saveSession(any());
    verify(emailNotificationFacade, atLeastOnce()).sendNewEnquiryEmailNotification(any(), any());
    assertEquals(SESSION_ID, response.getSessionId());
    assertEquals(RC_GROUP_ID, response.getRcGroupId());
    assertEquals(response.getT(), messageResponse.getT());
    resetRequestAttributes();
  }

  @Test
  public void createEnquiryMessage_Should_SendDirectEnquiryEmail_When_ConsultantDirectlySet()
      throws Exception {
    session.setUser(user);
    session.setConsultingTypeId(0);
    session.setConsultant(CONSULTANT_2);
    session.setIsConsultantDirectlySet(true);
    session.setEnquiryMessageDate(null);
    session.setAgencyId(AGENCY_ID);
    extendedConsultingTypeResponseDTO.getWelcomeMessage().sendWelcomeMessage(false);
    extendedConsultingTypeResponseDTO.initializeFeedbackChat(false);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    var messageResponse = createMessageResponse();
    when(messageServiceProvider.postEnquiryMessage(
            any(RocketChatData.class), any(CreateEnquiryExceptionInformation.class)))
        .thenReturn(messageResponse);

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));

    verify(emailNotificationFacade, never()).sendNewEnquiryEmailNotification(any(), any());
    verify(emailNotificationFacade)
        .sendNewDirectEnquiryEmailNotification(any(), any(), any(), any());

    resetRequestAttributes();
  }

  @Test(expected = ConflictException.class)
  public void createEnquiryMessage_Should_ThrowConflictException_When_EnquiryMessageAlreadySaved() {

    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(sessionService.getSession(SESSION_ID))
        .thenReturn(Optional.of(SESSION_WITH_ENQUIRY_MESSAGE));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    resetRequestAttributes();
  }

  @Test(expected = CreateEnquiryMessageException.class)
  public void
      createEnquiryMessage_Should_ThrowCreateEnquiryMessageException_When_SessionNotFoundForUser() {

    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.empty());
    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    resetRequestAttributes();
  }

  @Test(expected = CreateEnquiryMessageException.class)
  public void
      createEnquiryMessage_Should_ThrowCreateEnquiryMessageException_When_SessionIsAnonymous() {
    Session anonymousSession = new EasyRandom().nextObject(Session.class);
    anonymousSession.setRegistrationType(ANONYMOUS);
    anonymousSession.getUser().setUserId(USER.getUserId());
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(anonymousSession));

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    resetRequestAttributes();
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createEnquiryMessage_Should_ThrowInternalServerErrorException_When_GetSessionCallFails() {

    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(sessionService.getSession(SESSION_ID))
        .thenThrow(new InternalServerErrorException(MESSAGE))
        .thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    resetRequestAttributes();
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createEnquiryMessage_Should_ThrowInternalServerErrorException_When_CreationOfRocketChatGroupFailsWithAnException()
          throws RocketChatCreateGroupException {

    session.setUser(user);
    session.setConsultingTypeId(0);
    session.setConsultant(null);
    session.setEnquiryMessageDate(null);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenThrow(new RocketChatCreateGroupException(ERROR));

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));
    resetRequestAttributes();
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createEnquiryMessage_Should_ThrowInternalServerErrorException_When_PostMessageFailsWithAnException() {

    session.setUser(user);
    session.setConsultingTypeId(0);
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
    when(consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));
    resetRequestAttributes();
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createEnquiryMessage_Should_ThrowInternalServerErrorException_When_ConsultantsOfAgencyCanNotBeReadFromDB() {

    session.setUser(user);
    session.setConsultingTypeId(0);
    session.setConsultant(null);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));
    verify(rocketChatService, atLeast(1)).rollbackGroup(RC_GROUP_ID, rocketChatCredentials);
    resetRequestAttributes();
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createEnquiryMessage_Should_ThrowInternalServerErrorException_When_AddConsultantToRocketChatGroupFails()
          throws Exception {

    session.setUser(user);
    session.setConsultingTypeId(0);
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
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());

    Mockito.doThrow(new RocketChatAddUserToGroupException(MESSAGE))
        .when(rocketChatService)
        .addUserToGroup(anyString(), anyString());

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));
    resetRequestAttributes();
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroup_When_PostMessageFails() throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager.getConsultingTypeSettings(
            SESSION_WITHOUT_CONSULTANT.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NO_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(RC_POST_MESSAGE_EXCEPTION)
        .when(messageServiceProvider)
        .postEnquiryMessage(
            any(RocketChatData.class), any(CreateEnquiryExceptionInformation.class));
    when(rocketChatRoomNameGenerator.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1)).rollbackGroup(RC_GROUP_ID, RC_CREDENTIALS);
    resetRequestAttributes();
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
    when(rocketChatRoomNameGenerator.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    Mockito.doThrow(new RocketChatAddUserToGroupException(MESSAGE))
        .when(rocketChatService)
        .addUserToGroup(
            CONSULTANT_AGENCY_LIST.get(0).getConsultant().getRocketChatId(),
            GROUP_RESPONSE_DTO.getGroup().getId());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1)).rollbackGroup(RC_GROUP_ID, RC_CREDENTIALS);
    resetRequestAttributes();
  }

  @Test(expected = CreateEnquiryMessageException.class)
  public void
      createEnquiryMessage_Should_ThrowCreateEnquiryMessageException_When_KeycloakAndRocketChatUsersDontMatch() {

    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO_2);

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    resetRequestAttributes();
  }

  @Test
  public void createEnquiryMessage_Should_UpdateCorrectSessionInformation_When_Successful()
      throws Exception {

    session.setUser(user);
    session.setConsultingTypeId(0);
    session.setConsultant(null);
    session.setIsConsultantDirectlySet(false);
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);
    Session spySession = Mockito.spy(session);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(spySession));
    when(consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(messageServiceProvider.postEnquiryMessage(
            any(RocketChatData.class), any(CreateEnquiryExceptionInformation.class)))
        .thenReturn(createMessageResponse());

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));

    verify(spySession, times(1)).setGroupId(groupResponseDTO.getGroup().getId());
    verify(spySession, times(1)).setFeedbackGroupId(groupResponseDTO.getGroup().getId());
    assertNotNull(spySession.getEnquiryMessageDate());
    verify(spySession, times(1)).setStatus(SessionStatus.NEW);
    verify(sessionService, times(1)).saveSession(spySession);
    resetRequestAttributes();
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createEnquiryMessage_Should_ThrowInternalServerErrorException_When_CreatePrivateGroupWithSystemUserFails()
          throws RocketChatCreateGroupException {

    session.setUser(user);
    session.setConsultingTypeId(0);
    session.setConsultant(null);
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));
    resetRequestAttributes();
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createEnquiryMessage_Should_ThrowInternalServerErrorException_When_UpdateRocketChatIdForUserFails()
          throws RocketChatCreateGroupException {

    session.setUser(user);
    session.setConsultingTypeId(0);
    session.setConsultant(null);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));
    resetRequestAttributes();
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createEnquiryMessage_Should_ThrowInternalServerErrorException_When_UpdateOfSessionFails()
          throws RocketChatCreateGroupException {

    session.setUser(user);
    session.setConsultingTypeId(0);
    session.setConsultant(null);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);
    InternalServerErrorException internalServerErrorException =
        new InternalServerErrorException(MESSAGE);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));
    resetRequestAttributes();
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createEnquiryMessage_Should_ThrowInternalServerErrorException_When_PrivateGroupWithSystemUserIsNotPresent()
          throws RocketChatCreateGroupException {

    session.setUser(user);
    session.setConsultingTypeId(0);
    session.setConsultant(null);
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(true);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId()))
        .thenReturn(extendedConsultingTypeResponseDTO);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(any())).thenReturn(Optional.empty());

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));
    resetRequestAttributes();
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroup_WhenAddSystemUserToGroupFails() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager.getConsultingTypeSettings(
            SESSION_WITHOUT_CONSULTANT.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, atMost(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    resetRequestAttributes();
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroup_WhenRemoveSystemMessagesFails()
      throws RocketChatCreateGroupException, RocketChatRemoveSystemMessagesException,
          RocketChatUserNotInitializedException {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager.getConsultingTypeSettings(
            SESSION_WITHOUT_CONSULTANT.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    doThrow(RC_CHAT_REMOVE_SYSTEM_MESSAGES_EXCEPTION)
        .when(rocketChatService)
        .removeSystemMessages(
            Mockito.eq(GROUP_RESPONSE_DTO.getGroup().getId()), Mockito.any(), Mockito.any());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, atMost(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    resetRequestAttributes();
  }

  @Test
  public void
      createEnquiryMessage_Should_DeleteRcGroupAndFeedbackGroup_WhenAddSystemUserToFeedbackGroupFails() {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager.getConsultingTypeSettings(
            SESSION_WITHOUT_CONSULTANT.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, atMost(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, atMost(1))
        .rollbackGroup(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId(), RC_CREDENTIALS);
    resetRequestAttributes();
  }

  @Test
  public void
      createEnquiryMessage_Should_DeleteRcGroupAndFeedbackGroup_When_WhenRemoveSystemMessagesFailsForFeedbackGroup()
          throws Exception {

    when(consultingTypeManager.getConsultingTypeSettings(
            SESSION_WITHOUT_CONSULTANT.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatRoomNameGenerator.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    doThrow(RC_CHAT_REMOVE_SYSTEM_MESSAGES_EXCEPTION)
        .when(rocketChatService)
        .removeSystemMessages(
            Mockito.eq(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId()),
            Mockito.any(),
            Mockito.any());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, atMost(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, atMost(1))
        .rollbackGroup(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId(), RC_CREDENTIALS);
    resetRequestAttributes();
  }

  @Test
  public void
      createEnquiryMessage_Should_DeleteRcGroupAndFeedbackGroup_When_AddConsultantToRocketChatFeedbackGroupFails()
          throws RocketChatCreateGroupException, RocketChatAddUserToGroupException {

    when(consultingTypeManager.getConsultingTypeSettings(
            SESSION_WITHOUT_CONSULTANT.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatRoomNameGenerator.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    Mockito.doThrow(RC_ADD_USER_TO_GROUP_EXCEPTION)
        .when(rocketChatService)
        .addUserToGroup(
            CONSULTANT_AGENCY_LIST.get(0).getConsultant().getRocketChatId(),
            FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, times(1))
        .deleteGroupAsSystemUser(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());
    resetRequestAttributes();
  }

  @Test
  public void createEnquiryMessage_Should_DeleteRcGroupFeedbackGroup_When_UpdateOfSessionFails()
      throws Exception {

    Session spySession = Mockito.spy(SESSION_WITHOUT_CONSULTANT);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(spySession));
    when(consultingTypeManager.getConsultingTypeSettings(
            SESSION_WITHOUT_CONSULTANT.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    when(sessionService.saveSession(spySession)).thenThrow(INTERNAL_SERVER_ERROR_EXCEPTION);

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, times(1))
        .deleteGroupAsSystemUser(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());
    resetRequestAttributes();
  }

  @Test
  public void
      createEnquiryMessage_Should_DeleteRcGroupFeedbackGroup_When_PostWelcomeMessageFailsWithAnException()
          throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager.getConsultingTypeSettings(
            SESSION_WITHOUT_CONSULTANT.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT_AND_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    when(rocketChatRoomNameGenerator.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    CreateEnquiryExceptionInformation createEnquiryExceptionInformation =
        CreateEnquiryExceptionInformation.builder()
            .rcGroupId(GROUP_RESPONSE_DTO.getGroup().getId())
            .rcFeedbackGroupId(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId())
            .session(SESSION_WITHOUT_CONSULTANT)
            .build();
    RocketChatPostWelcomeMessageException rocketChatPostWelcomeMessageException =
        new RocketChatPostWelcomeMessageException(
            MESSAGE, EXCEPTION, createEnquiryExceptionInformation);
    doThrow(rocketChatPostWelcomeMessageException)
        .when(messageServiceProvider)
        .postWelcomeMessageIfConfigured(
            Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, times(1))
        .deleteGroupAsSystemUser(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());
    resetRequestAttributes();
  }

  @Test
  public void
      createEnquiryMessage_Should_DeleteRcGroupFeedbackGroup_When_PostMessageFailsWithAnException()
          throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager.getConsultingTypeSettings(
            SESSION_WITHOUT_CONSULTANT.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    when(rocketChatRoomNameGenerator.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    CreateEnquiryExceptionInformation createEnquiryExceptionInformation =
        CreateEnquiryExceptionInformation.builder()
            .rcGroupId(GROUP_RESPONSE_DTO.getGroup().getId())
            .rcFeedbackGroupId(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId())
            .session(SESSION_WITHOUT_CONSULTANT)
            .build();
    RocketChatPostMessageException rocketChatPostMessageException =
        new RocketChatPostMessageException(MESSAGE, createEnquiryExceptionInformation);
    doThrow(rocketChatPostMessageException)
        .when(messageServiceProvider)
        .postEnquiryMessage(
            any(RocketChatData.class), any(CreateEnquiryExceptionInformation.class));

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, times(1))
        .deleteGroupAsSystemUser(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());
    resetRequestAttributes();
  }

  @Test
  public void
      createEnquiryMessage_Should_DeleteRcGroupFeedback_When_UpdateOfUserFailsWithAnException()
          throws Exception {

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(SESSION_WITHOUT_CONSULTANT));
    when(consultingTypeManager.getConsultingTypeSettings(
            SESSION_WITHOUT_CONSULTANT.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_FEEDBACK_CHAT_AND_WELCOME_MESSAGE);
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(USER_INFO_RESPONSE_DTO);
    when(userHelper.doUsernamesMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    when(rocketChatService.createPrivateGroup(Mockito.anyString(), Mockito.eq(RC_CREDENTIALS)))
        .thenReturn(Optional.of(GROUP_RESPONSE_DTO));
    when(rocketChatService.createPrivateGroupWithSystemUser(Mockito.any()))
        .thenReturn(Optional.of(FEEDBACK_GROUP_RESPONSE_DTO_2));
    when(rocketChatRoomNameGenerator.generateGroupName(Mockito.any(Session.class)))
        .thenReturn(SESSION_WITHOUT_ENQUIRY_MESSAGE.getId().toString());
    doThrow(new IllegalArgumentException())
        .when(userService)
        .updateRocketChatIdInDatabase(USER, RC_CREDENTIALS.getRocketChatUserId());

    try {
      createEnquiryMessageFacade.createEnquiryMessage(
          new EnquiryData(USER, SESSION_ID, MESSAGE, null, RC_CREDENTIALS));
    } catch (Exception e) {
      assertThat(e, instanceOf(InternalServerErrorException.class));
    }

    verify(rocketChatService, times(1))
        .rollbackGroup(GROUP_RESPONSE_DTO.getGroup().getId(), RC_CREDENTIALS);
    verify(rocketChatService, times(1))
        .deleteGroupAsSystemUser(FEEDBACK_GROUP_RESPONSE_DTO_2.getGroup().getId());
    resetRequestAttributes();
  }

  @Test
  public void createEnquiryMessage_Should_PostAliasOnlyMessageAndWelcomeMessage() throws Exception {

    session.setUser(user);
    session.setConsultingTypeId(0);
    session.setIsConsultantDirectlySet(false);
    session.setConsultant(null);
    session.setEnquiryMessageDate(null);
    session.setAgencyId(AGENCY_ID);
    extendedConsultingTypeResponseDTO.getWelcomeMessage().setSendWelcomeMessage(true);
    extendedConsultingTypeResponseDTO.setSendFurtherStepsMessage(true);
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(false);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class)))
        .thenReturn(session.getId().toString());
    when(messageServiceProvider.postEnquiryMessage(
            any(RocketChatData.class), any(CreateEnquiryExceptionInformation.class)))
        .thenReturn(createMessageResponse());

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));

    verify(messageServiceProvider, times(1))
        .postEnquiryMessage(
            any(RocketChatData.class), any(CreateEnquiryExceptionInformation.class));
    verify(messageServiceProvider, times(1))
        .postWelcomeMessageIfConfigured(any(), any(), any(), any());
    verify(messageServiceProvider, times(1))
        .postFurtherStepsIfConfigured(anyString(), any(), any());
    resetRequestAttributes();
  }

  @Test
  public void createEnquiryMessage_Should_setSessionInProgress_When_consultantIsAlreadyAssigned()
      throws Exception {
    var session = mock(Session.class);
    when(session.getUser()).thenReturn(user);
    when(session.getConsultingTypeId()).thenReturn(0);
    when(session.getConsultant()).thenReturn(new Consultant());
    when(session.getAgencyId()).thenReturn(AGENCY_ID);
    when(session.getRegistrationType()).thenReturn(REGISTERED);
    extendedConsultingTypeResponseDTO.setInitializeFeedbackChat(false);
    groupDTO.setId(RC_GROUP_ID);
    groupResponseDTO.setSuccess(true);
    groupResponseDTO.setGroup(groupDTO);
    rocketChatUserDTO.setUsername(USERNAME);
    userInfoResponseDTO.setUser(rocketChatUserDTO);
    rocketChatCredentials.setRocketChatUserId(RC_USER_ID);
    rocketChatCredentials.setRocketChatUsername(RC_USERNAME);

    when(sessionService.getSession(SESSION_ID)).thenReturn(Optional.of(session));
    when(consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    when(rocketChatService.createPrivateGroup(anyString(), any()))
        .thenReturn(Optional.of(groupResponseDTO));
    when(rocketChatService.getUserInfo(RC_USER_ID)).thenReturn(userInfoResponseDTO);
    when(userHelper.doUsernamesMatch(anyString(), anyString())).thenReturn(true);
    when(rocketChatRoomNameGenerator.generateGroupName(any(Session.class))).thenReturn("0");
    when(messageServiceProvider.postEnquiryMessage(
            any(RocketChatData.class), any(CreateEnquiryExceptionInformation.class)))
        .thenReturn(createMessageResponse());

    createEnquiryMessageFacade.createEnquiryMessage(
        new EnquiryData(user, SESSION_ID, MESSAGE, null, rocketChatCredentials));

    verify(session).setStatus(SessionStatus.IN_PROGRESS);
    resetRequestAttributes();
  }

  private void resetRequestAttributes() {
    RequestContextHolder.setRequestAttributes(null);
  }

  private MessageResponseDTO createMessageResponse() {
    return easyRandom.nextObject(MessageResponseDTO.class);
  }
}
