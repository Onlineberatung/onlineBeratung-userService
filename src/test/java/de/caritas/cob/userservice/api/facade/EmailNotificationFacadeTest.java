package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.repository.session.RegistrationType.REGISTERED;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_VALUE_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_VALUE_ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_NAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.APPLICATION_BASE_URL;
import static de.caritas.cob.userservice.testHelper.TestConstants.APPLICATION_BASE_URL_FIELD_NAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.CITY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.DESCRIPTION;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_NOT_OFFLINE;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_NO_TEAM_AGENCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_NO_TEAM_SESSION;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_TEAM_SESSION;
import static de.caritas.cob.userservice.testHelper.TestConstants.NAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.POSTCODE;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_CONSULTANT_ENCODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_ENCODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.EmailNotificationException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.GroupChatDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.MonitoringDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.NewMessageDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.NotificationsDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.TeamSessionsDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.WelcomeMessageDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailsDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
public class EmailNotificationFacadeTest {

  private final Consultant CONSULTANT =
      new Consultant(CONSULTANT_ID, "XXX", USERNAME_CONSULTANT_ENCODED, "consultant", "consultant",
          "consultant@domain.de", false, false, null, false, 1L, null, null, null, null, null,
          null, null);
  private final Consultant CONSULTANT_WITHOUT_MAIL = new Consultant(CONSULTANT_ID, "XXX",
      "consultant", "consultant", "consultant", "", false, false, null, false, 1L, null, null, null,
      null, null, null, null);
  private final Consultant CONSULTANT2 =
      new Consultant(CONSULTANT_ID_2, "XXX", "consultant2", "consultant2", "consultant2",
          "consultant2@domain.de", false, false, null, false, 1L, null, null, null, null, null,
          null, null);
  private final Consultant CONSULTANT3 =
      new Consultant(CONSULTANT_ID_3, "XXX", "consultant3", "consultant3", "consultant3",
          "consultant3@domain.de", false, false, null, false, 1L, null, null, null, null, null,
          null, null);
  private final Consultant CONSULTANT_NO_EMAIL = new Consultant(CONSULTANT_ID, "XXX", "consultant",
      "consultant", "consultant", "", false, false, null, false, 1L, null, null, null, null, null,
      null, null);
  private final Consultant ABSENT_CONSULTANT = new Consultant("XXX", "XXX", "consultant",
      "consultant", "consultant", "consultant@domain.de", true, false, null, false, 1L, null,
      null, null, null, null, null, null);
  private final User USER = new User(USER_ID, null, USERNAME_ENCODED, "email@email.de", false);
  private final User USER_NO_EMAIL = new User(USER_ID, null, "username", "", false);
  private final ConsultantAgency CONSULTANT_AGENCY =
      new ConsultantAgency(1L, CONSULTANT, AGENCY_ID, nowInUtc(), nowInUtc(), nowInUtc());
  private final ConsultantAgency CONSULTANT_AGENCY_2 =
      new ConsultantAgency(1L, CONSULTANT2, AGENCY_ID, nowInUtc(), nowInUtc(), nowInUtc());
  private final ConsultantAgency ABSENT_CONSULTANT_AGENCY =
      new ConsultantAgency(1L, ABSENT_CONSULTANT, AGENCY_ID, nowInUtc(), nowInUtc(), nowInUtc());
  private final Session SESSION =
      new Session(1L, USER, CONSULTANT, CONSULTING_TYPE_ID_SUCHT, REGISTERED, "88045",
          AGENCY_ID, SessionStatus.INITIAL, nowInUtc(), RC_GROUP_ID, null, null,
          IS_NO_TEAM_SESSION, IS_MONITORING, false, nowInUtc(), null, null);
  private final Session SESSION_WITHOUT_CONSULTANT =
      new Session(1L, USER, null, CONSULTING_TYPE_ID_SUCHT, REGISTERED, "88045", AGENCY_ID,
          SessionStatus.NEW, nowInUtc(), RC_GROUP_ID, null, null, IS_NO_TEAM_SESSION, IS_MONITORING,
          false, nowInUtc(), null, null);
  private final Session SESSION_IN_PROGRESS = new Session(1L, USER, CONSULTANT,
      CONSULTING_TYPE_ID_SUCHT, REGISTERED, "88045", AGENCY_ID, SessionStatus.IN_PROGRESS,
      nowInUtc(), RC_GROUP_ID, null, null, IS_NO_TEAM_SESSION, IS_MONITORING, false, nowInUtc(),
      null, null);
  private final Session SESSION_IN_PROGRESS_NO_EMAIL = new Session(1L, USER_NO_EMAIL,
      CONSULTANT_NO_EMAIL, CONSULTING_TYPE_ID_SUCHT, REGISTERED, "88045", AGENCY_ID,
      SessionStatus.IN_PROGRESS, nowInUtc(), RC_GROUP_ID, null, null, IS_NO_TEAM_SESSION,
      IS_MONITORING, false, nowInUtc(), null, null);
  private final Session TEAM_SESSION =
      new Session(1L, USER, CONSULTANT, CONSULTING_TYPE_ID_SUCHT, REGISTERED, "12345", AGENCY_ID,
          SessionStatus.IN_PROGRESS, nowInUtc(), RC_GROUP_ID, null, null, IS_TEAM_SESSION,
          IS_MONITORING, false, nowInUtc(), null, null);
  private final AgencyDTO AGENCY_DTO = new AgencyDTO()
      .id(AGENCY_ID)
      .name(AGENCY_NAME)
      .postcode(POSTCODE)
      .city(CITY)
      .description(DESCRIPTION)
      .teamAgency(IS_NO_TEAM_AGENCY)
      .offline(IS_NOT_OFFLINE)
      .consultingType(0);
  private final String USER_ROLE = UserRole.USER.getValue();
  private final Set<String> USER_ROLES = new HashSet<>(Collections.singletonList(USER_ROLE));
  private final String CONSULTANT_ROLE = UserRole.CONSULTANT.getValue();
  private final Set<String> CONSULTANT_ROLES = new HashSet<>(
      Collections.singletonList(CONSULTANT_ROLE));
  private final String ERROR_MSG = "error";
  private final List<ConsultantAgency> CONSULTANT_LIST =
      Arrays.asList(CONSULTANT_AGENCY, CONSULTANT_AGENCY_2);
  private final List<ConsultantAgency> CONSULTANT_AGENCY_LIST = Collections
      .singletonList(CONSULTANT_AGENCY);
  private final List<ConsultantAgency> ABSENT_CONSULTANT_AGENCY_LIST =
      Collections.singletonList(ABSENT_CONSULTANT_AGENCY);
  private final String GROUP_MEMBER_1_RC_ID = "yzx324sdg";
  private final GroupMemberDTO GROUP_MEMBER_1 =
      new GroupMemberDTO(GROUP_MEMBER_1_RC_ID, "status1", "username1", "name1", "");
  private final String GROUP_MEMBER_2_RC_ID = "sdf33dfdsf";
  private final GroupMemberDTO GROUP_MEMBER_2 =
      new GroupMemberDTO(GROUP_MEMBER_2_RC_ID, "status2", "username2", "name2", "");
  private final List<GroupMemberDTO> GROUP_MEMBERS = Arrays.asList(GROUP_MEMBER_1, GROUP_MEMBER_2);
  private final NotificationsDTO NOTIFICATIONS_DTO_TO_ALL_TEAM_CONSULTANTS =
      new NotificationsDTO().teamSessions(
          new TeamSessionsDTO().newMessage(new NewMessageDTO().allTeamConsultants(true)));
  private final NotificationsDTO NOTIFICATIONS_DTO_TO_ASSIGNED_CONSULTANT_ONLY =
      new NotificationsDTO().teamSessions(
          new TeamSessionsDTO().newMessage(new NewMessageDTO().allTeamConsultants(false)));
  private final ExtendedConsultingTypeResponseDTO CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS =
      new ExtendedConsultingTypeResponseDTO().id(0).slug("suchtberatung")
          .excludeNonMainConsultantsFromTeamSessions(true)
          .groupChat(new GroupChatDTO().isGroupChat(false)).consultantBoundedToConsultingType(false)
          .welcomeMessage(
              new WelcomeMessageDTO().sendWelcomeMessage(false).welcomeMessageText(null))
          .sendFurtherStepsMessage(false).sendSaveSessionDataMessage(false)
          .sessionDataInitializing(null)
          .monitoring(new MonitoringDTO().initializeMonitoring(true).monitoringTemplateFile(null))
          .initializeFeedbackChat(false).notifications(NOTIFICATIONS_DTO_TO_ALL_TEAM_CONSULTANTS)
          .languageFormal(false).roles(null).registration(null);
  private final ExtendedConsultingTypeResponseDTO CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ASSIGNED_CONSULTANT_ONLY =
      new ExtendedConsultingTypeResponseDTO().id(0).slug("suchtberatung")
          .excludeNonMainConsultantsFromTeamSessions(true)
          .groupChat(new GroupChatDTO().isGroupChat(false)).consultantBoundedToConsultingType(false)
          .welcomeMessage(
              new WelcomeMessageDTO().sendWelcomeMessage(false).welcomeMessageText(null))
          .sendFurtherStepsMessage(false).sendSaveSessionDataMessage(false)
          .sessionDataInitializing(null)
          .monitoring(new MonitoringDTO().initializeMonitoring(true).monitoringTemplateFile(null))
          .initializeFeedbackChat(false)
          .notifications(NOTIFICATIONS_DTO_TO_ASSIGNED_CONSULTANT_ONLY)
          .languageFormal(false).roles(null).registration(null);

