package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.EmailNotificationTemplates.TEMPLATE_ASSIGN_ENQUIRY_NOTIFICATION;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Supplier to provide mails to be sent when an enquiry is assigned.
 */
@Slf4j
@AllArgsConstructor
public class AssignEnquiryEmailSupplier implements EmailSupplier {

  private final Consultant receiverConsultant;
  private final String senderUserId;
  private final String askerUserName;
  private final String applicationBaseUrl;
  private final ConsultantService consultantService;

  /**
   * Generates the enquiry notification mail sent to regarding consultant.
   *
   * @return a list of the generated {@link MailDTO}
   */
  @Override
  public List<MailDTO> generateEmails() {
    if (isReceiverConsultantValid()) {
      return buildAssignEnquiryMailWithValidReceiver();
    }
    var receiverId = nonNull(receiverConsultant) ? receiverConsultant.getId() : "unknown";
    log.error(
        "EmailNotificationFacade error: Error while sending assign message notification: Receiver "
            + "consultant with id {} is null or doesn't have an email address.", receiverId
    );

    return emptyList();
  }

  private boolean isReceiverConsultantValid() {
    return nonNull(receiverConsultant) && isNotBlank(receiverConsultant.getEmail());
  }

  private List<MailDTO> buildAssignEnquiryMailWithValidReceiver() {

    Optional<Consultant> senderConsultant = consultantService.getConsultant(senderUserId);
    if (senderConsultant.isPresent()) {
      return singletonList(buildMailDtoForAssignEnquiryNotification(
          receiverConsultant.getEmail(),
          senderConsultant.get().getFullName(),
          receiverConsultant.getFullName(),
          new UsernameTranscoder().decodeUsername(askerUserName)));
    }
    log.error(
        "EmailNotificationFacade error: Error while sending assign message notification: Sender "
            + "consultant with id {} could not be found in database.", senderUserId
    );

    return emptyList();
  }

  private MailDTO buildMailDtoForAssignEnquiryNotification(String email, String nameSender,
      String nameRecipient, String nameUser) {
    return new MailDTO()
        .template(TEMPLATE_ASSIGN_ENQUIRY_NOTIFICATION)
        .email(email)
        .templateData(asList(
            new TemplateDataDTO().key("name_sender").value(nameSender),
            new TemplateDataDTO().key("name_recipient").value(nameRecipient),
            new TemplateDataDTO().key("name_user").value(nameUser),
            new TemplateDataDTO().key("url").value(applicationBaseUrl)));
  }

}
