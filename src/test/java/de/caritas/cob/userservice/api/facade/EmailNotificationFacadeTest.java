package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.REGISTERED;
import static de.caritas.cob.userservice.api.testHelper.AsyncVerification.verifyAsync;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_VALUE_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.userservice.api.testHelper.FieldConstants.FIELD_VALUE_ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.APPLICATION_BASE_URL;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.APPLICATION_BASE_URL_FIELD_NAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ID_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ID_3;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.IS_NO_TEAM_SESSION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.IS_TEAM_SESSION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.NAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_CONSULTANT_ENCODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_ENCODED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import com.google.api.client.util.Lists;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ReassignmentNotificationDTO;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.EmailNotificationException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.json.JsonSerializationUtils;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggle;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggleService;
import de.caritas.cob.userservice.api.service.emailsupplier.AssignEnquiryEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewDirectEnquiryEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewEnquiryEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewFeedbackEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.NewMessageEmailSupplier;
import de.caritas.cob.userservice.api.service.emailsupplier.TenantTemplateSupplier;
import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.GroupChatDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.NewMessageDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.NotificationsDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.TeamSessionsDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.WelcomeMessageDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailsDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class EmailNotificationFacadeTest {

  private final Consultant CONSULTANT =
      new Consultant(
          CONSULTANT_ID,
          "XXX",
          USERNAME_CONSULTANT_ENCODED,
          "consultant",
          "consultant",
          "consultant@domain.de",
          false,
          false,
          null,
          false,
          null,
          1L,
          null,
          null,
          null,
          null,
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);
  private final Consultant CONSULTANT_WITHOUT_MAIL =
      new Consultant(
          CONSULTANT_ID,
          "XXX",
          "consultant",
          "consultant",
          "consultant",
          "",
          false,
          false,
          null,
          false,
          null,
          1L,
          null,
          null,
          null,
          null,
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);
  private final Consultant CONSULTANT2 =
      new Consultant(
          CONSULTANT_ID_2,
          "XXX",
          "consultant2",
          "consultant2",
          "consultant2",
          "consultant2@domain.de",
          false,
          false,
          null,
          false,
          null,
          1L,
          null,
          null,
          null,
          null,
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);
  private final Consultant CONSULTANT3 =
      new Consultant(
          CONSULTANT_ID_3,
          "XXX",
          "consultant3",
          "consultant3",
          "consultant3",
          "consultant3@domain.de",
          false,
          false,
          null,
          false,
          null,
          1L,
          null,
          null,
          null,
          null,
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);
  private final Consultant CONSULTANT_NO_EMAIL =
      new Consultant(
          CONSULTANT_ID,
          "XXX",
          "consultant",
          "consultant",
          "consultant",
          "",
          false,
          false,
          null,
          false,
          null,
          1L,
          null,
          null,
          null,
          null,
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);
  private final Consultant ABSENT_CONSULTANT =
      new Consultant(
          "XXX",
          "XXX",
          "consultant",
          "consultant",
          "consultant",
          "consultant@domain.de",
          true,
          false,
          null,
          false,
          null,
          1L,
          null,
          null,
          null,
          null,
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);
  private final User USER = new User(USER_ID, null, USERNAME_ENCODED, "email@email.de", false);
  private final User USER_NO_EMAIL = new User(USER_ID, null, "username", "", false);
  private final ConsultantAgency CONSULTANT_AGENCY =
      new ConsultantAgency(
          1L, CONSULTANT, AGENCY_ID, nowInUtc(), nowInUtc(), nowInUtc(), null, null);
  private final ConsultantAgency CONSULTANT_AGENCY_2 =
      new ConsultantAgency(
          1L, CONSULTANT2, AGENCY_ID, nowInUtc(), nowInUtc(), nowInUtc(), null, null);
  private final ConsultantAgency ABSENT_CONSULTANT_AGENCY =
      new ConsultantAgency(
          1L, ABSENT_CONSULTANT, AGENCY_ID, nowInUtc(), nowInUtc(), nowInUtc(), null, null);
  private final Session SESSION =
      Session.builder()
          .id(1L)
          .user(USER)
          .consultant(CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .postcode("88045")
          .agencyId(AGENCY_ID)
          .status(SessionStatus.INITIAL)
          .enquiryMessageDate(nowInUtc())
          .groupId(RC_GROUP_ID)
          .teamSession(IS_NO_TEAM_SESSION)
          .createDate(nowInUtc())
          .build();

  private final Session SESSION_WITHOUT_CONSULTANT =
      Session.builder()
          .id(1L)
          .user(USER)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .postcode("88045")
          .agencyId(AGENCY_ID)
          .status(SessionStatus.INITIAL)
          .enquiryMessageDate(nowInUtc())
          .groupId(RC_GROUP_ID)
          .teamSession(IS_NO_TEAM_SESSION)
          .createDate(nowInUtc())
          .build();

  private final Session SESSION_IN_PROGRESS =
      Session.builder()
          .id(1L)
          .user(USER)
          .consultant(CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .postcode("88045")
          .agencyId(AGENCY_ID)
          .status(SessionStatus.IN_PROGRESS)
          .enquiryMessageDate(nowInUtc())
          .groupId(RC_GROUP_ID)
          .teamSession(IS_NO_TEAM_SESSION)
          .createDate(nowInUtc())
          .build();

  private final Session SESSION_IN_PROGRESS_NO_EMAIL =
      Session.builder()
          .id(1L)
          .user(USER_NO_EMAIL)
          .consultant(CONSULTANT_NO_EMAIL)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .postcode("88045")
          .agencyId(AGENCY_ID)
          .status(SessionStatus.IN_PROGRESS)
          .enquiryMessageDate(nowInUtc())
          .groupId(RC_GROUP_ID)
          .teamSession(IS_NO_TEAM_SESSION)
          .createDate(nowInUtc())
          .build();

  private final Session TEAM_SESSION =
      Session.builder()
          .id(1L)
          .user(USER)
          .consultant(CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .postcode("88045")
          .agencyId(AGENCY_ID)
          .status(SessionStatus.IN_PROGRESS)
          .enquiryMessageDate(nowInUtc())
          .groupId(RC_GROUP_ID)
          .teamSession(IS_TEAM_SESSION)
          .createDate(nowInUtc())
          .build();

  private final String USER_ROLE = UserRole.USER.getValue();
  private final Set<String> USER_ROLES = new HashSet<>(Collections.singletonList(USER_ROLE));
  private final String CONSULTANT_ROLE = UserRole.CONSULTANT.getValue();
  private final Set<String> CONSULTANT_ROLES =
      new HashSet<>(Collections.singletonList(CONSULTANT_ROLE));
  private final String ERROR_MSG = "error";
  private final List<ConsultantAgency> CONSULTANT_LIST =
      Arrays.asList(CONSULTANT_AGENCY, CONSULTANT_AGENCY_2);

  private final String GROUP_MEMBER_1_RC_ID = "yzx324sdg";
  private final GroupMemberDTO GROUP_MEMBER_1 =
      new GroupMemberDTO(GROUP_MEMBER_1_RC_ID, "status1", "username1", "name1", "");
  private final String GROUP_MEMBER_2_RC_ID = "sdf33dfdsf";
  private final GroupMemberDTO GROUP_MEMBER_2 =
      new GroupMemberDTO(GROUP_MEMBER_2_RC_ID, "status2", "username2", "name2", "");
  private final List<GroupMemberDTO> GROUP_MEMBERS = Arrays.asList(GROUP_MEMBER_1, GROUP_MEMBER_2);
  private final NotificationsDTO NOTIFICATIONS_DTO_TO_ALL_TEAM_CONSULTANTS =
      new NotificationsDTO()
          .teamSessions(
              new TeamSessionsDTO().newMessage(new NewMessageDTO().allTeamConsultants(true)));
  private final NotificationsDTO NOTIFICATIONS_DTO_TO_ASSIGNED_CONSULTANT_ONLY =
      new NotificationsDTO()
          .teamSessions(
              new TeamSessionsDTO().newMessage(new NewMessageDTO().allTeamConsultants(false)));
  private final ExtendedConsultingTypeResponseDTO
      CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS =
          new ExtendedConsultingTypeResponseDTO()
              .id(0)
              .slug("suchtberatung")
              .excludeNonMainConsultantsFromTeamSessions(true)
              .groupChat(new GroupChatDTO().isGroupChat(false))
              .consultantBoundedToConsultingType(false)
              .welcomeMessage(
                  new WelcomeMessageDTO().sendWelcomeMessage(false).welcomeMessageText(null))
              .sendFurtherStepsMessage(false)
              .sessionDataInitializing(null)
              .initializeFeedbackChat(false)
              .notifications(NOTIFICATIONS_DTO_TO_ALL_TEAM_CONSULTANTS)
              .languageFormal(false)
              .roles(null)
              .registration(null);
  private final ExtendedConsultingTypeResponseDTO
      CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ASSIGNED_CONSULTANT_ONLY =
          new ExtendedConsultingTypeResponseDTO()
              .id(0)
              .slug("suchtberatung")
              .excludeNonMainConsultantsFromTeamSessions(true)
              .groupChat(new GroupChatDTO().isGroupChat(false))
              .consultantBoundedToConsultingType(false)
              .welcomeMessage(
                  new WelcomeMessageDTO().sendWelcomeMessage(false).welcomeMessageText(null))
              .sendFurtherStepsMessage(false)
              .sessionDataInitializing(null)
              .initializeFeedbackChat(false)
              .notifications(NOTIFICATIONS_DTO_TO_ASSIGNED_CONSULTANT_ONLY)
              .languageFormal(false)
              .roles(null)
              .registration(null);

  @InjectMocks private EmailNotificationFacade emailNotificationFacade;

  @Mock private NewEnquiryEmailSupplier newEnquiryEmailSupplier;

  @SuppressWarnings("unused")
  @Mock
  private NewDirectEnquiryEmailSupplier newDirectEnquiryEmailSupplier;

  @Spy private AssignEnquiryEmailSupplier assignEnquiryEmailSupplier;
  @Mock private ConsultantAgencyRepository consultantAgencyRepository;
  @Mock private MailService mailService;
  @Mock private AgencyService agencyService;
  @Mock SessionService sessionService;
  @Mock ConsultantAgencyService consultantAgencyService;
  @Mock Logger logger;
  @Mock ConsultantService consultantService;
  @Mock RocketChatService rocketChatService;
  @Mock ConsultingTypeManager consultingTypeManager;
  @Mock IdentityClientConfig identityClientConfig;
  @Mock ReleaseToggleService releaseToggleService;

  @Mock
  @SuppressWarnings("unused")
  KeycloakService keycloakService;

  @Mock TenantTemplateSupplier tenantTemplateSupplier;

  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    when(identityClientConfig.getEmailDummySuffix()).thenReturn(FIELD_VALUE_EMAIL_DUMMY_SUFFIX);
    ReflectionTestUtils.setField(
        emailNotificationFacade,
        FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID,
        FIELD_VALUE_ROCKET_CHAT_SYSTEM_USER_ID);
    ReflectionTestUtils.setField(
        emailNotificationFacade, APPLICATION_BASE_URL_FIELD_NAME, APPLICATION_BASE_URL);
    ReflectionTestUtils.setField(
        assignEnquiryEmailSupplier, "consultantService", consultantService);
    setInternalState(EmailNotificationFacade.class, "log", logger);
    setInternalState(AssignEnquiryEmailSupplier.class, "log", logger);
    setInternalState(NewFeedbackEmailSupplier.class, "log", logger);
    setInternalState(NewMessageEmailSupplier.class, "log", logger);
    when(releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS))
        .thenReturn(false);
  }

  @Test
  public void
      sendNewEnquiryEmailNotification_Should_SendEmailNotificationViaMailServiceHelperToConsultants() {
    givenNewEnquiryMailSupplierReturnNonEmptyMails();
    var session = givenEnquirySession();

    emailNotificationFacade.sendNewEnquiryEmailNotification(session, null);

    verify(mailService).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void
      sendNewEnquiryEmailNotification_ShouldNot_SendEmailNotificationViaMailServiceHelperToUser() {
    givenNewEnquiryMailSupplierReturnNonEmptyMails();
    var session = givenEnquirySession();

    emailNotificationFacade.sendNewEnquiryEmailNotification(session, null);

    verify(mailService).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewEnquiryEmailNotification_Should_SetCurrentTenantContextFromSession() {
    assertThat(TenantContext.getCurrentTenant()).isNull();
    givenNewEnquiryMailSupplierReturnNonEmptyMails();
    var session = givenEnquirySession();

    session.setTenantId(1L);
    emailNotificationFacade.sendNewEnquiryEmailNotification(session, null);

    verify(mailService).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  private Session givenEnquirySession() {
    var session = new EasyRandom().nextObject(Session.class);
    session.setConsultant(null);
    return session;
  }

  private void givenNewEnquiryMailSupplierReturnNonEmptyMails() {
    List<MailDTO> mails = getMailDTOS();
    when(newEnquiryEmailSupplier.generateEmails()).thenReturn(mails);
  }

  private List<MailDTO> getMailDTOS() {
    List<MailDTO> mails = Lists.newArrayList();
    mails.add(new MailDTO());
    return mails;
  }

  @Test
  public void sendNewEnquiryEmailNotification_ShouldNot_SendEmailWhenGeneratedEmailListIsEmpty() {
    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION, null);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewEnquiryEmailNotification_Should_LogError_WhenSendEmailFails() {
    var session = givenEnquirySession();
    EmailNotificationException emailNotificationException =
        new EmailNotificationException(new Exception());
    when(newEnquiryEmailSupplier.generateEmails()).thenThrow(emailNotificationException);

    emailNotificationFacade.sendNewEnquiryEmailNotification(session, null);

    verify(logger).error(anyString(), any(), any(Exception.class));
  }

  /** Method: sendNewMessageNotification */
  @Test
  public void
      sendNewMessageNotification_Should_SendEmailNotificationViaMailServiceHelperToConsultant_WhenCalledAsUserAuthorityAndIsTeamSession() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(CONSULTANT_LIST);
    when(consultingTypeManager.getConsultingTypeSettings(TEAM_SESSION.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);

    verify(consultantAgencyService).findConsultantsByAgencyId(AGENCY_ID);
    verify(mailService).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void
      sendNewMessageNotification_ShouldNot_SendEmailNotificationToUser_WhenCalledAsUserAuthorityAndIsTeamSession() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(CONSULTANT_LIST);
    when(consultingTypeManager.getConsultingTypeSettings(TEAM_SESSION.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);

    verify(consultantAgencyService).findConsultantsByAgencyId(AGENCY_ID);
    verify(mailService).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void
      sendNewMessageNotification_ShouldNot_SendEmail_WhenMailListIsEmptyAndCalledAsUserAuthorityAndIsTeamSession() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(null);
    when(consultingTypeManager.getConsultingTypeSettings(TEAM_SESSION.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_Should_LogError_WhenSessionServiceFails() {
    InternalServerErrorException serviceException = new InternalServerErrorException(ERROR_MSG);
    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenThrow(serviceException);
    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);
    verify(logger)
        .error(anyString(), anyString(), anyString(), any(InternalServerErrorException.class));
  }

  @Test
  public void
      sendNewMessageNotification_ShouldNot_SendEmailAndLogEmailNotificationFacadeError_WhenSessionIsNullOrEmpty() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(null);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void
      sendNewMessageNotification_ShouldNot_SendEmailAndLogEmailNotificationFacadeError_WhenSessionIsNotInProgress() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(SESSION);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void
      sendNewMessageNotification_ShouldNot_SendEmail_WhenCalledAsUserAuthorityAndIsSingleSessionAndConsultantHasNoEmailProvided() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(SESSION_IN_PROGRESS_NO_EMAIL);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void
      sendNewMessageNotification_Should_SendEmailNotificationViaMailServiceHelper_WhenCalledAsUserAuthorityAndIsSingleSession() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(SESSION_IN_PROGRESS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);

    verify(mailService).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void
      sendNewMessageNotification_ShouldNot_SendEmailAndLogEmailNotificationFacadeWarning_When_GetSessionFails() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenThrow(new NotFoundException(ERROR_MSG));

    emailNotificationFacade.sendNewMessageNotification(
        RC_GROUP_ID, CONSULTANT_ROLES, CONSULTANT_ID, null);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verify(logger, atLeastOnce()).warn(anyString(), anyString(), anyString(), any(Exception.class));
  }

  @Test
  public void
      sendNewMessageNotification_Should_LogEmailNotificationFacadeError_When_ErrorOccursDuringMailTransmission() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenReturn(SESSION_IN_PROGRESS);
    doThrow(new NullPointerException()).when(mailService).sendEmailNotification(any());

    emailNotificationFacade.sendNewMessageNotification(
        RC_GROUP_ID, CONSULTANT_ROLES, CONSULTANT_ID, null);

    verify(logger, atLeastOnce())
        .error(anyString(), anyString(), anyString(), any(NullPointerException.class));
  }

  @Test
  public void
      sendNewMessageNotification_ShouldNot_SendEmail_WhenCalledAsConsultantAuthorityAndAskerHasNoEmailProvided() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenReturn(SESSION_IN_PROGRESS_NO_EMAIL);

    emailNotificationFacade.sendNewMessageNotification(
        RC_GROUP_ID, CONSULTANT_ROLES, CONSULTANT_ID, null);

    verify(mailService, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void
      sendNewMessageNotification_Should_SendEmailToUserWithEncodedUsernames_WhenCalledAsConsultantAuthorityAndAskerHasEmail() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenReturn(SESSION_IN_PROGRESS);

    emailNotificationFacade.sendNewMessageNotification(
        RC_GROUP_ID, CONSULTANT_ROLES, CONSULTANT_ID, null);

    verify(mailService).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void
      sendNewMessageNotification_Should_SendEmailToAllConsultants_WhenIsTeamSessionAndConsultingTypeSettingsToSendToAllTeamConsultantsIsTrue() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultingTypeManager.getConsultingTypeSettings(TEAM_SESSION.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(CONSULTANT_LIST);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);

    verify(mailService).sendEmailNotification(Mockito.any());
  }

  @Test
  public void
      sendNewMessageNotification_Should_SendEmailToAssignConsultantOnly_WhenIsTeamSessionAndConsultingTypeSettingsToSendToAllTeamConsultantsIsFalse() {

    when(sessionService.getSessionByGroupIdAndUser(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultingTypeManager.getConsultingTypeSettings(TEAM_SESSION.getConsultingTypeId()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ASSIGNED_CONSULTANT_ONLY);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);

    verify(mailService).sendEmailNotification(Mockito.any());
  }

  /** Method: sendNewFeedbackMessageNotification */
  @Test
  public void
      sendNewFeedbackMessageNotification_Should_SendEmailToAllFeedbackChatGroupMembersWithDecodedUsernames_WhenAssignedConsultantWroteAFeedbackMessage() {
    when(consultantService.getConsultant(CONSULTANT_ID)).thenReturn(Optional.of(CONSULTANT));
    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID)).thenReturn(SESSION);
    when(rocketChatService.getChatUsers(RC_FEEDBACK_GROUP_ID)).thenReturn(GROUP_MEMBERS);
    when(consultantService.getConsultantByRcUserId(GROUP_MEMBER_1_RC_ID))
        .thenReturn(Optional.of(CONSULTANT2));
    when(consultantService.getConsultantByRcUserId(GROUP_MEMBER_2_RC_ID))
        .thenReturn(Optional.of(CONSULTANT3));

    emailNotificationFacade.sendNewFeedbackMessageNotification(
        RC_FEEDBACK_GROUP_ID, CONSULTANT_ID, null);

    verify(mailService).sendEmailNotification(Mockito.any());
  }

  @Test
  public void
      sendNewFeedbackMessageNotification_Should_SendEmailToAssignedConsultantWithDecodedUsername_WhenOtherConsultantWrote() {

    when(consultantService.getConsultant(CONSULTANT_ID_2)).thenReturn(Optional.of(CONSULTANT2));
    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID)).thenReturn(SESSION);

    emailNotificationFacade.sendNewFeedbackMessageNotification(
        RC_FEEDBACK_GROUP_ID, CONSULTANT_ID_2, null);

    verify(mailService).sendEmailNotification(Mockito.any());
  }

  @Test
  public void
      sendNewFeedbackMessageNotification_Should_LogErrorAndSendNoMails_WhenCallingConsultantIsNotFound() {

    emailNotificationFacade.sendNewFeedbackMessageNotification(
        RC_FEEDBACK_GROUP_ID, CONSULTANT_ID, null);

    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  public void
      sendNewFeedbackMessageNotification_Should_LogErrorAndSendNoMails_WhenSessionIsNotFound() {

    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID)).thenReturn(null);

    emailNotificationFacade.sendNewFeedbackMessageNotification(
        RC_FEEDBACK_GROUP_ID, CONSULTANT_ID, null);

    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  public void
      sendNewFeedbackMessageNotification_Should_LogErrorAndSendNoMails_WhenNoConsultantIsAssignedToSession() {

    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID))
        .thenReturn(SESSION_WITHOUT_CONSULTANT);

    emailNotificationFacade.sendNewFeedbackMessageNotification(
        RC_FEEDBACK_GROUP_ID, CONSULTANT_ID, null);

    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  public void sendAssignEnquiryEmailNotification_Should_SendEmail_WhenAllParametersAreValid() {

    when(consultantService.getConsultant(CONSULTANT_ID_2)).thenReturn(Optional.of(CONSULTANT2));
    emailNotificationFacade.sendAssignEnquiryEmailNotification(
        CONSULTANT, CONSULTANT_ID_2, USERNAME, null);

    verify(mailService).sendEmailNotification(Mockito.any());
  }

  @Test
  public void
      sendAssignEnquiryEmailNotification_Should_LogErrorAndSendNoMails_WhenReceiverConsultantIsNull() {
    emailNotificationFacade.sendAssignEnquiryEmailNotification(
        null, CONSULTANT_ID_2, USERNAME, null);
    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  public void
      sendAssignEnquiryEmailNotification_Should_LogErrorAndSendNoMails_WhenReceiverConsultantIsMissingEmailAddress() {
    emailNotificationFacade.sendAssignEnquiryEmailNotification(
        CONSULTANT_WITHOUT_MAIL, CONSULTANT_ID_2, USERNAME, null);

    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  public void
      sendAssignEnquiryEmailNotification_Should_LogErrorAndSendNoMails_WhenSenderConsultantIsNotFound() {

    when(consultantService.getConsultant(Mockito.anyString())).thenReturn(Optional.empty());
    emailNotificationFacade.sendAssignEnquiryEmailNotification(
        CONSULTANT, CONSULTANT_ID_2, USERNAME, null);

    verify(logger, atLeastOnce()).error(anyString(), anyString());
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_LogError_When_SessionStatusIsNew() {
    Session session = mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.NEW);
    when(session.getUser()).thenReturn(USER);

    when(sessionService.getSessionByGroupIdAndUser(any(), any(), any())).thenReturn(session);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID, null);

    verifyNoInteractions(logger);
  }

  @Test
  public void
      sendAssignEnquiryEmailNotification_Should_LogError_When_MailServiceHelperThrowsException() {
    doThrow(new RuntimeException("unexpected")).when(mailService).sendEmailNotification(any());
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(CONSULTANT));
    emailNotificationFacade.sendAssignEnquiryEmailNotification(CONSULTANT, USER_ID, NAME, null);
    verify(logger).error(anyString(), any(RuntimeException.class));
  }

  @Test
  public void
      sendAssignEnquiryEmailNotification_Should_LogError_When_SessionServiceThrowsRuntimeException() {
    when(sessionService.getSessionByFeedbackGroupId(any())).thenThrow(new RuntimeException(""));

    emailNotificationFacade.sendNewFeedbackMessageNotification(GROUP_MEMBER_1_RC_ID, USER_ID, null);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), any(Exception.class));
  }

  @Test
  public void
      sendNewEnquiryEmailNotification_Should_notSendAnyMail_When_sessionHasAlreadyAConsultantAssigned() {
    emailNotificationFacade.sendNewEnquiryEmailNotification(
        new EasyRandom().nextObject(Session.class), null);

    verifyNoInteractions(newEnquiryEmailSupplier);
  }

  @Test
  public void sendReassignRequestNotification_Should_SendEmail_When_askerHasValidMailAddress() {
    var session = new EasyRandom().nextObject(Session.class);
    when(sessionService.getSessionByGroupId(any())).thenReturn(session);
    session.getUser().setEmail("mail@valid.de");
    session
        .getUser()
        .setNotificationsSettings(
            JsonSerializationUtils.serializeToJsonString(new NotificationsSettingsDTO()));

    emailNotificationFacade.sendReassignRequestNotification("id", null);

    verify(mailService).sendEmailNotification(Mockito.any());
  }

  @Test
  public void sendReassignRequestNotification_ShouldNot_SendEmail_When_askerHasDummyMailAddress() {
    var session = new EasyRandom().nextObject(Session.class);
    when(sessionService.getSessionByGroupId(any())).thenReturn(session);
    session.getUser().setEmail("mail@" + FIELD_VALUE_EMAIL_DUMMY_SUFFIX);

    emailNotificationFacade.sendReassignRequestNotification("id", null);

    verifyNoInteractions(mailService);
  }

  @Test
  public void
      sendReassignRequestNotification_Should_SendEmail_When_NewNotificationModeEnabledAndAskerDoesNotWantToReceiveNotifications() {
    var session = new EasyRandom().nextObject(Session.class);
    when(sessionService.getSessionByGroupId(any())).thenReturn(session);
    session.getUser().setEmail("mail@valid.de");
    session
        .getUser()
        .setNotificationsSettings(
            JsonSerializationUtils.serializeToJsonString(
                new NotificationsSettingsDTO().reassignmentNotificationEnabled(false)));
    when(releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS))
        .thenReturn(true);

    emailNotificationFacade.sendReassignRequestNotification("id", null);

    verifyNoInteractions(mailService);
  }

  @Test
  public void sendReassignConfirmationNotification_Should_sendEmail_When_consultantsExists() {
    var randomConsultant = new EasyRandom().nextObject(Consultant.class);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(randomConsultant));
    var reassignmentNotification = new EasyRandom().nextObject(ReassignmentNotificationDTO.class);
    randomConsultant.setNotificationsSettings(
        JsonSerializationUtils.serializeToJsonString(new NotificationsSettingsDTO()));

    emailNotificationFacade.sendReassignConfirmationNotification(reassignmentNotification, null);

    verifyAsync(a -> mailService.sendEmailNotification(Mockito.any()));
  }

  @Test
  public void
      sendReassignConfirmationNotification_Should_sendNotEmail_When_newEmailNotificationsEnabledAndConsultantsDoesNotWantToReceiveNotifications() {
    var randomConsultant = new EasyRandom().nextObject(Consultant.class);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(randomConsultant));
    randomConsultant.setNotificationsSettings(
        JsonSerializationUtils.serializeToJsonString(
            new NotificationsSettingsDTO().reassignmentNotificationEnabled(false)));
    var reassignmentNotification = new EasyRandom().nextObject(ReassignmentNotificationDTO.class);
    randomConsultant.setNotificationsSettings(
        JsonSerializationUtils.serializeToJsonString(new NotificationsSettingsDTO()));
    when(releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS))
        .thenReturn(true);

    emailNotificationFacade.sendReassignConfirmationNotification(reassignmentNotification, null);

    verifyAsync(a -> mailService.sendEmailNotification(Mockito.any()));
  }

  @Test
  public void
      sendReassignConfirmationNotification_Should_sendEmail_When_newEmailNotificationsEnabledAndConsultantsDoesWantsToReceiveNotifications() {
    var randomConsultant = new EasyRandom().nextObject(Consultant.class);
    when(consultantService.getConsultant(any())).thenReturn(Optional.of(randomConsultant));
    randomConsultant.setNotificationsSettings(
        JsonSerializationUtils.serializeToJsonString(
            new NotificationsSettingsDTO().reassignmentNotificationEnabled(true)));
    var reassignmentNotification = new EasyRandom().nextObject(ReassignmentNotificationDTO.class);
    randomConsultant.setNotificationsSettings(
        JsonSerializationUtils.serializeToJsonString(new NotificationsSettingsDTO()));
    when(releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS))
        .thenReturn(true);

    emailNotificationFacade.sendReassignConfirmationNotification(reassignmentNotification, null);

    verifyAsync(a -> mailService.sendEmailNotification(Mockito.any()));
  }

  @Test(expected = NotFoundException.class)
  public void
      sendReassignConfirmationNotification_ShouldThrow_NotFoundEception_When_consultantDoesNotExist() {
    var reassignmentNotification = new EasyRandom().nextObject(ReassignmentNotificationDTO.class);
    when(consultantService.getConsultant(any())).thenReturn(Optional.empty());

    emailNotificationFacade.sendReassignConfirmationNotification(reassignmentNotification, null);
  }
}
