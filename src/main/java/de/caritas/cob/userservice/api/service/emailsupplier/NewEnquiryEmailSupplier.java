package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.EmailNotificationUtils.deserializeNotificationSettingsDTOOrDefaultIfNull;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.NotificationsAware;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggle;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggleService;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Supplier to provide mails to be sent when a new enquiry was created. */
@RequiredArgsConstructor
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NewEnquiryEmailSupplier implements EmailSupplier {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final @NonNull AgencyService agencyService;

  private final @NonNull ReleaseToggleService releaseToggleService;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  private Session session;

  @Value("${multitenancy.enabled}")
  private boolean multiTenancyEnabled;

  private final TenantTemplateSupplier tenantTemplateSupplier;

  public void setCurrentSession(Session session) {
    this.session = session;
  }

  /**
   * Generates the enquiry notification mails sent to regarding consultants when a new enquiry has
   * been created.
   *
   * @return a list of the generated {@link MailDTO}
   */
  @Override
  @Transactional
  public List<MailDTO> generateEmails() {
    log.info("Generating emails for new enquiry");
    List<ConsultantAgency> consultantAgencyList =
        consultantAgencyRepository.findByAgencyIdAndDeleteDateIsNull(session.getAgencyId());
    log.info("Retrieved consultant agency list {}", consultantAgencyList);
    if (isEmpty(consultantAgencyList)) {
      return emptyList();
    }
    AgencyDTO agency = agencyService.getAgency(session.getAgencyId());
    log.info("Retrieved agency {}", agency);
    return consultantAgencyList.stream()
        .filter(this::validConsultantAgency)
        .filter(this::shouldSendNewEnquiryNotificationForConsultant)
        .map(toEnquiryMailDTO(agency))
        .collect(Collectors.toList());
  }

  private Boolean validConsultantAgency(ConsultantAgency consultantAgency) {
    return nonNull(consultantAgency)
        && isNotBlank(consultantAgency.getConsultant().getEmail())
        && !consultantAgency.getConsultant().isAbsent();
  }

  private boolean shouldSendNewEnquiryNotificationForConsultant(ConsultantAgency consultantAgency) {
    if (releaseToggleService.isToggleEnabled(ReleaseToggle.NEW_EMAIL_NOTIFICATIONS)) {
      return wantsToReceiveNotificationsAboutNewEnquiry(consultantAgency.getConsultant());
    }
    return true;
  }

  private boolean wantsToReceiveNotificationsAboutNewEnquiry(
      NotificationsAware notificationsAware) {
    NotificationsSettingsDTO notificationsSettingsDTO =
        deserializeNotificationSettingsDTOOrDefaultIfNull(notificationsAware);
    return notificationsAware.isNotificationsEnabled()
        && notificationsSettingsDTO.getInitialEnquiryNotificationEnabled();
  }

  private Function<ConsultantAgency, MailDTO> toEnquiryMailDTO(AgencyDTO agency) {
    return consultantAgency ->
        mailOf(consultantAgency.getConsultant(), session.getPostcode(), agency.getName());
  }

  private MailDTO mailOf(Consultant consultant, String postCode, String agency) {

    var templateAttributes = new ArrayList<TemplateDataDTO>();
    templateAttributes.add(new TemplateDataDTO().key("name").value(consultant.getFullName()));
    templateAttributes.add(new TemplateDataDTO().key("plz").value(postCode));
    templateAttributes.add(new TemplateDataDTO().key("beratungsstelle").value(agency));

    if (!multiTenancyEnabled) {
      templateAttributes.add(new TemplateDataDTO().key("url").value(applicationBaseUrl));
    } else {
      templateAttributes.addAll(tenantTemplateSupplier.getTemplateAttributes());
    }

    var language =
        de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode.fromValue(
            consultant.getLanguageCode().toString());

    return new MailDTO()
        .template(TEMPLATE_NEW_ENQUIRY_NOTIFICATION)
        .email(consultant.getEmail())
        .language(language)
        .dialect(consultant.getDialect())
        .templateData(templateAttributes);
  }
}
