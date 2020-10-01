package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_VALUE_EMAIL_DUMMY_SUFFIX;
import static de.caritas.cob.userservice.testHelper.FieldConstants.FIELD_VALUE_ROCKET_CHAT_SYSTEM_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.APPLICATION_BASE_URL;
import static de.caritas.cob.userservice.testHelper.TestConstants.APPLICATION_BASE_URL_FIELD_NAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_NO_TEAM_SESSION;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_TEAM_SESSION;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_CONSULTANT_DECODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_CONSULTANT_ENCODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_ENCODED;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.EmailNotificationException;
import de.caritas.cob.userservice.api.exception.NewMessageNotificationException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.EmailNotificationHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.manager.consultingType.notifications.NewMessage;
import de.caritas.cob.userservice.api.manager.consultingType.notifications.Notifications;
import de.caritas.cob.userservice.api.manager.consultingType.notifications.TeamSession;
import de.caritas.cob.userservice.api.manager.consultingType.notifications.ToConsultant;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.model.mailService.MailDtoBuilder;
import de.caritas.cob.userservice.api.model.mailService.MailsDTO;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;
import de.caritas.cob.userservice.api.service.helper.MailServiceHelper;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
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
          "consultant@domain.de", false, false, null, false, 1L, null, null);
  private final Consultant CONSULTANT_WITHOUT_MAIL = new Consultant(CONSULTANT_ID, "XXX",
      "consultant", "consultant", "consultant", "", false, false, null, false, 1L, null, null);
  private final Consultant CONSULTANT2 =
      new Consultant(CONSULTANT_ID_2, "XXX", "consultant2", "consultant2", "consultant2",
          "consultant2@domain.de", false, false, null, false, 1L, null, null);
  private final Consultant CONSULTANT3 =
      new Consultant(CONSULTANT_ID_3, "XXX", "consultant3", "consultant3", "consultant3",
          "consultant3@domain.de", false, false, null, false, 1L, null, null);
  private final Consultant CONSULTANT_NO_EMAIL = new Consultant(CONSULTANT_ID, "XXX", "consultant",
      "consultant", "consultant", "", false, false, null, false, 1L, null, null);
  private final Consultant ABSENT_CONSULTANT = new Consultant("XXX", "XXX", "consultant",
      "consultant", "consultant", "consultant@domain.de", true, false, null, false, 1L, null, null);
  private final User USER = new User(USER_ID, USERNAME_ENCODED, "email@email.de", null);
  private final User USER_NO_EMAIL = new User(USER_ID, "username", "", null);
  private final ConsultantAgency CONSULTANT_AGENCY =
      new ConsultantAgency(1L, CONSULTANT, AGENCY_ID);
  private final ConsultantAgency CONSULTANT_AGENCY_2 =
      new ConsultantAgency(1L, CONSULTANT2, AGENCY_ID);
  private final ConsultantAgency ABSENT_CONSULTANT_AGENCY =
      new ConsultantAgency(1L, ABSENT_CONSULTANT, AGENCY_ID);
  private final Session SESSION =
      new Session(1L, USER, CONSULTANT, ConsultingType.SUCHT, "88045", AGENCY_ID, SessionStatus.INITIAL,
          new Date(), RC_GROUP_ID, null, IS_NO_TEAM_SESSION, IS_MONITORING);
  private final Session SESSION_WITHOUT_CONSULTANT =
      new Session(1L, USER, null, ConsultingType.SUCHT, "88045", AGENCY_ID, SessionStatus.NEW,
          new Date(), RC_GROUP_ID, null, IS_NO_TEAM_SESSION, IS_MONITORING);
  private final Session SESSION_IN_PROGRESS = new Session(1L, USER, CONSULTANT,
      ConsultingType.SUCHT, "88045", AGENCY_ID, SessionStatus.IN_PROGRESS, new Date(), RC_GROUP_ID,
      null, IS_NO_TEAM_SESSION, IS_MONITORING);
  private final Session SESSION_IN_PROGRESS_NO_EMAIL = new Session(1L, USER_NO_EMAIL,
      CONSULTANT_NO_EMAIL, ConsultingType.SUCHT, "88045", AGENCY_ID, SessionStatus.IN_PROGRESS,
      new Date(), RC_GROUP_ID, null, IS_NO_TEAM_SESSION, IS_MONITORING);
  private final Session TEAM_SESSION =
      new Session(1L, USER, CONSULTANT, ConsultingType.SUCHT, "12345", AGENCY_ID,
          SessionStatus.IN_PROGRESS, new Date(), RC_GROUP_ID, null, IS_TEAM_SESSION, IS_MONITORING);
  private final AgencyDTO AGENCY_DTO = new AgencyDTO(1L, "Test Beratungsstelle", "99999",
      "testcity",
      "Beratungsstellenbeschreibung", false, false, ConsultingType.SUCHT);
  private final String USER_ROLE = UserRole.USER.getValue();
  private final Set<String> USER_ROLES = new HashSet<>(Arrays.asList(USER_ROLE));
  private final String CONSULTANT_ROLE = UserRole.CONSULTANT.getValue();
  private final Set<String> CONSULTANT_ROLES = new HashSet<>(Arrays.asList(CONSULTANT_ROLE));
  private final String ERROR_MSG = "error";
  private final List<ConsultantAgency> CONSULTANT_LIST =
      Arrays.asList(CONSULTANT_AGENCY, CONSULTANT_AGENCY_2);
  private final List<ConsultantAgency> CONSULTANT_AGENCY_LIST = Arrays.asList(CONSULTANT_AGENCY);
  private final List<ConsultantAgency> ABSENT_CONSULTANT_AGENCY_LIST =
      Arrays.asList(ABSENT_CONSULTANT_AGENCY);
  private final Set<String> U25_PEER_ROLES_DEFAULT =
      new HashSet<>(Arrays.asList(UserRole.U25_CONSULTANT.getValue()));
  private final String GROUP_MEMBER_1_RC_ID = "yzx324sdg";
  private final GroupMemberDTO GROUP_MEMBER_1 =
      new GroupMemberDTO(GROUP_MEMBER_1_RC_ID, "status1", "username1", "name1", "");
  private final String GROUP_MEMBER_2_RC_ID = "sdf33dfdsf";
  private final GroupMemberDTO GROUP_MEMBER_2 =
      new GroupMemberDTO(GROUP_MEMBER_2_RC_ID, "status2", "username2", "name2", "");
  private final List<GroupMemberDTO> GROUP_MEMBERS = Arrays.asList(GROUP_MEMBER_1, GROUP_MEMBER_2);
  private final Notifications NOTIFICATIONS_TO_ALL_TEAM_CONSULTANTS =
      new Notifications(new NewMessage(new TeamSession(new ToConsultant(true))));
  private final Notifications NOTIFICATIONS_TO_ASSIGNED_CONSULTANT_ONLY =
      new Notifications(new NewMessage(new TeamSession(new ToConsultant(false))));
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null, null, true, null, false,
          NOTIFICATIONS_TO_ALL_TEAM_CONSULTANTS, false, null, null);
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ASSIGNED_CONSULTANT_ONLY =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null, null, true, null, false,
          NOTIFICATIONS_TO_ASSIGNED_CONSULTANT_ONLY, false, null, null);

  @InjectMocks
  private EmailNotificationFacade emailNotificationFacade;
  @Mock
  private ConsultantAgencyRepository consultantAgencyRepository;
  @Mock
  private MailServiceHelper mailServiceHelper;
  @Mock
  private AgencyServiceHelper agencyServiceHelper;
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
  MailDtoBuilder mailDtoBuilder;
  @Mock
  ConsultingTypeManager consultingTypeManager;
  @Mock
  UserHelper userHelper;

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
  @SuppressWarnings("unchecked")
  @Test
  public void sendNewEnquiryEmailNotification_Should_SendEmailNotificationViaMailServiceHelperToConsultants()
      throws AgencyServiceHelperException {

    when(consultantAgencyRepository.findByAgencyId(Mockito.eq(SESSION.getAgencyId())))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(agencyServiceHelper.getAgency(SESSION.getAgencyId())).thenReturn(AGENCY_DTO);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(mailDtoBuilder, times(1)).build(
        Mockito.eq(EmailNotificationHelper.TEMPLATE_NEW_ENQUIRY_NOTIFICATION),
        Mockito.eq(CONSULTANT.getEmail()),
        Mockito.eq(new SimpleImmutableEntry<String, String>("name", CONSULTANT.getFullName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("plz", SESSION.getPostcode())),
        Mockito
            .eq(new SimpleImmutableEntry<String, String>("beratungsstelle", AGENCY_DTO.getName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL)));

    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));

  }

  @Test
  public void sendNewEnquiryEmailNotification_ShouldNot_SendEmailNotificationViaMailServiceHelperToUser()
      throws AgencyServiceHelperException {

    when(consultantAgencyRepository.findByAgencyId(Mockito.eq(SESSION.getAgencyId())))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(agencyServiceHelper.getAgency(SESSION.getAgencyId())).thenReturn(AGENCY_DTO);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(mailDtoBuilder, times(0)).build(Mockito.any(), Mockito.eq(SESSION.getUser().getEmail()),
        ArgumentMatchers.any());

    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewEnquiryEmailNotification_Should_GetAgencyInformationFromAgencyServiceHelper()
      throws AgencyServiceHelperException {

    when(consultantAgencyRepository.findByAgencyId(Mockito.eq(SESSION.getAgencyId())))
        .thenReturn(CONSULTANT_AGENCY_LIST);
    when(agencyServiceHelper.getAgency(SESSION.getAgencyId())).thenReturn(AGENCY_DTO);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(agencyServiceHelper, times(1)).getAgency(SESSION.getAgencyId());

  }

  @Test
  public void sendNewEnquiryEmailNotification_ShouldNot_SendEmailWhenConsultantAgencyListIsEmpty() {

    when(consultantAgencyRepository.findByAgencyId(Mockito.eq(AGENCY_ID))).thenReturn(null);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(mailServiceHelper, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verifyNoMoreInteractions(mailDtoBuilder);

  }

  @Test
  public void sendNewEnquiryEmailNotification_ShouldNot_SendEmailWhenConsultantIsAbsent() {

    when(consultantAgencyRepository.findByAgencyId(Mockito.eq(AGENCY_ID)))
        .thenReturn(ABSENT_CONSULTANT_AGENCY_LIST);

    emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);

    verify(mailServiceHelper, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verifyNoMoreInteractions(mailDtoBuilder);

  }

  @Test
  public void sendNewEnquiryEmailNotification_Should_ThrowEmailNotificationException_WhenSendEmailFails() {

    EmailNotificationException emailNotificationException =
        new EmailNotificationException(new Exception());

    when(consultantAgencyRepository.findByAgencyId(Mockito.eq(AGENCY_ID)))
        .thenThrow(emailNotificationException);

    try {
      emailNotificationFacade.sendNewEnquiryEmailNotification(SESSION);
      fail("Expected exception: EmailNotificationException");
    } catch (EmailNotificationException mailNotificationException) {
      assertTrue("Excepted EmailNotificationException thrown", true);
    }
  }

  /**
   * Method: sendNewMessageNotification
   */

  @SuppressWarnings("unchecked")
  @Test
  public void sendNewMessageNotification_Should_SendEmailNotificationViaMailServiceHelperToConsultant_WhenCalledAsUserAuthorityAndIsTeamSession() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(CONSULTANT_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(TEAM_SESSION.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailDtoBuilder, times(1)).build(
        Mockito.eq(EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT),
        Mockito.eq(CONSULTANT.getEmail()),
        Mockito.eq(new SimpleImmutableEntry<String, String>("name", CONSULTANT.getFullName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("plz", TEAM_SESSION.getPostcode())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL)));

    verify(consultantAgencyService, times(1)).findConsultantsByAgencyId(AGENCY_ID);
    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmailNotificationToUser_WhenCalledAsUserAuthorityAndIsTeamSession() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(CONSULTANT_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(TEAM_SESSION.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailDtoBuilder, times(0)).build(Mockito.any(),
        Mockito.eq(TEAM_SESSION.getUser().getEmail()), ArgumentMatchers.any());

    verify(consultantAgencyService, times(1)).findConsultantsByAgencyId(AGENCY_ID);
    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmail_WhenMailListIsEmptyAndCalledAsUserAuthorityAndIsTeamSession() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(null);
    when(consultingTypeManager.getConsultantTypeSettings(TEAM_SESSION.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailServiceHelper, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verifyNoMoreInteractions(mailDtoBuilder);
  }

  @Test
  public void sendNewMessageNotification_Should_ThrowNewMessageNotificationException_WhenSessionServiceFails() {

    InternalServerErrorException serviceException = new InternalServerErrorException(ERROR_MSG);

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenThrow(serviceException);

    try {
      emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);
      fail("Expected exception: NewMessageNotificationException");
    } catch (NewMessageNotificationException newMessageNotificationException) {
      assertTrue("Excepted InternalServerErrorException thrown", true);
    }
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmailAndLogEmailNotificationFacadeError_WhenSessionIsNullOrEmpty() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(null);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailServiceHelper, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verifyNoMoreInteractions(mailDtoBuilder);
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmailAndLogEmailNotificationFacadeError_WhenSessionIsNotInProgress() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(SESSION);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailServiceHelper, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verifyNoMoreInteractions(mailDtoBuilder);
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmail_WhenCalledAsUserAuthorityAndIsSingleSessionAndConsultantHasNoEmailProvided() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(SESSION_IN_PROGRESS_NO_EMAIL);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailServiceHelper, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verifyNoMoreInteractions(mailDtoBuilder);
  }

  @Test
  public void sendNewMessageNotification_Should_SendEmailNotificationViaMailServiceHelper_WhenCalledAsUserAuthorityAndIsSingleSession() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(SESSION_IN_PROGRESS);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmailAndLogEmailNotificationFacadeError_WhenSessionIsNullOrEmpty_WithConsultantRole() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenReturn(null);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, CONSULTANT_ROLES,
        CONSULTANT_ID);

    verify(mailServiceHelper, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verifyNoMoreInteractions(mailDtoBuilder);
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_SendEmail_WhenCalledAsConsultantAuthorityAndAskerHasNoEmailProvided() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenReturn(SESSION_IN_PROGRESS_NO_EMAIL);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, CONSULTANT_ROLES,
        CONSULTANT_ID);

    verify(mailServiceHelper, times(0)).sendEmailNotification(Mockito.any(MailsDTO.class));
    verifyNoMoreInteractions(mailDtoBuilder);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void sendNewMessageNotification_Should_SendEmailToUserWithEncodedUsernames_WhenCalledAsConsultantAuthorityAndAskerHasEmail() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, CONSULTANT_ID, CONSULTANT_ROLES))
        .thenReturn(SESSION_IN_PROGRESS);
    when(userHelper.decodeUsername(USERNAME_ENCODED)).thenReturn(USERNAME_DECODED);
    when(userHelper.decodeUsername(USERNAME_CONSULTANT_ENCODED))
        .thenReturn(USERNAME_CONSULTANT_DECODED);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, CONSULTANT_ROLES,
        CONSULTANT_ID);

    verify(mailDtoBuilder, times(1)).build(
        Mockito.eq(EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER),
        Mockito.eq(SESSION_IN_PROGRESS.getUser().getEmail()),
        Mockito.eq(new SimpleImmutableEntry<String, String>("consultantName",
            USERNAME_CONSULTANT_DECODED)),
        Mockito.eq(new SimpleImmutableEntry<String, String>("askerName", USERNAME_DECODED)),
        Mockito.eq(new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL)));

    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any(MailsDTO.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void sendNewMessageNotification_Should_SendEmailToAllConsultants_WhenIsTeamSessionAndConsultingTypeSettingsToSendToAllTeamConsultantsIsTrue() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultingTypeManager.getConsultantTypeSettings(TEAM_SESSION.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ALL_TEAM_CONSULTANTS);
    when(consultantAgencyService.findConsultantsByAgencyId(AGENCY_ID)).thenReturn(CONSULTANT_LIST);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailDtoBuilder, times(1)).build(
        Mockito.eq(EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT),
        Mockito.eq(CONSULTANT.getEmail()),
        Mockito.eq(new SimpleImmutableEntry<String, String>("name", CONSULTANT.getFullName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("plz", TEAM_SESSION.getPostcode())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL)));

    verify(mailDtoBuilder, times(1)).build(
        Mockito.eq(EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT),
        Mockito.eq(CONSULTANT2.getEmail()),
        Mockito.eq(new SimpleImmutableEntry<String, String>("name", CONSULTANT2.getFullName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("plz", TEAM_SESSION.getPostcode())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL)));

    verify(mailDtoBuilder, times(2)).build(Mockito.any(), Mockito.any(), ArgumentMatchers.any());

    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void sendNewMessageNotification_Should_SendEmailToAssignConsultantOnly_WhenIsTeamSessionAndConsultingTypeSettingsToSendToAllTeamConsultantsIsFalse() {

    when(sessionService.getSessionByGroupIdAndUserId(RC_GROUP_ID, USER_ID, USER_ROLES))
        .thenReturn(TEAM_SESSION);
    when(consultingTypeManager.getConsultantTypeSettings(TEAM_SESSION.getConsultingType()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_NOTIFICATION_TO_ASSIGNED_CONSULTANT_ONLY);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verify(mailDtoBuilder, times(1)).build(
        Mockito.eq(EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT),
        Mockito.eq(CONSULTANT.getEmail()),
        Mockito.eq(new SimpleImmutableEntry<String, String>("name", CONSULTANT.getFullName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("plz", TEAM_SESSION.getPostcode())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL)));

    verify(mailDtoBuilder, times(1)).build(Mockito.any(), Mockito.any(), ArgumentMatchers.any());

    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any());
  }

  /**
   * Method: sendNewFeedbackMessageNotification
   */

  @SuppressWarnings("unchecked")
  @Test
  public void sendNewFeedbackMessageNotification_Should_SendEmailToAllFeedbackChatGroupMembersWithDecodedUsernames_WhenAssigendConsultantWroteAFeedbackMessage()
      throws Exception {

    when(consultantService.getConsultant(CONSULTANT_ID)).thenReturn(Optional.of(CONSULTANT));
    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID)).thenReturn(SESSION);
    when(rocketChatService.getMembersOfGroup(RC_FEEDBACK_GROUP_ID)).thenReturn(GROUP_MEMBERS);
    when(consultantService.getConsultantByRcUserId(GROUP_MEMBER_1_RC_ID))
        .thenReturn(Optional.of(CONSULTANT2));
    when(consultantService.getConsultantByRcUserId(GROUP_MEMBER_2_RC_ID))
        .thenReturn(Optional.of(CONSULTANT3));
    when(userHelper.decodeUsername(USERNAME_ENCODED)).thenReturn(USERNAME_DECODED);

    emailNotificationFacade.sendNewFeedbackMessageNotification(RC_FEEDBACK_GROUP_ID, CONSULTANT_ID);

    verify(mailDtoBuilder, times(1)).build(
        Mockito.eq(EmailNotificationHelper.TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION),
        Mockito.eq(CONSULTANT2.getEmail()),
        Mockito
            .eq(new SimpleImmutableEntry<String, String>("name_sender", CONSULTANT.getFullName())),
        Mockito.eq(
            new SimpleImmutableEntry<String, String>("name_recipient", CONSULTANT2.getFullName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("name_user", USERNAME_DECODED)),
        Mockito.eq(new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL)));

    verify(mailDtoBuilder, times(1)).build(
        Mockito.eq(EmailNotificationHelper.TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION),
        Mockito.eq(CONSULTANT3.getEmail()),
        Mockito
            .eq(new SimpleImmutableEntry<String, String>("name_sender", CONSULTANT.getFullName())),
        Mockito.eq(
            new SimpleImmutableEntry<String, String>("name_recipient", CONSULTANT3.getFullName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("name_user", USERNAME_DECODED)),
        Mockito.eq(new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL)));

    verify(mailDtoBuilder, times(2)).build(Mockito.any(), Mockito.any(), ArgumentMatchers.any());

    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any());

  }

  @SuppressWarnings("unchecked")
  @Test
  public void sendNewFeedbackMessageNotification_Should_SendEmailToAssignedConsultantWithDecodedUsername_WhenOtherConsultantWrote() {

    when(consultantService.getConsultant(CONSULTANT_ID_2)).thenReturn(Optional.of(CONSULTANT2));
    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID)).thenReturn(SESSION);
    when(userHelper.decodeUsername(USERNAME_ENCODED)).thenReturn(USERNAME_DECODED);

    emailNotificationFacade.sendNewFeedbackMessageNotification(RC_FEEDBACK_GROUP_ID,
        CONSULTANT_ID_2);

    // Verify that mail for assigned consultant is prepared...
    verify(mailDtoBuilder, times(1)).build(
        Mockito.eq(EmailNotificationHelper.TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION),
        Mockito.eq(CONSULTANT.getEmail()),
        Mockito
            .eq(new SimpleImmutableEntry<String, String>("name_sender", CONSULTANT2.getFullName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("name_recipient",
            SESSION.getConsultant().getFullName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("name_user", USERNAME_DECODED)),
        Mockito.eq(new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL)));

    // and verify that only 1 mail is prepared
    verify(mailDtoBuilder, times(1)).build(Mockito.any(), Mockito.any(), ArgumentMatchers.any());

    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any());

  }

  @Test
  public void sendNewFeedbackMessageNotification_Should_LogErrorAndSendNoMails_WhenCallingConsultantIsNotFound() {

    emailNotificationFacade.sendNewFeedbackMessageNotification(RC_FEEDBACK_GROUP_ID,
        CONSULTANT_ID);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verifyNoMoreInteractions(mailServiceHelper, mailDtoBuilder);

  }

  @Test
  public void sendNewFeedbackMessageNotification_Should_LogErrorAndSendNoMails_WhenSessionIsNotFound() {

    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID)).thenReturn(null);

    emailNotificationFacade.sendNewFeedbackMessageNotification(RC_FEEDBACK_GROUP_ID,
        CONSULTANT_ID);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verifyNoMoreInteractions(mailServiceHelper, mailDtoBuilder);

  }

  @Test
  public void sendNewFeedbackMessageNotification_Should_LogErrorAndSendNoMails_WhenNoConsultantIsAssignedToSession() {

    when(sessionService.getSessionByFeedbackGroupId(RC_FEEDBACK_GROUP_ID))
        .thenReturn(SESSION_WITHOUT_CONSULTANT);

    emailNotificationFacade.sendNewFeedbackMessageNotification(RC_FEEDBACK_GROUP_ID,
        CONSULTANT_ID);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verifyNoMoreInteractions(mailServiceHelper, mailDtoBuilder);

  }

  @SuppressWarnings("unchecked")
  @Test
  public void sendAssignEnquiryEmailNotification_Should_SendEmail_WhenAllParametersAreValid() {

    when(consultantService.getConsultant(CONSULTANT_ID_2)).thenReturn(Optional.of(CONSULTANT2));
    when(userHelper.decodeUsername(Mockito.any())).thenReturn(USERNAME);

    emailNotificationFacade.sendAssignEnquiryEmailNotification(CONSULTANT, CONSULTANT_ID_2,
        USERNAME);

    // Verify that mail for assigned consultant is prepared...
    verify(mailDtoBuilder, times(1)).build(
        Mockito.eq(EmailNotificationHelper.TEMPLATE_ASSIGN_ENQUIRY_NOTIFICATION),
        Mockito.eq(CONSULTANT.getEmail()),
        Mockito
            .eq(new SimpleImmutableEntry<String, String>("name_sender", CONSULTANT2.getFullName())),
        Mockito.eq(
            new SimpleImmutableEntry<String, String>("name_recipient", CONSULTANT.getFullName())),
        Mockito.eq(new SimpleImmutableEntry<String, String>("name_user", USERNAME)),
        Mockito.eq(new SimpleImmutableEntry<String, String>("url", APPLICATION_BASE_URL)));

    // and verify that only 1 mail is prepared
    verify(mailDtoBuilder, times(1)).build(Mockito.any(), Mockito.any(), ArgumentMatchers.any());

    verify(mailServiceHelper, times(1)).sendEmailNotification(Mockito.any());
  }

  @Test
  public void sendAssignEnquiryEmailNotification_Should_LogErrorAndSendNoMails_WhenReceiverConsultantIsNull() {

    emailNotificationFacade.sendAssignEnquiryEmailNotification(null, CONSULTANT_ID_2, USERNAME);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verifyNoMoreInteractions(mailServiceHelper, mailDtoBuilder);
  }

  @Test
  public void sendAssignEnquiryEmailNotification_Should_LogErrorAndSendNoMails_WhenReceiverConsultantIsMissingEmailAddress() {

    emailNotificationFacade.sendAssignEnquiryEmailNotification(CONSULTANT_WITHOUT_MAIL,
        CONSULTANT_ID_2, USERNAME);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verifyNoMoreInteractions(mailServiceHelper, mailDtoBuilder);
  }

  @Test
  public void sendAssignEnquiryEmailNotification_Should_LogErrorAndSendNoMails_WhenSenderConsultantIsNotFound() {

    when(consultantService.getConsultant(Mockito.anyString())).thenReturn(Optional.empty());

    emailNotificationFacade.sendAssignEnquiryEmailNotification(CONSULTANT, CONSULTANT_ID_2,
        USERNAME);

    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
    verifyNoMoreInteractions(mailServiceHelper, mailDtoBuilder);
  }

  @Test
  public void sendNewMessageNotification_ShouldNot_LogError_When_SessionStatusIsNew() {
    Session session = mock(Session.class);
    when(session.getStatus()).thenReturn(SessionStatus.NEW);
    when(session.getUser()).thenReturn(USER);

    when(sessionService.getSessionByGroupIdAndUserId(any(), any(), any())).thenReturn(session);

    emailNotificationFacade.sendNewMessageNotification(RC_GROUP_ID, USER_ROLES, USER_ID);

    verifyZeroInteractions(logger);
  }

}
