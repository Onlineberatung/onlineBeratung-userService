package de.caritas.cob.userservice.api.service.emailsupplier;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.mailservice.generated.web.model.Dialect;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import de.caritas.cob.userservice.mailservice.generated.web.model.TemplateDataDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.transaction.Transactional;
import lombok.Builder;

@Builder
public class ReassignmentRequestEmailSupplier implements EmailSupplier {

  private final String receiverEmailAddress;
  private final LanguageCode receiverLanguageCode;
  private final String receiverUsername;

  private final Dialect receiverDialect;
  private final String applicationBaseUrl;
  private final TenantTemplateSupplier tenantTemplateSupplier;
  private final boolean multiTenancyEnabled;

  @Override
  @Transactional
  public List<MailDTO> generateEmails() {
    return Collections.singletonList(buildMailDtoForReassignRequestNotification());
  }

  private MailDTO buildMailDtoForReassignRequestNotification() {
    var decodedUsername = new UsernameTranscoder().decodeUsername(receiverUsername);
    var templateAttributes = new ArrayList<TemplateDataDTO>();
    templateAttributes.add(new TemplateDataDTO().key("name_recipient").value(decodedUsername));

    if (!multiTenancyEnabled) {
      templateAttributes.add(new TemplateDataDTO().key("url").value(applicationBaseUrl));
    } else {
      templateAttributes.addAll(tenantTemplateSupplier.getTemplateAttributes());
    }

    return new MailDTO()
        .template(TEMPLATE_REASSIGN_REQUEST_NOTIFICATION)
        .email(receiverEmailAddress)
        .dialect(receiverDialect)
        .language(languageOf(receiverLanguageCode))
        .templateData(templateAttributes);
  }

  private static de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode languageOf(
      LanguageCode languageCode) {
    return de.caritas.cob.userservice.mailservice.generated.web.model.LanguageCode.fromValue(
        languageCode.toString());
  }
}
