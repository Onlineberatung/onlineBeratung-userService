package de.caritas.cob.userservice.api.service.emailsupplier;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Supplier to provide mails to be sent when an enquiry is assigned. */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AssignEnquiryEmailSupplier implements EmailSupplier {

  @Setter private Consultant receiverConsultant;

  @Setter private String senderUserId;

  @Setter private String askerUserName;

  @Value("${app.base.url}")
  private String applicationBaseUrl;

  @Autowired private ConsultantService consultantService;
  @Autowired private TenantTemplateSupplier tenantTemplateSupplier;

  @Value("${multitenancy.enabled}")
  private boolean multiTenancyEnabled;

  /**
   * Generates the enquiry notification mail sent to regarding consultant.
   *
   * @return a list of the generated {@link MailDTO}
   */
  @Override
  @Transactional
  public List<MailDTO> generateEmails() {
    if (isReceiverConsultantValid()) {
      return buildAssignEnquiryMailWithValidReceiver();
    }
    var receiverId = nonNull(receiverConsultant) ? receiverConsultant.getId() : "unknown";
    log.error(
        "EmailNotificationFacade error: Error while sending assign message notification: Receiver "
            + "consultant with id {} is null or doesn't have an email address.",
        receiverId);

    return emptyList();
  }

  private boolean isReceiverConsultantValid() {
    return nonNull(receiverConsultant) && isNotBlank(receiverConsultant.getEmail());
  }

  private List<MailDTO> buildAssignEnquiryMailWithValidReceiver() {

    Optional<Consultant> senderConsultant = consultantService.getConsultant(senderUserId);
    if (senderConsultant.isPresent()) {
      var nameUser = new UsernameTranscoder().decodeUsername(askerUserName);
      var mailDTO =
          mailOf(
              receiverConsultant.getEmail(),
              senderConsultant.get().getFullName(),
              receiverConsultant.getFullName(),
              nameUser,
              receiverConsultant.getLanguageCode());

      return singletonList(mailDTO);
    }
    log.error(
        "EmailNotificationFacade error: Error while sending assign message notification: Sender "
            + "consultant with id {} could not be found in database.",
        senderUserId);

    return emptyList();
  }

  private MailDTO mailOf(
      String email,
      String nameSender,
      String nameRecipient,
      String nameUser,
      LanguageCode languageCode) {
    var templateAttributes = new ArrayList<TemplateDataDTO>();
    templateAttributes.add(new TemplateDataDTO().key("name_sender").value(nameSender));
    templateAttributes.add(new TemplateDataDTO().key("name_recipient").value(nameRecipient));
    templateAttributes.add(new TemplateDataDTO().key("name_user").value(nameUser));

    if (!multiTenancyEnabled) {
      templateAttributes.add(new TemplateDataDTO().key("url").value(applicationBaseUrl));
    } else {
      templateAttributes.addAll(tenantTemplateSupplier.getTemplateAttributes());
    }

    var language =
        de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode.fromValue(
            languageCode.toString());

    return new MailDTO()
        .template(TEMPLATE_ASSIGN_ENQUIRY_NOTIFICATION)
        .email(email)
        .language(language)
        .templateData(templateAttributes);
  }
}
