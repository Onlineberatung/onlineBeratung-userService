package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.service.emailsupplier.EmailSupplier.TEMPLATE_NEW_ENQUIRY_NOTIFICATION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_DTO_U25;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MAIN_CONSULTANT;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NewEnquiryEmailSupplierTest {

  private NewEnquiryEmailSupplier newEnquiryEmailSupplier;

  @Mock private Session session;

  @Mock private ConsultantAgencyRepository consultantAgencyRepository;

  @Mock private AgencyService agencyService;

  @Before
  public void setup() {
    this.newEnquiryEmailSupplier =
        new NewEnquiryEmailSupplier(consultantAgencyRepository, agencyService, null);
    this.newEnquiryEmailSupplier.setCurrentSession(session);
  }

  @Test
  public void generateEmails_Should_ReturnEmptyList_When_NoParametersAreProvided() {
    List<MailDTO> generatedMails = newEnquiryEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
  }

  @Test
  public void generateEmails_Should_ReturnEmptyList_When_NoValidConsultantWasFound() {
    Consultant absentConsultant = new Consultant();
    absentConsultant.setAbsent(true);
    absentConsultant.setEmail("email");
    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(anyLong()))
        .thenReturn(
            asList(
                null,
                new ConsultantAgency(
                    0L, new Consultant(), 0L, nowInUtc(), nowInUtc(), nowInUtc(), null, null),
                new ConsultantAgency(
                    1L, absentConsultant, 1L, nowInUtc(), nowInUtc(), nowInUtc(), null, null)));

    List<MailDTO> generatedMails = newEnquiryEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(0));
  }

  @Test
  public void generateEmails_Should_ReturnExpectedMailDTO_When_PresentConsultantsWereFound() {
    when(consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(anyLong()))
        .thenReturn(
            asList(
                new ConsultantAgency(
                    0L, MAIN_CONSULTANT, 0L, nowInUtc(), nowInUtc(), nowInUtc(), null, null),
                new ConsultantAgency(
                    1L, MAIN_CONSULTANT, 1L, nowInUtc(), nowInUtc(), nowInUtc(), null, null)));
    when(agencyService.getAgency(any())).thenReturn(AGENCY_DTO_U25);
    when(session.getPostcode()).thenReturn("12345");

    List<MailDTO> generatedMails = newEnquiryEmailSupplier.generateEmails();

    assertThat(generatedMails, hasSize(2));
    MailDTO generatedMail = generatedMails.get(0);
    assertThat(generatedMail.getTemplate(), is(TEMPLATE_NEW_ENQUIRY_NOTIFICATION));
    assertThat(generatedMail.getEmail(), is("email@email.com"));
    assertThat(generatedMail.getLanguage(), is(LanguageCode.DE));
    List<TemplateDataDTO> templateData = generatedMail.getTemplateData();
    assertThat(templateData, hasSize(4));
    assertThat(templateData.get(0).getKey(), is("name"));
    assertThat(templateData.get(0).getValue(), is("first name last name"));
    assertThat(templateData.get(1).getKey(), is("plz"));
    assertThat(templateData.get(1).getValue(), is("12345"));
    assertThat(templateData.get(2).getKey(), is("beratungsstelle"));
    assertThat(templateData.get(2).getValue(), is("Test Beratungsstelle"));
    assertThat(templateData.get(3).getKey(), is("url"));
  }
}
