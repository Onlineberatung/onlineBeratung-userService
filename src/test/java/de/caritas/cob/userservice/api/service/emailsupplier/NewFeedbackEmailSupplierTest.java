package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.EmailNotificationHelper.TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.GROUP_MEMBER_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME_DECODED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.mailservice.MailDTO;
import de.caritas.cob.userservice.api.model.mailservice.TemplateDataDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class NewFeedbackEmailSupplierTest {

  private NewFeedbackEmailSupplier newFeedbackEmailSupplier;

  @Mock
  private Session session;

  @Mock
  private UserHelper userHelper;

  @Mock
  private ConsultantService consultantService;

  @Mock
  private RocketChatService rocketChatService;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    this.newFeedbackEmailSupplier = new NewFeedbackEmailSupplier(session, "feedbackGroupId",
        "userId", "applicationBaseUrl", userHelper, consultantService, rocketChatService,
        "rocketChatSystemUserId");
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void generateEmails_Should_ReturnEmptyListAndLogError_When_SessionIsNull()
      throws RocketChatGetGroupMembersException {
    List<MailDTO> generatedMails = new NewFeedbackEmailSupplier(null, "feedbackGroupId",
        "userId", "applicationBaseUrl", userHelper, consultantService, rocketChatService,
        "rocketChatSystemUserId").generateEmails();

    assertThat(generatedMails, hasSize(0));
    verify(logger, times(1)).error(anyString(), anyString(), anyString());
  }

  @Test
  public void generateEmails_Should_ReturnEmptyLisAndLogError_When_NoValidConsultantWasFound()
      throws RocketChatGetGroupMembersException {

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
    verify(logger, times(1)).error(anyString(), anyString(), anyString());
  }

  @Test
  public void generateEmails_Should_ReturnEmptyLisAndLogError_When_SessionHasConsultantAndWriterIsNotAConsultant()
      throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT);

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
    verify(logger, times(1)).error(anyString(), anyString(), anyString());
  }

  @Test
  public void generateEmails_Should_ReturnEmptyListAndLogError_When_SessionHasConsultantIsWriterAndNoMembersAreInFeedbackGroup()
      throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
    verify(logger, times(1)).error(anyString(), anyString(), anyString());
  }

  @Test
  public void generateEmails_Should_ReturnEmptyListAndLogError_When_SessionHasConsultantAndNoFeedbackGroupMemberExists()
      throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));
    when(rocketChatService.getMembersOfGroup(anyString())).thenReturn(GROUP_MEMBER_DTO_LIST);

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void generateEmails_Should_ReturnExpectedMails_When_SessionHasConsultantIsWriterAndMembersAreInFeedbackGroup()
      throws RocketChatGetGroupMembersException {
    when(session.getUser()).thenReturn(USER);
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));
    when(rocketChatService.getMembersOfGroup(anyString())).thenReturn(GROUP_MEMBER_DTO_LIST);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(CONSULTANT_2));
    when(userHelper.decodeUsername(anyString())).thenReturn(USERNAME_DECODED);

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(4));
    MailDTO generatedMail = generatedMails.get(0);
    assertThat(generatedMail.getTemplate(), is(TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION));
    assertThat(generatedMail.getEmail(), is("email@email.com"));
    List<TemplateDataDTO> templateData = generatedMail.getTemplateData();
    assertThat(templateData, hasSize(4));
    assertThat(templateData.get(0).getKey(), is("name_sender"));
    assertThat(templateData.get(0).getValue(), is("vorname nachname"));
    assertThat(templateData.get(1).getKey(), is("name_recipient"));
    assertThat(templateData.get(1).getValue(), is("first name last name"));
    assertThat(templateData.get(2).getKey(), is("name_user"));
    assertThat(templateData.get(2).getValue(), is("Username!#123"));
    assertThat(templateData.get(3).getKey(), is("url"));
    assertThat(templateData.get(3).getValue(), is("applicationBaseUrl"));
  }

  @Test
  public void generateEmails_Should_ReturnEmptyList_When_SessionConsultantIsAbsent()
      throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", "other id");
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
  }

  @Test
  public void generateEmails_Should_ReturnExpectedMail_When_AnotherConsultantWrote()
      throws RocketChatGetGroupMembersException {
    when(session.getUser()).thenReturn(USER);
    when(session.getConsultant()).thenReturn(CONSULTANT_2);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT_2));
    when(userHelper.decodeUsername(anyString())).thenReturn(USERNAME_DECODED);

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(1));
    MailDTO generatedMail = generatedMails.get(0);
    assertThat(generatedMail.getTemplate(), is(TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION));
    assertThat(generatedMail.getEmail(), is("email@email.com"));
    List<TemplateDataDTO> templateData = generatedMail.getTemplateData();
    assertThat(templateData, hasSize(4));
    assertThat(templateData.get(0).getKey(), is("name_sender"));
    assertThat(templateData.get(0).getValue(), is("first name last name"));
    assertThat(templateData.get(1).getKey(), is("name_recipient"));
    assertThat(templateData.get(1).getValue(), is("first name last name"));
    assertThat(templateData.get(2).getKey(), is("name_user"));
    assertThat(templateData.get(2).getValue(), is("Username!#123"));
    assertThat(templateData.get(3).getKey(), is("url"));
    assertThat(templateData.get(3).getValue(), is("applicationBaseUrl"));
  }

}
