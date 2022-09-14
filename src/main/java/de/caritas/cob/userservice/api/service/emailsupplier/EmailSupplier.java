package de.caritas.cob.userservice.api.service.emailsupplier;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.mailservice.generated.web.model.MailDTO;
import java.util.List;

/** Supplier to provide functionality to generate emails in several contexts. */
public interface EmailSupplier {

  String TEMPLATE_NEW_ENQUIRY_NOTIFICATION = "enquiry-notification-consultant";
  String TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT = "message-notification-consultant";
  String TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER = "message-notification-asker";
  String TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION = "feedback-message-notification";
  String TEMPLATE_ASSIGN_ENQUIRY_NOTIFICATION = "assign-enquiry-notification";
  String TEMPLATE_FREE_TEXT = "free-text";
  String TEMPLATE_DAILY_ENQUIRY_NOTIFICATION = "daily-enquiry-notification";
  String TEMPLATE_REASSIGN_REQUEST_NOTIFICATION = "reassign-request-notification";
  String TEMPLATE_REASSIGN_CONFIRMATION_NOTIFICATION = "reassign-confirmation-notification";

  /**
   * Functionality to generate a list of {@link MailDTO} used in {@link EmailNotificationFacade}.
   *
   * @return the generated emails
   */
  List<MailDTO> generateEmails() throws RocketChatGetGroupMembersException;
}
