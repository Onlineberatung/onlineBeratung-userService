package de.caritas.cob.userservice.api.exception;

public class NewMessageNotificationException extends RuntimeException {

  private static final long serialVersionUID = 5573541126570935402L;

  /**
   * New message notification exception
   *
   * @param message
   */
  public NewMessageNotificationException(String message) {
    super(message);
  }

  /**
   * New message notification exception
   *
   * @param message
   * @param ex
   */
  public NewMessageNotificationException(String message, Exception ex) {
    super(message, ex);
  }
}
