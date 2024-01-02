package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.EmailNotificationUtils.deserializeNotificationSettingsDTOOrDefaultIfNull;
import static de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode.fromValue;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.adapters.web.dto.NotificationsSettingsDTO;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.NotificationsAware;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggle;
import de.caritas.cob.userservice.api.service.consultingtype.ReleaseToggleService;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Supplier to provide mails to be sent when a new direct enquiry was created. */
@RequiredArgsConstructor
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NewDirectEnquiryEmailSupplier implements EmailSupplier {

  private final @NonNull ConsultantAgencyRepository consultantAgencyRepository;
  private final TenantTemplateSupplier tenantTemplateSupplier;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  @Value("${multitenancy.enabled}")
  private boolean multiTenancyEnabled;

  @Setter private Long agencyId;

  @Setter private String postCode;

  @Setter private String consultantId;

  private final @NonNull ReleaseToggleService releaseToggleService;

  /**
   * Generates a direct-enquiry email and sends it to the set consultant.
   *
   * @return a list of the generated {@link MailDTO}
   */
  @Override
  @Transactional
  public List<MailDTO> generateEmails() {
    log.info("Generating email for new direct enquiry...");

    var consultantAgencyList =
        consultantAgencyRepository.findByConsultantIdAndAgencyIdAndDeleteDateIsNull(
            consultantId, agencyId);
    log.info("Retrieved consultant agency list: {}", consultantAgencyList);

    return consultantAgencyList.stream()
        .filter(this::validConsultantAgency)
        .filter(this::shouldSendNewEnquiryNotificationForConsultant)
        .map(consultantAgency -> mailOf(consultantAgency.getConsultant(), postCode))
        .collect(Collectors.toList());
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

  private Boolean validConsultantAgency(ConsultantAgency consultantAgency) {
    var consultant = consultantAgency.getConsultant();

    return nonNull(consultant) && isNotBlank(consultant.getEmail()) && !consultant.isAbsent();
  }

  @SuppressWarnings("Duplicates")
  private MailDTO mailOf(Consultant consultant, String postCode) {
    var templateAttributes = new ArrayList<TemplateDataDTO>();
    templateAttributes.add(new TemplateDataDTO().key("name").value(consultant.getFullName()));
    templateAttributes.add(new TemplateDataDTO().key("plz").value(postCode));

    if (!multiTenancyEnabled) {
      templateAttributes.add(new TemplateDataDTO().key("url").value(applicationBaseUrl));
    } else {
      templateAttributes.addAll(tenantTemplateSupplier.getTemplateAttributes());
    }

    return new MailDTO()
        .template(TEMPLATE_NEW_DIRECT_ENQUIRY_NOTIFICATION)
        .email(consultant.getEmail())
        .language(fromValue(consultant.getLanguageCode().toString()))
        .templateData(templateAttributes)
        .dialect(consultant.getDialect());
  }


}
