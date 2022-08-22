package de.caritas.cob.userservice.api.exception.rocketchat;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.exception.CreateEnquiryException;

/** Exception when posting a further step message fails. */
public class RocketChatPostFurtherStepsMessageException extends CreateEnquiryException {

  private static final long serialVersionUID = -1247285841233110339L;

  /**
   * Exception when posting the further steps message to a Rocket.Chat group fails.
   *
   * @param message Message
   * @param exception Exception
   */
  public RocketChatPostFurtherStepsMessageException(
      String message, Exception exception, CreateEnquiryExceptionInformation exceptionInformation) {
    super(message, exception, exceptionInformation);
  }
}
