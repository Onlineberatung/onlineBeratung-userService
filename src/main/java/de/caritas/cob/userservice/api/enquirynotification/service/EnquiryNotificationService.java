package de.caritas.cob.userservice.api.enquirynotification.service;

import static de.caritas.cob.userservice.api.helper.EmailNotificationTemplates.TEMPLATE_FREE_TEXT;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.enquirynotification.model.EnquiriesNotificationMailContent;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service to build and send email notifications for open enquiries.
 */
@Service
@RequiredArgsConstructor
public class EnquiryNotificationService {

  private final @NonNull MailService mailService;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull ConsultantAgencyService consultantAgencyService;

  @Value("${enquiry.open.notification.check.hours}")
  private Long openEnquiryCheckHours;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  /**
   * Entry method to build and send email notifications.
   */
  public void sendEmailNotificationsForOpenEnquiries() {
    var agenciesWithOpenEnquiries = collectAgenciesWithOpenEnquiries();
    var enquiryMailsContent = createMailsContentForAgencies(agenciesWithOpenEnquiries);

    enquiryMailsContent.forEach(this::buildAndSendNotificationMails);
  }

  private Map<Long, Long> collectAgenciesWithOpenEnquiries() {
    return sessionRepository.findByStatus(SessionStatus.NEW).stream()
        .filter(this::longerOpenThanCheckHours)
        .map(Session::getAgencyId)
        .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
  }

  private boolean longerOpenThanCheckHours(Session session) {
    var rightNow = nowInUtc();
    var enquiryMessageDate = session.getEnquiryMessageDate();

    if (nonNull(enquiryMessageDate)) {
      return rightNow
          .minusHours(openEnquiryCheckHours)
          .isAfter(enquiryMessageDate);
    }
    return false;
  }

  private Set<EnquiriesNotificationMailContent> createMailsContentForAgencies(
      Map<Long, Long> agenciesWithOpenEnquiries) {
    return agenciesWithOpenEnquiries.entrySet().stream()
        .map(this::toEnquiryMailContent)
        .collect(Collectors.toSet());
  }

  private EnquiriesNotificationMailContent toEnquiryMailContent(
      Entry<Long, Long> agenciesWithOpenEnquiries) {
    return EnquiriesNotificationMailContent.builder()
        .agencyId(agenciesWithOpenEnquiries.getKey())
        .amountOfOpenEnquiries(agenciesWithOpenEnquiries.getValue())
        .build();
  }

  private void buildAndSendNotificationMails(EnquiriesNotificationMailContent enquiryMailContent) {
    var mailDTOs = consultantAgencyService
        .findConsultantsByAgencyId(enquiryMailContent.getAgencyId()).stream()
        .map(ConsultantAgency::getConsultant)
        .map(Consultant::getEmail)
        .map(mailAddress -> buildMailTO(mailAddress, enquiryMailContent))
        .collect(Collectors.toList());

    buildAndSendNotificationEmail(mailDTOs);
  }

  private MailDTO buildMailTO(String email,
      EnquiriesNotificationMailContent enquiryNotificationContent) {
    return new MailDTO()
        .template(TEMPLATE_FREE_TEXT)
        .email(email)
        .templateData(asList(
            new TemplateDataDTO().key("subject").value("Open enquiries"),
            new TemplateDataDTO().key("url").value(applicationBaseUrl),
            new TemplateDataDTO().key("text").value(enquiryNotificationContent.toString())));
  }

  private void buildAndSendNotificationEmail(List<MailDTO> mailsToSend) {
    if (isNotEmpty(mailsToSend)) {
      var mailsDTO = new MailsDTO().mails(mailsToSend);
      mailService.sendEmailNotification(mailsDTO);
    }
  }

}
