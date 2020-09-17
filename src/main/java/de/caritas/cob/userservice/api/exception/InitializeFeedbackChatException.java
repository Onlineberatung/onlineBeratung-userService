package de.caritas.cob.userservice.api.exception;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;

public class InitializeFeedbackChatException extends CreateEnquiryException {

  private static final long serialVersionUID = -9002763989727764277L;

  /**
   * Exception when the initialization of a feedback chat fails
   *
   * @param sessionId
   * @param exceptionInformation
   */
  public InitializeFeedbackChatException(Long sessionId,
      CreateEnquiryExceptionInformation exceptionInformation) {
    super(String.format("Could not create feedback chat group for session %s", sessionId), exceptionInformation);
  }
}