  @InjectMocks
  private EmailNotificationFacade emailNotificationFacade;
  @Mock
  private ConsultantAgencyRepository consultantAgencyRepository;
  @Mock
  private MailService mailService;
  @Mock
  private AgencyService agencyService;
  @Mock
  SessionService sessionService;
  @Mock
  ConsultantAgencyService consultantAgencyService;
  @Mock
  Logger logger;
  @Mock
  ConsultantService consultantService;
  @Mock
  RocketChatService rocketChatService;
  @Mock
  ConsultingTypeManager consultingTypeManager;
  @Mock
  @SuppressWarnings("unused")
  KeycloakAdminClientService keycloakAdminClientService;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(emailNotificationFacade,
        emailNotificationFacade.getClass().getDeclaredField(FIELD_NAME_EMAIL_DUMMY_SUFFIX),
        FIELD_VALUE_EMAIL_DUMMY_SUFFIX);
    FieldSetter.setField(emailNotificationFacade,
        emailNotificationFacade.getClass().getDeclaredField(FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID),
        FIELD_VALUE_ROCKET_CHAT_SYSTEM_USER_ID);
    FieldSetter.setField(emailNotificationFacade,
        emailNotificationFacade.getClass().getDeclaredField(APPLICATION_BASE_URL_FIELD_NAME),
        APPLICATION_BASE_URL);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  /**
   * Method: sendNewEnquiryEmailNotification
   */
  @Test
  public void sendNewEnquiryEmailNotification_Should_SendEmailNotificationViaMailServiceHelperToConsultants() {

    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(SESSION.getAgencyId()))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(agencyService.getAgency(SESSION.getAgencyId())).thenReturn(AGENCY_DTO);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(mailService, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));

  }

  @Test
  public void sendNewEnquiryEmailNotification_ShouldNot_SendEmailNotificationViaMailServiceHelperToUser() {

    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(SESSION.getAgencyId()))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(agencyService.getAgency(SESSION.getAgencyId())).thenReturn(AGENCY_DTO);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(mailService, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewEnquiryEmailNotification_Should_GetAgencyInformationFromAgencyServiceHelper() {

    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(SESSION.getAgencyId()))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(agencyService.getAgency(SESSION.getAgencyId())).thenReturn(AGENCY_DTO);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(agencyService, times(1)).getAgency(SESSION.getAgencyId());

  }

  @Test
  public void sendNewEnquiryEmailNotification_ShouldNot_SendEmailWhenConsultantAgencyListIsEmpty() {

    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(AGENCY_ID)).thenReturn(null);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewEnquiryEmailNotification_ShouldNot_SendEmailWhenConsultantIsAbsent() {

    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(AGENCY_ID))
        .thenReturn(ABSENT_CONSULTANT_AGENCY_LIST);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewEnquiryEmailNotification_Should_LogError_WhenSendEmailFails() {

    EmailNotificationException emailNotificationException =
        new EmailNotificationException(new Exception());

    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(AGENCY_ID))
        .thenThrow(emailNotificationException);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(logger, times(2)).error(anyString(), anyString(), anyString());
  }

  /**
   * Method: sendNewMessageNotification
   */
  @Test
  public void sendNewMessageNotification_Should_SendEmailNotificationViaMailServiceHelperToConsultant_WhenCalledAsUserAuthorityAndIsTeamSession() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(CONSULTANT_LIST);
    when(consultingTypeManager.getConsultingTypeSettings(TEAM_SESSION.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(consultantAgencyService, times(1)).findConsultantsByAgencyId(AGENCY_ID);
    verify(mailService, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmailNotificationToUser_WhenCalledAsUserAuthorityAndIsTeamSession() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(CONSULTANT_LIST);
    when(consultingTypeManager.getConsultingTypeSettings(TEAM_SESSION.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(consultantAgencyService, times(1)).findConsultantsByAgencyId(AGENCY_ID);
    verify(mailService, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmail_WhenMailListIsEmptyAndCalledAsUserAuthorityAndIsTeamSession() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(null);
    when(consultingTypeManager.getConsultingTypeSettings(TEAM_SESSION.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_Should_LogError_WhenSessionServiceFails() {

    InternalServerErrorException serviceException = new InternalServerErrorException(ERROR_MSG);

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenThrow(serviceException);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);
    verify(logger, times(2)).error(anyString(), anyString(), anyString());
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmailAndLogEmailNotificationFacadeError_WhenSessionIsNullOrEmpty() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(null);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmailAndLogEmailNotificationFacadeError_WhenSessionIsNotInProgress() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(SESSION);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmail_WhenCalledAsUserAuthorityAndIsSingleSessionAndConsultantHasNoEmailProvided() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(SESSION_IN_PROGRESS_NO_EMAIL);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_Should_SendEmailNotificationViaMailServiceHelper_WhenCalledAsUserAuthorityAndIsSingleSession() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(SESSION_IN_PROGRESS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailService, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmailAndLogEmailNotificationFacadeWarning_When_GetSessionFails() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenThrow(new NotFoundException(ERROR_MSG));

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, CONSULTANT_ROLES,
        CONSULTANT_ID);

    verify(mailService, times(0))
        .sendEmailNotification(Mockito.any(MailsDTO.class));
    verify(logger, atLeastOnce()).warn(anyString(), anyString(), anyString());
  }

  @Test
  public void sendNewMessageNotification_Should_LogEmailNotificationFacadeError_When_ErrorOccursDuringMailTransmission() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenReturn(SESSION_IN_PROGRESS);
    doThrow(new NullPointerException())
        .when(mailService).sendEmailNotification(any());

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, CONSULTANT_ROLES,
        CONSULTANT_ID);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmail_WhenCalledAsConsultantAuthorityAndAskerHasNoEmailProvided() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenReturn(SESSION_IN_PROGRESS_NO_EMAIL);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, CONSULTANT_ROLES,
        CONSULTANT_ID);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_Should_SendEmailToUserWithEncodedUsernames_WhenCalledAsConsultantAuthorityAndAskerHasEmail() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenReturn(SESSION_IN_PROGRESS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, CONSULTANT_ROLES,
        CONSULTANT_ID);

    verify(mailService, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_Should_SendEmailToAllConsultants_WhenIsTeamSessionAndConsultingTypeSettingsToSendToAllTeamConsultantsIsTrue() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultingTypeManager.getConsultingTypeSettings(TEAM_SESSION.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(CONSULTANT_LIST);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailService, times(1)).sendEmailNotification(Mockito.any());
  }

  @Test
  public void sendNewMessageNotification_Should_SendEmailToAssignConsultantOnly_WhenIsTeamSessionAndConsultingTypeSettingsToSendToAllTeamConsultantsIsFalse() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultingTypeManager.getConsultingTypeSettings(TEAM_SESSION.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ASSIGNED_CONSULTANT_ONLY);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailService, times(1)).sendEmailNotification(Mockito.any());
  }

  /**
   * Method: sendNewFeedbackMessageNotification
   */
  @Test
  public void sendNewFeedbackMessageNotification_Should_SendEmailToAllFeedbackChatGroupMembersWithDecodedUsernames_WhenAssignedConsultantWroteAFeedbackMessage()
      throws Exception {

    when(consultantService.getConsultant(CONSULTANT_ID)).thenReturn(Optional.of(CONSULTANT));
    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID)).thenReturn(SESSION);
    when(rocketChatService.getMembersOfGroup(RC_FEEDBACK_GROUP_ID)).thenReturn(GROUP_MEMBERS);
    when(consultantService.getConsultantByRcUserId(GROUP_MEMBER_1_RC_ID))
        .thenReturn(Optional.of(CONSULTANT2));
    when(consultantService.getConsultantByRcUserId(GROUP_MEMBER_2_RC_ID))
        .thenReturn(Optional.of(CONSULTANT3));

    emailNotificationFacade.sendNewFeedbackMessageNotification(RC_FEEDBACK_GROUP_ID, CONSULTANT_ID);

    verify(mailService, times(1)).sendEmailNotification(Mockito.any());

  }

  @Test
  public void sendNewFeedbackMessageNotification_Should_SendEmailToAssignedConsultantWithDecodedUsername_WhenOtherConsultantWrote() {

    when(consultantService.getConsultant(CONSULTANT_ID_2)).thenReturn(Optional.of(CONSULTANT2));
    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID)).thenReturn(SESSION);

    emailNotificationFacade.sendNewFeedbackMessageNotification(RC_FEEDBACK_GROUP_ID,
        CONSULTANT_ID_2);

    verify(mailService, times(1)).sendEmailNotification(Mockito.any());

  }

  @Test
  public void sendNewFeedbackMessageNotification_Should_LogErrorAndSendNoMails_WhenCallingConsultantIsNotFound() {

    emailNotificationFacade.sendNewFeedbackMessageNotification(RC_FEEDBACK_GROUP_ID,
        CONSULTANT_ID);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());

  }

  @Test
  public void sendNewFeedbackMessageNotification_Should_LogErrorAndSendNoMails_WhenSessionIsNotFound() {

    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID)).thenReturn(null);

    emailNotificationFacade.sendNewFeedbackMessageNotification(RC_FEEDBACK_GROUP_ID,
        CONSULTANT_ID);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());

  }

  @Test
  public void sendNewFeedbackMessageNotification_Should_LogErrorAndSendNoMails_WhenNoConsultantIsAssignedToSession() {

    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID))
        .thenReturn(SESSION_WITHOUT_CONSULTANT);

    emailNotificationFacade.sendNewFeedbackMessageNotification(RC_FEEDBACK_GROUP_ID,
        CONSULTANT_ID);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());

  }

  @Test
  public void sendAssignEnquiryEmailNotification_Should_SendEmail_WhenAllParametersAreValid() {

    when(consultantService.getConsultant(CONSULTANT_ID_2)).thenReturn(Optional.of(CONSULTANT2));

    emailNotificationFacade.sendAssignEnquiryEmailNotification(CONSULTANT, CONSULTANT_ID_2,
        USERNAME);

    verify(mailService, times(1)).sendEmailNotification(Mockito.any());
  }

  @Test
  public void sendAssignEnquiryEmailNotification_Should_LogErrorAndSendNoMails_WhenReceiverConsultantIsNull() {

    emailNotificationFacade.sendAssignEnquiryEmailNotification(null, CONSULTANT_ID_2, USERNAME);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void sendAssignEnquiryEmailNotification_Should_LogErrorAndSendNoMails_WhenReceiverConsultantIsMissingEmailAddress() {

    emailNotificationFacade.sendAssignEnquiryEmailNotification(CONSULTANT_WITHOUT_MAIL,
        CONSULTANT_ID_2, USERNAME);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void sendAssignEnquiryEmailNotification_Should_LogErrorAndSendNoMails_WhenSenderConsultantIsNotFound() {

    when(consultantService.getConsultant(Mockito.anyString())).thenReturn(Optional.empty());

    emailNotificationFacade.sendAssignEnquiryEmailNotification(CONSULTANT, CONSULTANT_ID_2,
        USERNAME);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_LogError_When_SessionStatusIsNew() {
    Session session = mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.NEW);
    when(session.getUser()).thenReturn(USER);

    when(sessionService.getSessionByGroupIdAndUser(any(), any(), any())).thenReturn(session);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verifyNoInteractions(logger);
  }

  @Test
  public void sendAssignEnquiryEmailNotification_Should_LogError_When_MailServiceHelperThrowsException() {
    doThrow(new RuntimeException("unexpected")).when(mailService)
        .sendEmailNotification(any());
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(CONSULTANT));

    emailNotificationFacade.sendAssignEnquiryEmailNotification(CONSULTANT, USER_ID, NAME);

    verify(logger, times(1)).error(anyString(), anyString(), contains("unexpected"));
  }

  @Test
  public void sendAssignEnquiryEmailNotification_Should_LogError_When_SessionServiceThrowsRuntimeException() {
    when(sessionService.getSessionByFeedbackGroupId(any())).thenThrow(new RuntimeException(""));

    emailNotificationFacade.sendNewFeedbackMessageNotification(GROUP_MEMBER_1_RC_ID, USER_ID);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

}
