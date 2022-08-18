package de.caritas.cob.userservice.api.service.emailsupplier;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import java.util.List;

/** Supplier to provide functionality to generate emails in several contexts. */
public interface EmailSupplier {

  /**
   * Functionality to generate a list of {@link MailDTO} used in {@link EmailNotificationFacade}.
   *
   * @return the generated emails
   */
  List<MailDTO> generateEmails() throws RocketChatGetGroupMembersException;
}
