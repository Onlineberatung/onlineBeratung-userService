package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER;
import static de.caritas.cob.userservice.api.helper.EmailNotificationHelper.TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_AGENCY_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.NewMessageDTO;
import de.caritas.cob.userservice.api.model.NotificationDTO;
import de.caritas.cob.userservice.api.model.TeamSessionDTO;
import de.caritas.cob.userservice.api.model.ToConsultantDTO;
import de.caritas.cob.userservice.api.model.mailservice.MailDTO;
import de.caritas.cob.userservice.api.model.mailservice.TemplateDataDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class NewMessageEmailSupplierTest {

  private NewMessageEmailSupplier newMessageEmailSupplier;

  @Mock
  private Session session;

  @Mock
  private Set<String> roles;

  @Mock
  private ConsultantAgencyService consultantAgencyService;

  @Mock
  private ConsultingTypeManager consultingTypeManager;

  @Mock
  private UserHelper userHelper;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    this.newMessageEmailSupplier = new NewMessageEmailSupplier(session, "feedbackGroupId",
        roles, USER.getUserId(), consultantAgencyService, consultingTypeManager, "app baseurl",
        "dummySuffix", userHelper);
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void generateEmails_Should_ReturnEmptyList_When_NoUserRoleIsAvailable() {
    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
  }

  @Test
  public void generateEmails_Should_ReturnEmptyListAndLogError_When_UserRoleIsUserAndSessionIsNotValidAndMessageIsNotTheFirst() {
    when(roles.contains(UserRole.USER.getValue())).thenReturn(true);
    User userAnotherId = mock(User.class);
    when(userAnotherId.getUserId()).thenReturn("another");
    when(session.getUser()).thenReturn(userAnotherId);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
    verify(logger, times(1)).error(anyString(), anyString(), anyString());
  }

  @Test
  public void generateEmails_Should_ReturnEmptyListAndNotLogError_When_UserRoleIsUserAndSessionIsNotValidAndMessageIsTheFirst() {
    when(roles.contains(UserRole.USER.getValue())).thenReturn(true);
    when(session.getUser()).thenReturn(USER);
    when(session.getStatus()).thenReturn(SessionStatus.NEW);

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
  }

  @Test
  public void generateEmails_Should_ReturnEmptyList_When_UserRoleIsUserAndSessionIsValidAndMessageAndNoDependentConsultantsExists() {
    when(roles.contains(UserRole.USER.getValue())).thenReturn(true);
    when(session.getUser()).thenReturn(USER);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(session.getConsultant()).thenReturn(new Consultant());

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
  }

  @Test
  public void generateEmails_Should_ReturnExpectedMail_When_UserRoleIsUserAndSessionIsNoTeamSession() {
    when(roles.contains(UserRole.USER.getValue())).thenReturn(true);
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(USER.getUserId());
    when(session.getUser()).thenReturn(user);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(session.getConsultant()).thenReturn(CONSULTANT);
    when(session.getPostcode()).thenReturn("1234");

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(1));
    MailDTO generatedMail = generatedMails.get(0);
    assertThat(generatedMail.getTemplate(), is(TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT));
    assertThat(generatedMail.getEmail(), is("email@email.com"));
    List<TemplateDataDTO> templateData = generatedMail.getTemplateData();
    assertThat(templateData, hasSize(3));
    assertThat(templateData.get(0).getKey(), is("name"));
    assertThat(templateData.get(0).getValue(), is("vorname nachname"));
    assertThat(templateData.get(1).getKey(), is("plz"));
    assertThat(templateData.get(1).getValue(), is("1234"));
    assertThat(templateData.get(2).getKey(), is("url"));
    assertThat(templateData.get(2).getValue(), is("app baseurl"));
  }

  @Test
  public void generateEmails_Should_ReturnExpectedMail_When_UserRoleIsUserAndSessionIsTeamSession() {
    ConsultingTypeSettings settings = mock(ConsultingTypeSettings.class);
    ToConsultantDTO toConsultantDTO = new ToConsultantDTO().allTeamConsultants(true);
    TeamSessionDTO teamSessionDTO = new TeamSessionDTO().toConsultant(toConsultantDTO);
    NewMessageDTO newMessageDTO = new NewMessageDTO().teamSession(teamSessionDTO);
    NotificationDTO notificationDTO = new NotificationDTO().newMessage(newMessageDTO);
    when(settings.getNotifications()).thenReturn(notificationDTO);
    when(consultingTypeManager.getConsultingTypeSettings(any())).thenReturn(settings);
    when(roles.contains(UserRole.USER.getValue())).thenReturn(true);
    when(session.isTeamSession()).thenReturn(true);
    User user = mock(User.class);
    when(user.getUserId()).thenReturn(USER.getUserId());
    when(session.getUser()).thenReturn(user);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    when(session.getPostcode()).thenReturn("1234");
    when(consultantAgencyService.findConsultantsByAgencyId(any()))
        .thenReturn(asList(CONSULTANT_AGENCY_2, CONSULTANT_AGENCY_2));

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(2));
    MailDTO generatedMail = generatedMails.get(0);
    assertThat(generatedMail.getTemplate(), is(TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT));
    assertThat(generatedMail.getEmail(), is("email@email.com"));
    List<TemplateDataDTO> templateData = generatedMail.getTemplateData();
    assertThat(templateData, hasSize(3));
    assertThat(templateData.get(0).getKey(), is("name"));
    assertThat(templateData.get(0).getValue(), is("vorname nachname"));
    assertThat(templateData.get(1).getKey(), is("plz"));
    assertThat(templateData.get(1).getValue(), is("1234"));
    assertThat(templateData.get(2).getKey(), is("url"));
    assertThat(templateData.get(2).getValue(), is("app baseurl"));
  }

  @Test
  public void generateEmails_Should_ReturnEmptyListAndLogError_When_UserRoleIsConsultantAndSessionIsNotValid() {
    when(roles.contains(UserRole.CONSULTANT.getValue())).thenReturn(true);
    when(session.getConsultant()).thenReturn(CONSULTANT);
    when(session.getUser()).thenReturn(USER);

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
    verify(logger, times(1)).error(anyString(), anyString(), anyString());
  }

  @Test
  public void generateEmails_Should_ReturnEmptyList_When_UserMailIsDummy() {
    when(roles.contains(UserRole.CONSULTANT.getValue())).thenReturn(true);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    Consultant consultant = mock(Consultant.class);
    when(consultant.getId()).thenReturn(USER.getUserId());
    when(session.getConsultant()).thenReturn(consultant);
    User user = mock(User.class);
    when(user.getEmail()).thenReturn("email@dummySuffix");
    when(session.getUser()).thenReturn(user);

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
  }

  @Test
  public void generateEmails_Should_ReturnExpectedEmailToAsker_When_ConsultantWritesToValidReceiver() {
    when(roles.contains(UserRole.CONSULTANT.getValue())).thenReturn(true);
    when(session.getStatus()).thenReturn(SessionStatus.IN_PROGRESS);
    Consultant consultant = mock(Consultant.class);
    when(consultant.getId()).thenReturn(USER.getUserId());
    when(session.getConsultant()).thenReturn(consultant);
    when(session.getUser()).thenReturn(USER);
    when(userHelper.decodeUsername(any())).thenReturn("decoded user name");

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(1));
    MailDTO generatedMail = generatedMails.get(0);
    assertThat(generatedMail.getTemplate(), is(TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER));
    assertThat(generatedMail.getEmail(), is("email@email.com"));
    List<TemplateDataDTO> templateData = generatedMail.getTemplateData();
    assertThat(templateData, hasSize(3));
    assertThat(templateData.get(0).getKey(), is("consultantName"));
    assertThat(templateData.get(0).getValue(), is("decoded user name"));
    assertThat(templateData.get(1).getKey(), is("askerName"));
    assertThat(templateData.get(1).getValue(), is("decoded user name"));
    assertThat(templateData.get(2).getKey(), is("url"));
    assertThat(templateData.get(2).getValue(), is("app baseurl"));
  }

}
