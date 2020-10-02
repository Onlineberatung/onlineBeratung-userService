package de.caritas.cob.userservice.api.service.emailsupplier;

import de.caritas.cob.userservice.api.exception.AgencyServiceHelperException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.model.mailservice.MailDTO;
import java.util.List;

/**
 * Supplier to provide functionality for generate emails in several contexts.
 */
public interface EmailSupplier {

  /**
   * Functionality to generate a list of {@link MailDTO} used in
   * {@link de.caritas.cob.userservice.api.facade.EmailNotificationFacade}.
   *
   * @return the generated emails
   */
  List<MailDTO> generateEmails()
      throws RocketChatGetGroupMembersException, AgencyServiceHelperException;

}
