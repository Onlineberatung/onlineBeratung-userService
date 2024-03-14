package de.caritas.cob.userservice.api.service.emailsupplier;

import static java.util.Arrays.asList;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.transaction.Transactional;
import lombok.Builder;

@Builder
public class ReassignmentConfirmationEmailSupplier implements EmailSupplier {

  private final Consultant receiverConsultant;
  private final String senderConsultantName;
  private final String applicationBaseUrl;
  private final TenantTemplateSupplier tenantTemplateSupplier;
  private final boolean multiTenancyEnabled;

  private final UsernameTranscoder usernameTranscoder = new UsernameTranscoder();

  @Override
  @Transactional
  public List<MailDTO> generateEmails() {
    return Collections.singletonList(buildMailDtoForReassignRequestNotification());
  }

  private MailDTO buildMailDtoForReassignRequestNotification() {
    var templateAttributes =
        new ArrayList<>(
            asList(
                new TemplateDataDTO()
                    .key("name_recipient")
                    .value(decodedUsernameOf(receiverConsultant)),
                new TemplateDataDTO()
                    .key("name_from_consultant")
                    .value(decodedUsernameOf(senderConsultantName))));

    if (!multiTenancyEnabled) {
      templateAttributes.add(new TemplateDataDTO().key("url").value(applicationBaseUrl));
    } else {
      templateAttributes.addAll(tenantTemplateSupplier.getTemplateAttributes());
    }

    return new MailDTO()
        .template(TEMPLATE_REASSIGN_CONFIRMATION_NOTIFICATION)
        .email(receiverConsultant.getEmail())
        .language(languageOf(receiverConsultant.getLanguageCode()))
        .dialect(receiverConsultant.getDialect())
        .templateData(templateAttributes);
  }

  private String decodedUsernameOf(Consultant consultant) {
    return decodedUsernameOf(consultant.getUsername());
  }

  private String decodedUsernameOf(String decoded) {
    return usernameTranscoder.decodeUsername(decoded);
  }

  private static de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode languageOf(
      LanguageCode languageCode) {
    return de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode.fromValue(
        languageCode.toString());
  }
}
