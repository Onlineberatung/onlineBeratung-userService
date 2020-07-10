package de.caritas.cob.UserService.api.exception;

import de.caritas.cob.UserService.api.container.CreateEnquiryExceptionParameter;

public class CreateMonitoringException extends CreateEnquiryException {

  private static final long serialVersionUID = -5776764841743979032L;

  /**
   * Exception when creating the inital monitoring fails
   * 
   * @param message Error Message
   * @param exception Exception
   * @param exceptionParameter {@link CreateEnquiryExceptionParameter}
   */
  public CreateMonitoringException(String message, Exception exception,
      CreateEnquiryExceptionParameter exceptionParameter) {
    super(message, exception, exceptionParameter);
  }
}
