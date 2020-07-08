package de.caritas.cob.UserService.api.exception.rocketChat;

import de.caritas.cob.UserService.api.container.CreateEnquiryExceptionParameter;
import de.caritas.cob.UserService.api.exception.CreateEnquiryException;

public class RocketChatAddConsultantsAndTechUserException extends CreateEnquiryException {

  private static final long serialVersionUID = -3027804676762081926L;

  /**
   * Exception when adding consultants and the technical user to a Rocket.Chat group fails.
   * 
   * @param message Error Message
   * @param exception Exception
   * @param exceptionParameter {@link CreateEnquiryExceptionParameter}
   */
  public RocketChatAddConsultantsAndTechUserException(String message, Exception exception,
      CreateEnquiryExceptionParameter exceptionParameter) {
    super(message, exception, exceptionParameter);
  }
}
