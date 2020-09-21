package de.caritas.cob.userservice.api.exception;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;

public class InitializeChatException extends CreateEnquiryException {

  private static final long serialVersionUID = -9002763989727764277L;

  /**
   * Exception when the initialization of a chat fails
   *
   * @param sessionId
   * @param exceptionInformation
   */
  public InitializeChatException(Long sessionId,
      CreateEnquiryExceptionInformation exceptionInformation) {
    super(String.format("Could not create chat group for session %s", sessionId), exceptionInformation);
  }
}
