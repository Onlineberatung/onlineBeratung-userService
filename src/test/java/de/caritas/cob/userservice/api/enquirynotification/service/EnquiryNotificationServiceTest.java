package de.caritas.cob.userservice.api.enquirynotification.service;

import static de.caritas.cob.userservice.api.helper.EmailNotificationTemplates.TEMPLATE_FREE_TEXT;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
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

  @InjectMocks
  private EnquiryNotificationService enquiryNotificationService;

  @Mock
  private MailService mailService;

  @Mock
  private SessionRepository sessionRepository;

  @Mock
  private ConsultantAgencyService consultantAgencyService;

  @BeforeEach
  public void setup() {
    setField(enquiryNotificationService, "openEnquiryCheckHours", 12L);
    setField(enquiryNotificationService, "applicationBaseUrl", "base/url");
  }

  @Test
  void sendEmailNotificationsForOpenEnquiries_Should_sendExpectedMailsToConsultantsOfAgency_When_agencyHasOpenEnquiries() {
    var openEnquiries = openEnquiriesForAgency(1L, nowInUtc().minusHours(13L), 3);
    openEnquiries.addAll(openEnquiriesForAgency(2L, nowInUtc().minusHours(13L), 2));
    openEnquiries.addAll(openEnquiriesForAgency(3L, nowInUtc().minusHours(13L), 1));
    openEnquiries.addAll(openEnquiriesForAgency(4L, nowInUtc().minusHours(11L), 5));
    when(sessionRepository.findByStatus(SessionStatus.NEW)).thenReturn(openEnquiries);
    when(consultantAgencyService.findConsultantsByAgencyId(1L)).thenReturn(List.of(
        createConsultantAgencyWithConsultantsMailAddress("consultant1"),
        createConsultantAgencyWithConsultantsMailAddress("consultant2")));
    when(consultantAgencyService.findConsultantsByAgencyId(2L)).thenReturn(List.of(
        createConsultantAgencyWithConsultantsMailAddress("consultant3")));
    when(consultantAgencyService.findConsultantsByAgencyId(3L)).thenReturn(List.of(
        createConsultantAgencyWithConsultantsMailAddress("consultant4")));

    enquiryNotificationService.sendEmailNotificationsForOpenEnquiries();

    var expectedMailsDTO = List.of(
        buildExpectedMail("consultant1", 3L),
        buildExpectedMail("consultant2", 3L),
        buildExpectedMail("consultant3", 2L),
        buildExpectedMail("consultant4", 1L));
    var argumentCaptor = ArgumentCaptor.forClass(MailsDTO.class);
    verify(mailService, times(3)).sendEmailNotification(argumentCaptor.capture());
    var resultMailsDTO = argumentCaptor.getAllValues().stream()
        .map(MailsDTO::getMails)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    assertThat(resultMailsDTO, containsInAnyOrder(expectedMailsDTO.toArray()));
  }

  @Test
  void sendEmailNotificationsForOpenEnquiries_Should_sendNoMails_When_agencyHasOpenEnquiriesYoungerThanCheckTime() {
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
  void sendEmailNotificationsForOpenEnquiries_Should_sendNoMails_When_agenciesWithOpenEnquiriesHaveNoConsultants() {
    var openEnquiries = openEnquiriesForAgency(1L, nowInUtc().minusHours(13L), 3);
    openEnquiries.addAll(openEnquiriesForAgency(1L, nowInUtc().minusHours(13L), 2));
    openEnquiries.addAll(openEnquiriesForAgency(2L, nowInUtc().minusHours(13L), 1));
    openEnquiries.addAll(openEnquiriesForAgency(3L, nowInUtc().minusHours(11L), 5));
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

  private List<Session> openEnquiriesForAgency(Long agencyId, LocalDateTime enquiryDate, int amount) {
    var enquiries = new ArrayList<Session>();
    for (int i = 0; i < amount; i++) {
      var session = new Session();
      session.setAgencyId(agencyId);
      session.setEnquiryMessageDate(enquiryDate);
      enquiries.add(session);
    }
    return enquiries;
  }

  private ConsultantAgency createConsultantAgencyWithConsultantsMailAddress(String mail) {
    var consultant = new Consultant();
    consultant.setEmail(mail);
    var consultantAgency = new ConsultantAgency();
    consultantAgency.setConsultant(consultant);
    return consultantAgency;
  }

  private MailDTO buildExpectedMail(String email, Long amountOfOpenEnquiries) {
    return new MailDTO()
        .template(TEMPLATE_FREE_TEXT)
        .email(email)
        .templateData(asList(
            new TemplateDataDTO().key("subject").value("Open enquiries"),
            new TemplateDataDTO().key("url").value("base/url"),
            new TemplateDataDTO().key("text")
                .value(String.format("Eine Ihrer Beratungsstellen hat aktuell %s offene Erstanfrage-/n.",
                    amountOfOpenEnquiries))));
  }

}
