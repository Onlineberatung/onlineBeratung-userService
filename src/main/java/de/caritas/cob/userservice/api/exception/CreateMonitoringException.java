package de.caritas.cob.userservice.api.exception;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;

public class CreateMonitoringException extends CreateEnquiryException {

  private static final long serialVersionUID = -5776764841743979032L;

  /**
   * Exception when creating the initial monitoring fails
   *
   * @param message              Error Message
   * @param exception            Exception
   * @param exceptionInformation {@link CreateEnquiryExceptionInformation}
   */
  public CreateMonitoringException(String message, Exception exception,
      CreateEnquiryExceptionInformation exceptionInformation) {
    super(message, exception, exceptionInformation);
  }
}
