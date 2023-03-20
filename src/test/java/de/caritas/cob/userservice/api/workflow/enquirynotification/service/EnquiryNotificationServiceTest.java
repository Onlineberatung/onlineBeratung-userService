package de.caritas.cob.userservice.api.workflow.enquirynotification.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.service.emailsupplier.EmailSupplier.TEMPLATE_DAILY_ENQUIRY_NOTIFICATION;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggleService;
import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailsDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnquiryNotificationServiceTest {

  @InjectMocks private EnquiryNotificationService enquiryNotificationService;

  @Mock private MailService mailService;

  @Mock private SessionRepository sessionRepository;

  @Mock private ConsultantAgencyService consultantAgencyService;

  @Mock private AgencyService agencyService;

  @Mock private ReleaseToggleService releaseToggleService;

  @BeforeEach
  public void setup() {
    setField(enquiryNotificationService, "openEnquiryCheckHours", 12L);
    setField(enquiryNotificationService, "applicationBaseUrl", "base/url");
  }

  @Test
  void
      sendEmailNotificationsForOpenEnquiries_Should_sendExpectedMailsToConsultantsOfAgency_When_agencyHasOpenEnquiries() {
    var openEnquiries = openEnquiriesForAgency(1L, nowInUtc().minusHours(13L), 3);
    openEnquiries.addAll(openEnquiriesForAgency(2L, nowInUtc().minusHours(13L), 2));
    openEnquiries.addAll(openEnquiriesForAgency(3L, nowInUtc().minusHours(13L), 1));
    openEnquiries.addAll(openEnquiriesForAgency(4L, nowInUtc().minusHours(11L), 5));
    when(sessionRepository.findByStatus(SessionStatus.NEW)).thenReturn(openEnquiries);
    when(consultantAgencyService.findConsultantsByAgencyId(1L))
        .thenReturn(
            List.of(
                createConsultantAgencyWithConsultantsMailAddress(
                    "consultant1", "firstname1 lastname1"),
                createConsultantAgencyWithConsultantsMailAddress(
                    "consultant2", "firstname2 lastname2")));
    when(consultantAgencyService.findConsultantsByAgencyId(2L))
        .thenReturn(
            List.of(
                createConsultantAgencyWithConsultantsMailAddress(
                    "consultant3", "firstname3 lastname3")));
    when(consultantAgencyService.findConsultantsByAgencyId(3L))
        .thenReturn(
            List.of(
                createConsultantAgencyWithConsultantsMailAddress(
                    "consultant4", "firstname4 lastname4")));
    var agencies =
        asList(
            createAgency(1L, "Blue Agency"),
            createAgency(2L, "Red Agency"),
            createAgency(3L, "Yellow Agency"));
    when(agencyService.getAgencies(asList(1L, 2L, 3L))).thenReturn(agencies);

    enquiryNotificationService.sendEmailNotificationsForOpenEnquiries();

    var expectedMailsDTO =
        List.of(
            buildExpectedMail("consultant1", "firstname1 lastname1", "Blue Agency", 3L),
            buildExpectedMail("consultant2", "firstname2 lastname2", "Blue Agency", 3L),
            buildExpectedMail("consultant3", "firstname3 lastname3", "Red Agency", 2L),
            buildExpectedMail("consultant4", "firstname4 lastname4", "Yellow Agency", 1L));
    var argumentCaptor = ArgumentCaptor.forClass(MailsDTO.class);
    verify(mailService, times(3)).sendEmailNotification(argumentCaptor.capture());
    var resultMailsDTO =
        argumentCaptor.getAllValues().stream()
            .map(MailsDTO::getMails)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    assertThat(resultMailsDTO, containsInAnyOrder(expectedMailsDTO.toArray()));
  }

  @Test
  void
      sendEmailNotificationsForOpenEnquiries_Should_sendNoMails_When_agencyHasOpenEnquiriesYoungerThanCheckTime() {
    var openEnquiries = openEnquiriesForAgency(1L, nowInUtc().minusHours(11L), 3);
    when(sessionRepository.findByStatus(SessionStatus.NEW)).thenReturn(openEnquiries);

    enquiryNotificationService.sendEmailNotificationsForOpenEnquiries();

    verifyNoInteractions(mailService);
  }

  @Test
  void sendEmailNotificationsForOpenEnquiries_Should_sendNoMails_When_noOpenEnquiriesExists() {
    enquiryNotificationService.sendEmailNotificationsForOpenEnquiries();

    verifyNoInteractions(mailService);
  }

  @Test
  void
      sendEmailNotificationsForOpenEnquiries_Should_sendNoMails_When_agenciesWithOpenEnquiriesHaveNoConsultants() {
    var openEnquiries = openEnquiriesForAgency(1L, nowInUtc().minusHours(13L), 3);
    openEnquiries.addAll(openEnquiriesForAgency(1L, nowInUtc().minusHours(13L), 2));
    openEnquiries.addAll(openEnquiriesForAgency(2L, nowInUtc().minusHours(13L), 1));
    openEnquiries.addAll(openEnquiriesForAgency(3L, nowInUtc().minusHours(11L), 5));
    when(sessionRepository.findByStatus(SessionStatus.NEW)).thenReturn(openEnquiries);

    enquiryNotificationService.sendEmailNotificationsForOpenEnquiries();

    verifyNoInteractions(mailService);
  }

  @Test
  void
      sendEmailNotificationsForOpenEnquiries_Should_sendNoMails_When_agenciesWithOpenEnquiriesAreNotToBeNotified() {
    var openEnquiries = openEnquiriesForAgency(2L, nowInUtc().minusHours(13L), 1);
    when(consultantAgencyService.findConsultantsByAgencyId(2L))
        .thenReturn(
            List.of(
                createConsultantAgencyWithConsultantsMailAddress(
                    "consultant3", "firstname3 lastname3", false)));
    when(sessionRepository.findByStatus(SessionStatus.NEW)).thenReturn(openEnquiries);

    enquiryNotificationService.sendEmailNotificationsForOpenEnquiries();

    verifyNoInteractions(mailService);
  }

  @Test
  void sendEmailNotificationsForOpenEnquiries_Should_sendNoMails_When_enquiryDateDoesNotExist() {
    var openEnquiries = openEnquiriesForAgency(1L, null, 3);
    when(sessionRepository.findByStatus(SessionStatus.NEW)).thenReturn(openEnquiries);

    enquiryNotificationService.sendEmailNotificationsForOpenEnquiries();

    verifyNoInteractions(mailService);
  }

  private List<Session> openEnquiriesForAgency(
      Long agencyId, LocalDateTime enquiryDate, int amount) {
    var enquiries = new ArrayList<Session>();
    for (int i = 0; i < amount; i++) {
      var session = new Session();
      session.setAgencyId(agencyId);
      session.setEnquiryMessageDate(enquiryDate);
      enquiries.add(session);
    }
    return enquiries;
  }

  private ConsultantAgency createConsultantAgencyWithConsultantsMailAddress(
      String mail, String fullName) {
    return createConsultantAgencyWithConsultantsMailAddress(mail, fullName, true);
  }

  private ConsultantAgency createConsultantAgencyWithConsultantsMailAddress(
      String mail, String fullName, boolean notifyEnqRep) {
    var consultant = new Consultant();
    String[] firstNameLastName = fullName.split(" ");
    consultant.setFirstName(firstNameLastName[0]);
    consultant.setLastName(firstNameLastName[1]);
    consultant.setEmail(mail);
    consultant.setLanguageCode(LanguageCode.de);
    consultant.setNotifyEnquiriesRepeating(notifyEnqRep);
    var consultantAgency = new ConsultantAgency();
    consultantAgency.setConsultant(consultant);

    return consultantAgency;
  }

  private AgencyDTO createAgency(long id, String name) {
    AgencyDTO agency = new AgencyDTO();
    agency.setId(id);
    agency.setName(name);
    return agency;
  }

  private MailDTO buildExpectedMail(
      String email, String consultantName, String agencyName, Long amountOfOpenEnquiries) {
    return new MailDTO()
        .template(TEMPLATE_DAILY_ENQUIRY_NOTIFICATION)
        .email(email)
        .language(de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode.DE)
        .templateData(
            asList(
                new TemplateDataDTO()
                    .key("subject")
                    .value("Online-Beratung | Unbeantwortete Erstanfragen"),
                new TemplateDataDTO().key("consultant_name").value(consultantName),
                new TemplateDataDTO().key("url").value("base/url"),
                new TemplateDataDTO().key("agency_name").value(agencyName),
                new TemplateDataDTO()
                    .key("enquiries")
                    .value(String.valueOf(amountOfOpenEnquiries))));
  }
}
