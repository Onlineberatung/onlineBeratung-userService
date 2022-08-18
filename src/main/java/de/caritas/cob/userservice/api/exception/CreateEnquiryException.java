package de.caritas.cob.userservice.api.exception;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEnquiryException extends Exception {

  private static final long serialVersionUID = -4322443849222920981L;
  private CreateEnquiryExceptionInformation exceptionInformation;

  /**
   * Exception when writing an enquiry message fails with errors
   *
   * @param message Error Message
   * @param exception Exception
   * @param exceptionInformation {@link CreateEnquiryExceptionInformation}
   */
  public CreateEnquiryException(
      String message, Exception exception, CreateEnquiryExceptionInformation exceptionInformation) {
    super(message, exception);
    this.exceptionInformation = exceptionInformation;
  }

  /**
   * Exception when writing an enquiry message fails with errors
   *
   * @param message Error Message
   * @param exceptionInformation {@link CreateEnquiryExceptionInformation}
   */
  public CreateEnquiryException(
      String message, CreateEnquiryExceptionInformation exceptionInformation) {
    super(message);
    this.exceptionInformation = exceptionInformation;
  }

  /**
   * Exception when writing an enquiry message fails with errors
   *
   * @param exception Exception
   * @param exceptionInformation {@link CreateEnquiryExceptionInformation}
   */
  public CreateEnquiryException(
      Exception exception, CreateEnquiryExceptionInformation exceptionInformation) {
    super(exception);
    this.exceptionInformation = exceptionInformation;
  }
}
