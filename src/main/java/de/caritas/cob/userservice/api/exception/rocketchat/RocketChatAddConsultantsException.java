package de.caritas.cob.userservice.api.exception.rocketchat;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.exception.CreateEnquiryException;

public class RocketChatAddConsultantsException extends CreateEnquiryException {

  private static final long serialVersionUID = -3027804676762081926L;

  /**
   * Exception when adding consultants to a Rocket.Chat group fails.
   *
   * @param message Error Message
   * @param exception Exception
   * @param exceptionInformation {@link CreateEnquiryExceptionInformation}
   */
  public RocketChatAddConsultantsException(
      String message, Exception exception, CreateEnquiryExceptionInformation exceptionInformation) {
    super(message, exception, exceptionInformation);
  }
}
