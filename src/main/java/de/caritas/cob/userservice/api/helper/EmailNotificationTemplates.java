package de.caritas.cob.userservice.api.helper;

/**
 * Templates of MailService for Email notifications.
 */
public class EmailNotificationTemplates {

  private EmailNotificationTemplates() {}

  public static final String TEMPLATE_NEW_ENQUIRY_NOTIFICATION = "enquiry-notification-consultant";
  public static final String TEMPLATE_NEW_MESSAGE_NOTIFICATION_CONSULTANT =
      "message-notification-consultant";
  public static final String TEMPLATE_NEW_MESSAGE_NOTIFICATION_ASKER = "message-notification-asker";
  public static final String TEMPLATE_NEW_FEEDBACK_MESSAGE_NOTIFICATION =
      "feedback-message-notification";
  public static final String TEMPLATE_ASSIGN_ENQUIRY_NOTIFICATION = "assign-enquiry-notification";
  public static final String TEMPLATE_FREE_TEXT = "free-text";

}
