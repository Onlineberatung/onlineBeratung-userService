package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.service.emailsupplier.EmailSupplier.TEMPLATE_REASSIGN_CONFIRMATION_NOTIFICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import de.caritas.cob.userservice.api.model.Consultant;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReassignmentConfirmationEmailSupplierTest {

  @Test
  void generateEmails_Should_returnMailWithExpectedData() {
    var easyRandom = new EasyRandom();
    var receiverConsultant = easyRandom.nextObject(Consultant.class);
    var reassignmentSupplier =
        ReassignmentConfirmationEmailSupplier.builder()
            .receiverConsultant(receiverConsultant)
            .senderConsultantName("seder name")
            .applicationBaseUrl("base")
            .build();

    var mails = reassignmentSupplier.generateEmails();

    assertThat(mails, hasSize(1));
    var mail = mails.iterator().next();
    assertThat(mail.getEmail(), is(receiverConsultant.getEmail()));
    assertThat(mail.getTemplate(), is(TEMPLATE_REASSIGN_CONFIRMATION_NOTIFICATION));

    var language = mail.getLanguage();
    assertThat(language, is(notNullValue()));
    assertThat(language.toString(), is(receiverConsultant.getLanguageCode().toString()));
    assertThat(mail.getDialect(), is(receiverConsultant.getDialect()));

    assertThat(mail.getTemplateData(), hasSize(3));
    assertThat(mail.getTemplateData().get(0).getKey(), is("name_recipient"));
    assertThat(mail.getTemplateData().get(0).getValue(), is(receiverConsultant.getUsername()));
    assertThat(mail.getTemplateData().get(1).getKey(), is("name_from_consultant"));
    assertThat(mail.getTemplateData().get(1).getValue(), is("seder name"));
    assertThat(mail.getTemplateData().get(2).getKey(), is("url"));
    assertThat(mail.getTemplateData().get(2).getValue(), is("base"));
  }
}
