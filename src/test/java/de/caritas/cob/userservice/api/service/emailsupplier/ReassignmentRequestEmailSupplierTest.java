package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.service.emailsupplier.EmailSupplier.TEMPLATE_REASSIGN_REQUEST_NOTIFICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.mailservice.generated.web.model.Dialect;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReassignmentRequestEmailSupplierTest {

  private static final EasyRandom easyRandom = new EasyRandom();

  @Test
  void generateEmails_Should_returnMailWithExpectedData() {
    var receiverLanguageCode = easyRandom.nextObject(LanguageCode.class);

    var reassignmentSupplier =
        ReassignmentRequestEmailSupplier.builder()
            .receiverEmailAddress("receiverMail")
            .receiverLanguageCode(receiverLanguageCode)
            .applicationBaseUrl("base")
            .receiverUsername("receiverUsername")
            .receiverDialect(Dialect.INFORMAL)
            .build();

    var mails = reassignmentSupplier.generateEmails();

    assertThat(mails, hasSize(1));
    var mail = mails.iterator().next();
    assertThat(mail.getEmail(), is("receiverMail"));
    assertThat(mail.getTemplate(), is(TEMPLATE_REASSIGN_REQUEST_NOTIFICATION));
    assertThat(mail.getLanguage(), is(notNullValue()));
    assertThat(mail.getDialect(), is(Dialect.INFORMAL));
    assertThat(mail.getLanguage().toString(), is(receiverLanguageCode.toString()));
    assertThat(mail.getTemplateData(), hasSize(2));
    assertThat(mail.getTemplateData().get(0).getKey(), is("name_recipient"));
    assertThat(mail.getTemplateData().get(0).getValue(), is("receiverUsername"));
    assertThat(mail.getTemplateData().get(1).getKey(), is("url"));
    assertThat(mail.getTemplateData().get(1).getValue(), is("base"));
  }
}
