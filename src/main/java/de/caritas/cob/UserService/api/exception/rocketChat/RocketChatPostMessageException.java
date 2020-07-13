package de.caritas.cob.UserService.api.exception.rocketChat;

import de.caritas.cob.UserService.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.UserService.api.exception.CreateEnquiryException;

public class RocketChatPostMessageException extends CreateEnquiryException {

  private static final long serialVersionUID = -2247287831013110339L;

  /**
   * Exception when posting a message to a Rocket.Chat group fails
   * 
   * @param message Message
   * @param exception Exception
   */
  public RocketChatPostMessageException(String message, Exception exception,
      CreateEnquiryExceptionInformation exceptionParameter) {
    super(message, exception, exceptionParameter);
  }

  /**
   * Exception when posting a message to a Rocket.Chat group fails
   * 
   * @param message Message
   */
  public RocketChatPostMessageException(String message,
      CreateEnquiryExceptionInformation exceptionParameter) {
    super(message, exceptionParameter);
  }
}
