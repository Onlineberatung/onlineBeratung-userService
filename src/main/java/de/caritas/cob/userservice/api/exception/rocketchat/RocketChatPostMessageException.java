package de.caritas.cob.userservice.api.exception.rocketchat;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.exception.CreateEnquiryException;

public class RocketChatPostMessageException extends CreateEnquiryException {

  private static final long serialVersionUID = -2247287831013110339L;

  /**
   * Exception when posting a message to a Rocket.Chat group fails
   *
   * @param message Message
   * @param exception Exception
   */
  public RocketChatPostMessageException(
      String message, Exception exception, CreateEnquiryExceptionInformation exceptionInformation) {
    super(message, exception, exceptionInformation);
  }

  /**
   * Exception when posting a message to a Rocket.Chat group fails
   *
   * @param message Message
   */
  public RocketChatPostMessageException(
      String message, CreateEnquiryExceptionInformation exceptionInformation) {
    super(message, exceptionInformation);
  }
}
