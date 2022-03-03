package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.EmailNotificationTemplates.TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER;
import static de.caritas.cob.userservice.api.helper.EmailNotificationTemplates.TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_AGENCY_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_ENCODED;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.api.tenant.TenantData;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.NewMessageDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.NotificationsDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.TeamSessionsDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

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
  private ConsultantService consultantService;

  @Mock
  private UserHelper userHelper;

  @Mock
  private Logger logger;

  @Mock
  private TenantDataSupplier tenantDataSupplier;

  @Before
  public void setup() {
    this.newMessageEmailSupplier = NewMessageEmailSupplier
        .builder()
        .session(session)
        .rcGroupId("feedbackGroupId")
        .roles(roles)
        .userId(USER.getUserId())
        .consultantAgencyService(consultantAgencyService)
        .consultingTypeManager(consultingTypeManager)
        .consultantService(consultantService)
        .applicationBaseUrl("app baseurl")
        .emailDummySuffix("dummySuffix")
        .build();
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
    ExtendedConsultingTypeResponseDTO settings = mock(ExtendedConsultingTypeResponseDTO.class);
    NewMessageDTO newMessageDTO = new NewMessageDTO().allTeamConsultants(true);
    TeamSessionsDTO teamSessionsDTO = new TeamSessionsDTO().newMessage(newMessageDTO);
    NotificationsDTO notificationsDTO = new NotificationsDTO().teamSessions(teamSessionsDTO);
    when(settings.getNotifications()).thenReturn(notificationsDTO);
    when(consultingTypeManager.getConsultingTypeSettings(anyInt())).thenReturn(settings);
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
  public void generateEmails_Should_ReturnEmptyListAndLogError_When_UserRoleIsConsultantAndAskerHasNoMailAddress() {
    when(roles.contains(UserRole.CONSULTANT.getValue())).thenReturn(true);
    User user = Mockito.mock(User.class);
    when(session.getUser()).thenReturn(user);
    when(user.getEmail()).thenReturn(StringUtils.EMPTY);

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
    verify(logger, times(1)).error(anyString(), anyString(), anyString());
  }

  @Test
  public void generateEmails_Should_ReturnEmptyList_When_UserMailIsDummy() {
    when(roles.contains(UserRole.CONSULTANT.getValue())).thenReturn(true);
    User user = mock(User.class);
    when(user.getEmail()).thenReturn("email@dummySuffix");
    when(session.getUser()).thenReturn(user);

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
  }

  @Test
  public void generateEmails_Should_ReturnExpectedEmailToAsker_When_ConsultantWritesToValidReceiver() {
    when(roles.contains(UserRole.CONSULTANT.getValue())).thenReturn(true);
    Consultant consultant = mock(Consultant.class);
    when(consultant.getUsername()).thenReturn(USERNAME_ENCODED);
    when(session.getConsultant()).thenReturn(consultant);
    when(consultant.getId()).thenReturn(USER.getUserId());
    when(session.getUser()).thenReturn(USER);

    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(1));
    MailDTO generatedMail = generatedMails.get(0);
    assertThat(generatedMail.getTemplate(), is(TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER));
    assertThat(generatedMail.getEmail(), is("email@email.com"));
    List<TemplateDataDTO> templateData = generatedMail.getTemplateData();
    assertThat(templateData, hasSize(3));
    assertThat(templateData.get(0).getKey(), is("consultantName"));
    assertThat(templateData.get(0).getValue(), is("Username!#123"));
    assertThat(templateData.get(1).getKey(), is("askerName"));
    assertThat(templateData.get(1).getValue(), is("username"));
    assertThat(templateData.get(2).getKey(), is("url"));
    assertThat(templateData.get(2).getValue(), is("app baseurl"));
  }

  @Test
  public void generateEmails_Should_ReturnExpectedEmailToAsker_When_ConsultantWritesToValidReceiverMultiTenancy(){
    var tenantData = new TenantData();
    tenantData.setTenantId(1L);
    tenantData.setSubdomain("subdomain");
    TenantContext.setCurrentTenantData(tenantData);
    when(roles.contains(UserRole.CONSULTANT.getValue())).thenReturn(true);
    Consultant consultant = mock(Consultant.class);
    when(consultant.getUsername()).thenReturn(USERNAME_ENCODED);
    when(session.getConsultant()).thenReturn(consultant);
    when(consultant.getId()).thenReturn(USER.getUserId());
    when(session.getUser()).thenReturn(USER);
    var mockedTemplateAtt = new ArrayList<TemplateDataDTO>();
    mockedTemplateAtt.add(new TemplateDataDTO());
    when(tenantDataSupplier.getTemplateAttributes()).thenReturn(mockedTemplateAtt);

    ReflectionTestUtils.setField(newMessageEmailSupplier, "multiTenancyEnabled",true);
    ReflectionTestUtils.setField(newMessageEmailSupplier, "tenantDataSupplier",tenantDataSupplier);
    List<MailDTO> generatedMails = this.newMessageEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(1));
    assertThat(generatedMails.get(0).getTemplateData(), hasSize(3));
  }

  @Test(expected = InternalServerErrorException.class)
  public void generateEmails_Should_ThrowInternalServerException_When_ConsultantIsNotFound() {
    when(roles.contains(UserRole.CONSULTANT.getValue())).thenReturn(true);
    Consultant consultant = mock(Consultant.class);
    when(session.getConsultant()).thenReturn(consultant);
    when(consultant.getId()).thenReturn(CONSULTANT_ID);
    when(session.getUser()).thenReturn(USER);
    when(consultantService.getConsultant(USER.getUserId()))
        .thenReturn(Optional.empty());

    this.newMessageEmailSupplier.generateEmails();
  }


}
