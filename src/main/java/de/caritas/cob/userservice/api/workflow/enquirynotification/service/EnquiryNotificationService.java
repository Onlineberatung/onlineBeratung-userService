package de.caritas.cob.userservice.api.workflow.enquirynotification.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.helper.EmailNotificationUtils.deserializeNotificationSettingsOrDefaultIfNull;
import static de.caritas.cob.userservice.api.service.emailsupplier.EmailSupplier.TEMPLATE_DAILY_ENQUIRY_NOTIFICATION;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggle;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggleService;
import de.caritas.cob.userservice.api.service.helper.MailService;
import de.caritas.cob.userservice.api.workflow.enquirynotification.model.EnquiriesNotificationMailContent;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailsDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service to build and send email notifications for open enquiries. */
@Service
@RequiredArgsConstructor
public class EnquiryNotificationService {

  private static final String MAIL_SUBJECT = "Online-Beratung | Unbeantwortete Erstanfragen";
  private static final String UNKNOWN_AGENCY = "Unbekannte Beratungsstelle";

  private final @NonNull MailService mailService;
  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull AgencyService agencyService;

  private final @NonNull ReleaseToggleService releaseToggleService;

  @Value("${enquiry.open.notification.check.hours}")
  private Long openEnquiryCheckHours;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  /** Entry method to build and send email notifications. */
  public void sendEmailNotificationsForOpenEnquiries() {
    var agencyIdsWithOpenEnquiries = findAgencyIdsWithOpenEnquiries();
    var agenciesWithOpenEnquiries =
        agencyService.getAgencies(new ArrayList<>(agencyIdsWithOpenEnquiries.keySet()));
    var agencyIdToAgency =
        agenciesWithOpenEnquiries.stream()
            .collect(Collectors.toMap(AgencyDTO::getId, Function.identity()));
    var mailsContentForAgencies =
        createMailsContentForAgencies(agencyIdsWithOpenEnquiries, agencyIdToAgency);

    mailsContentForAgencies.forEach(this::buildAndSendEnquiryNotificationMails);
  }

  private Map<Long, Long> findAgencyIdsWithOpenEnquiries() {
    return sessionRepository.findByStatus(SessionStatus.NEW).stream()
        .filter(this::longerOpenThanCheckHours)
        .map(Session::getAgencyId)
        .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
  }

  private boolean longerOpenThanCheckHours(Session session) {
    var rightNow = nowInUtc();
    var enquiryMessageDate = session.getEnquiryMessageDate();

    if (nonNull(enquiryMessageDate)) {
      return rightNow.minusHours(openEnquiryCheckHours).isAfter(enquiryMessageDate);
    }
    return false;
  }

  private Collection<EnquiriesNotificationMailContent> createMailsContentForAgencies(
      Map<Long, Long> agencyIdsWithOpenEnquiries, Map<Long, AgencyDTO> agencyIdToAgency) {
    return agencyIdsWithOpenEnquiries.entrySet().stream()
        .map(toMailContent(agencyIdToAgency))
        .collect(Collectors.toSet());
  }

  private Function<Entry<Long, Long>, EnquiriesNotificationMailContent> toMailContent(
      Map<Long, AgencyDTO> agencyIdToAgency) {
    return agencyIdEnquiriesEntry -> {
      var agencyId = agencyIdEnquiriesEntry.getKey();
      var openEnquiries = agencyIdEnquiriesEntry.getValue();
      AgencyDTO agency = agencyIdToAgency.get(agencyId);
      var agencyName = agency == null ? UNKNOWN_AGENCY : agency.getName();
      return EnquiriesNotificationMailContent.builder()
          .agencyId(agencyId)
          .amountOfOpenEnquiries(openEnquiries)
          .agencyName(agencyName)
          .build();
    };
  }

  private void buildAndSendEnquiryNotificationMails(
      EnquiriesNotificationMailContent enquiryMailContent) {
    var mailDTOs =
        consultantAgencyService.findConsultantsByAgencyId(enquiryMailContent.getAgencyId()).stream()
            .map(ConsultantAgency::getConsultant)
            .filter(c -> wantsToReceiveNotifications(c))
            .map(consultant -> buildMailTO(consultant, enquiryMailContent))
            .collect(Collectors.toList());

    buildAndSendNotificationEmail(mailDTOs);
  }

  private boolean wantsToReceiveNotifications(Consultant consultant) {
    if (releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS)) {
      return consultant.isNotificationsEnabled()
          && deserializeNotificationSettingsOrDefaultIfNull(consultant)
              .isInitialEnquiryNotificationEnabled();
    } else {
      return consultant.getNotifyEnquiriesRepeating();
    }
  }

  private MailDTO buildMailTO(
      Consultant consultant, EnquiriesNotificationMailContent enquiryNotificationContent) {
    return new MailDTO()
        .template(TEMPLATE_DAILY_ENQUIRY_NOTIFICATION)
        .email(consultant.getEmail())
        .language(languageOf(consultant.getLanguageCode()))
        .templateData(
            asList(
                new TemplateDataDTO().key("subject").value(MAIL_SUBJECT),
                new TemplateDataDTO().key("consultant_name").value(consultant.getFullName()),
                new TemplateDataDTO().key("url").value(applicationBaseUrl),
                new TemplateDataDTO()
                    .key("agency_name")
                    .value(enquiryNotificationContent.getAgencyName()),
                new TemplateDataDTO()
                    .key("enquiries")
                    .value(String.valueOf(enquiryNotificationContent.getAmountOfOpenEnquiries()))));
  }

  private void buildAndSendNotificationEmail(List<MailDTO> mailsToSend) {
    if (isNotEmpty(mailsToSend)) {
      var mailsDTO = new MailsDTO().mails(mailsToSend);
      mailService.sendEmailNotification(mailsDTO);
    }
  }

  private static de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode languageOf(
      LanguageCode languageCode) {
    return de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode.fromValue(
        languageCode.toString());
  }
}
