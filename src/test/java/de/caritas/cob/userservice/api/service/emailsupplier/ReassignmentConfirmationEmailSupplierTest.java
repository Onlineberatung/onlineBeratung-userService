package de.caritas.cob.userservice.api.service.emailsupplier;

import static de.caritas.cob.userservice.api.helper.EmailNotificationTemplates.TEMPLATE_REASSIGN_CONFIRMATION_NOTIFICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

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
    var senderConsultant = easyRandom.nextObject(Consultant.class);
    var reassignmetSupplier = ReassignmentConfirmationEmailSupplier.builder()
        .receiverConsultant(receiverConsultant)
        .applicationBaseUrl("base")
        .build();

    var mails = reassignmetSupplier.generateEmails();

    assertThat(mails, hasSize(1));
    var mail = mails.iterator().next();
    assertThat(mail.getEmail(), is(receiverConsultant.getEmail()));
    assertThat(mail.getTemplate(), is(TEMPLATE_REASSIGN_CONFIRMATION_NOTIFICATION));
    assertThat(mail.getTemplateData(), hasSize(2));
    assertThat(mail.getTemplateData().get(0).getKey(), is("name_recipient"));
    assertThat(mail.getTemplateData().get(0).getValue(), is(receiverConsultant.getUsername()));
    assertThat(mail.getTemplateData().get(1).getKey(), is("url"));
    assertThat(mail.getTemplateData().get(1).getValue(), is("base"));
  }

}
