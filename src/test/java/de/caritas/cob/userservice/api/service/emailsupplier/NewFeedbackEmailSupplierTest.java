package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.service.emailsupplier.EmailSupplier.TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_2;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GROUP_MEMBER_DTO_LIST;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GROUP_MEMBER_USER_1;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class NewFeedbackEmailSupplierTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private NewFeedbackEmailSupplier newFeedbackEmailSupplier;

  @Mock private Session session;

  @Mock private ConsultantService consultantService;

  @Mock private RocketChatService rocketChatService;

  @Mock private KeycloakService keycloakService;

  @Mock private Logger logger;

  @Before
  public void setup() {
    this.newFeedbackEmailSupplier =
        new NewFeedbackEmailSupplier(
            session,
            "feedbackGroupId",
            "userId",
            "applicationBaseUrl",
            consultantService,
            rocketChatService,
            "rocketChatSystemUserId",
            keycloakService);
    setInternalState(NewFeedbackEmailSupplier.class, "log", logger);
  }

  @Test
  public void generateEmails_Should_ReturnEmptyListAndLogError_When_SessionIsNull()
      throws RocketChatGetGroupMembersException {
    List<MailDTO> generatedMails =
        new NewFeedbackEmailSupplier(
                null,
                "feedbackGroupId",
                "userId",
                "applicationBaseUrl",
                consultantService,
                rocketChatService,
                "rocketChatSystemUserId",
                keycloakService)
            .generateEmails();

    assertThat(generatedMails).isEmpty();
    verify(logger).error(anyString(), anyString());
  }

  @Test
  public void generateEmails_Should_ReturnEmptyLisAndLogError_When_NoValidConsultantWasFound()
      throws RocketChatGetGroupMembersException {

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).isEmpty();
    verify(logger).error(anyString(), anyString());
  }

  @Test
  public void
      generateEmails_Should_ReturnEmptyLisAndLogError_When_SessionHasConsultantAndWriterIsNotAConsultant()
          throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT);

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).isEmpty();
    verify(logger).error(anyString(), anyString());
  }

  @Test
  public void
      generateEmails_Should_ReturnEmptyListAndLogError_When_SessionHasConsultantIsWriterAndNoMembersAreInFeedbackGroup()
          throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).isEmpty();
    verify(logger).error(anyString(), anyString());
  }

  @Test
  public void
      generateEmails_Should_ReturnEmptyListAndLogError_When_SessionHasConsultantAndNoFeedbackGroupMemberExists()
          throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));
    when(rocketChatService.getChatUsers(anyString())).thenReturn(GROUP_MEMBER_DTO_LIST);

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).isEmpty();
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void
      generateEmails_Should_ReturnExpectedMails_When_SessionHasConsultantIsWriterAndMembersAreInFeedbackGroup()
          throws RocketChatGetGroupMembersException {
    when(session.getUser()).thenReturn(USER);
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));
    when(rocketChatService.getChatUsers(anyString())).thenReturn(GROUP_MEMBER_DTO_LIST);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(CONSULTANT_2));

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).hasSize(4);
    MailDTO generatedMail = generatedMails.get(0);
    assertThat(generatedMail.getTemplate()).isEqualTo(TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION);
    assertThat(generatedMail.getEmail()).isEqualTo("email@email.com");
    assertThat(generatedMail.getLanguage())
        .isEqualTo(de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode.DE);
    List<TemplateDataDTO> templateData = generatedMail.getTemplateData();
    assertThat(templateData).hasSize(4);
    assertThat(templateData.get(0).getKey()).isEqualTo("name_sender");
    assertThat(templateData.get(0).getValue()).isEqualTo(("vorname nachname"));
    assertThat(templateData.get(1).getKey()).isEqualTo("name_recipient");
    assertThat(templateData.get(1).getValue()).isEqualTo("first name last name");
    assertThat(templateData.get(2).getKey()).isEqualTo("name_user");
    assertThat(templateData.get(2).getValue()).isEqualTo(("username"));
    assertThat(templateData.get(3).getKey()).isEqualTo("url");
    assertThat(templateData.get(3).getValue()).isEqualTo("applicationBaseUrl");
  }

  @Test
  public void generateEmails_Should_ReturnExpectedMail_When_ConsultantsFeedbackToggleIsOn()
      throws RocketChatGetGroupMembersException {
    when(session.getUser()).thenReturn(USER);
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));
    when(rocketChatService.getChatUsers(anyString())).thenReturn(List.of(GROUP_MEMBER_USER_1));
    var consultant = mock(Consultant.class);
    when(consultant.getId()).thenReturn(UUID.randomUUID().toString());
    when(consultant.getEmail()).thenReturn("a@b.com");
    when(consultant.getLanguageCode()).thenReturn(LanguageCode.de);
    when(consultant.isAbsent()).thenReturn(false);
    when(consultant.getRocketChatId()).thenReturn(RandomStringUtils.randomAlphanumeric(17));
    when(consultant.getNotifyNewFeedbackMessageFromAdviceSeeker()).thenReturn(true);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(consultant));
    when(keycloakService.userHasRole(anyString(), anyString())).thenReturn(true);

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).hasSize(1);
  }

  @Test
  public void generateEmails_Should_ReturnNoMail_When_ConsultantsFeedbackToggleIsOff()
      throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));
    when(rocketChatService.getChatUsers(anyString())).thenReturn(List.of(GROUP_MEMBER_USER_1));
    var consultant = mock(Consultant.class);
    when(consultant.getId()).thenReturn(UUID.randomUUID().toString());
    when(consultant.getEmail()).thenReturn("a@b.com");
    when(consultant.isAbsent()).thenReturn(false);
    when(consultant.getRocketChatId()).thenReturn(RandomStringUtils.randomAlphanumeric(17));
    when(consultant.getNotifyNewFeedbackMessageFromAdviceSeeker()).thenReturn(false);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(consultant));
    when(keycloakService.userHasRole(anyString(), anyString())).thenReturn(true);

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).isEmpty();
  }

  @Test
  public void generateEmails_Should_ReturnExpectedMail_When_ConsultantIsOffline()
      throws RocketChatGetGroupMembersException {
    when(session.getUser()).thenReturn(USER);
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));
    when(rocketChatService.getChatUsers(anyString())).thenReturn(List.of(GROUP_MEMBER_USER_1));
    var consultant = mock(Consultant.class);
    when(consultant.getId()).thenReturn(UUID.randomUUID().toString());
    when(consultant.getEmail()).thenReturn("a@b.com");
    when(consultant.getLanguageCode()).thenReturn(LanguageCode.de);
    when(consultant.isAbsent()).thenReturn(false);
    when(consultant.getRocketChatId()).thenReturn(RandomStringUtils.randomAlphanumeric(17));
    when(consultant.getNotifyNewFeedbackMessageFromAdviceSeeker()).thenReturn(true);
    when(rocketChatService.isLoggedIn(anyString())).thenReturn(Optional.of(false));
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(consultant));
    when(keycloakService.userHasRole(anyString(), anyString())).thenReturn(true);

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).hasSize(1);
  }

  @Test
  public void generateEmails_Should_ReturnNoMail_When_ConsultantIsOnline()
      throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));
    when(rocketChatService.getChatUsers(anyString())).thenReturn(List.of(GROUP_MEMBER_USER_1));
    var consultant = mock(Consultant.class);
    when(consultant.getId()).thenReturn(UUID.randomUUID().toString());
    when(consultant.getEmail()).thenReturn("a@b.com");
    when(consultant.isAbsent()).thenReturn(false);
    when(consultant.getRocketChatId()).thenReturn(RandomStringUtils.randomAlphanumeric(17));
    when(consultant.getNotifyNewFeedbackMessageFromAdviceSeeker()).thenReturn(true);
    when(rocketChatService.isLoggedIn(anyString())).thenReturn(Optional.of(true));
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(consultant));
    when(keycloakService.userHasRole(anyString(), anyString())).thenReturn(true);

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).isEmpty();
  }

  @Test
  public void generateEmails_Should_ReturnEmptyList_When_SessionConsultantIsAbsent()
      throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT);
    setField(newFeedbackEmailSupplier, "userId", "other id");
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT));

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).isEmpty();
  }

  @Test
  public void generateEmails_Should_ReturnExpectedMail_When_AnotherConsultantWrote()
      throws RocketChatGetGroupMembersException {
    when(session.getUser()).thenReturn(USER);
    when(session.getConsultant()).thenReturn(CONSULTANT_2);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT_2));

    List<MailDTO> generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).hasSize(1);
    MailDTO generatedMail = generatedMails.get(0);
    assertThat(generatedMail.getTemplate()).isEqualTo(TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION);
    assertThat(generatedMail.getEmail()).isEqualTo("email@email.com");
    assertThat(generatedMail.getLanguage())
        .isEqualTo(de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode.DE);
    List<TemplateDataDTO> templateData = generatedMail.getTemplateData();
    assertThat(templateData).hasSize(4);
    assertThat(templateData.get(0).getKey()).isEqualTo("name_sender");
    assertThat(templateData.get(0).getValue()).isEqualTo("first name last name");
    assertThat(templateData.get(1).getKey()).isEqualTo("name_recipient");
    assertThat(templateData.get(1).getValue()).isEqualTo(("first name last name"));
    assertThat(templateData.get(2).getKey()).isEqualTo(("name_user"));
    assertThat(templateData.get(2).getValue()).isEqualTo(("username"));
    assertThat(templateData.get(3).getKey()).isEqualTo(("url"));
    assertThat(templateData.get(3).getValue()).isEqualTo(("applicationBaseUrl"));
  }

  @Test
  public void
      generateEmails_Should_ReturnExpectedMail_When_AnotherConsultantWroteAndFeedbackToggleIsOn()
          throws RocketChatGetGroupMembersException {
    when(session.getUser()).thenReturn(USER);
    when(session.getConsultant()).thenReturn(CONSULTANT_2);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT_2));

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).hasSize(1);
    assertThat(generatedMails.get(0).getDialect()).isEqualTo(CONSULTANT_2.getDialect());
  }

  @Test
  public void generateEmails_Should_ReturnNoMail_When_AnotherConsultantWroteAndFeedbackToggleIsOff()
      throws RocketChatGetGroupMembersException {
    var consultant = mock(Consultant.class);
    when(session.getConsultant()).thenReturn(consultant);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(consultant));

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).isEmpty();
  }

  @Test
  public void
      generateEmails_Should_ReturnExpectedMail_When_AnotherConsultantWroteAndConsultantIsOffline()
          throws RocketChatGetGroupMembersException {
    when(session.getUser()).thenReturn(USER);
    when(session.getConsultant()).thenReturn(CONSULTANT_2);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT_2));
    when(rocketChatService.isLoggedIn(anyString())).thenReturn(Optional.of(false));

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).hasSize(1);
  }

  @Test
  public void generateEmails_Should_ReturnNoMail_When_AnotherConsultantWroteAndConsultantIsOnline()
      throws RocketChatGetGroupMembersException {
    when(session.getConsultant()).thenReturn(CONSULTANT_2);
    setField(newFeedbackEmailSupplier, "userId", CONSULTANT.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(CONSULTANT_2));
    when(rocketChatService.isLoggedIn(anyString())).thenReturn(Optional.of(true));

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).isEmpty();
  }

  @Test
  public void generateEmails_Should_FilterNonMainConsultantsNorSessionAssignees() throws Exception {
    var consultant = givenAConsultant();
    consultant.setRocketChatId(RandomStringUtils.random(17));

    whenConsultantIsMain(false);
    when(session.getConsultant()).thenReturn(consultant);
    setField(newFeedbackEmailSupplier, "userId", consultant.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(consultant));
    when(rocketChatService.getChatUsers(anyString())).thenReturn(GROUP_MEMBER_DTO_LIST);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(CONSULTANT_2));

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).isEmpty();
  }

  @Test
  public void generateEmails_Should_NotFilter_WhenConsultantsAreMain() throws Exception {
    var consultant = givenAConsultant();
    consultant.setRocketChatId(RandomStringUtils.random(17));

    whenConsultantIsMain(true);
    when(session.getUser()).thenReturn(USER);
    when(session.getConsultant()).thenReturn(consultant);
    setField(newFeedbackEmailSupplier, "userId", consultant.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(consultant));
    when(rocketChatService.getChatUsers(anyString())).thenReturn(GROUP_MEMBER_DTO_LIST);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(CONSULTANT_2));

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).hasSize(4);
  }

  @Test
  public void generateEmails_Should_NotFilter_WhenConsultantsAreSessionAssigned() throws Exception {
    var consultant = givenAConsultant();

    whenConsultantIsMain(false);
    when(session.getUser()).thenReturn(USER);
    when(session.getConsultant()).thenReturn(consultant);
    setField(newFeedbackEmailSupplier, "userId", consultant.getId());
    when(consultantService.getConsultant(anyString())).thenReturn(Optional.of(consultant));
    when(rocketChatService.getChatUsers(anyString())).thenReturn(GROUP_MEMBER_DTO_LIST);
    when(consultantService.getConsultantByRcUserId(anyString()))
        .thenReturn(Optional.of(CONSULTANT_2));

    var generatedMails = newFeedbackEmailSupplier.generateEmails();

    assertThat(generatedMails).hasSize(4);
    assertThat(generatedMails).extracting("dialect").containsOnly(CONSULTANT_2.getDialect());
  }

  private Consultant givenAConsultant() throws JsonProcessingException {
    var content = MAPPER.writeValueAsString(CONSULTANT);

    return MAPPER.readValue(content, Consultant.class);
  }

  private void whenConsultantIsMain(boolean returnValue) {
    when(keycloakService.userHasRole(anyString(), eq("main-consultant"))).thenReturn(returnValue);
  }
}
