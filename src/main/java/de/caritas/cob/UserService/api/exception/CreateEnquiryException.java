package de.caritas.cob.UserService.api.exception;

import de.caritas.cob.UserService.api.container.CreateEnquiryExceptionInformation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEnquiryException extends Exception {

  private static final long serialVersionUID = -4322443849222920981L;
  private CreateEnquiryExceptionInformation exceptionParameter;

  /**
   * Exception when writing an enquiry message fails with errors
   * 
   * @param message Error Message
   * @param exception Exception
   * @param exceptionParameter {@link CreateEnquiryExceptionInformation}
   */
  public CreateEnquiryException(String message, Exception exception,
      CreateEnquiryExceptionInformation exceptionParameter) {
    super(message, exception);
    this.exceptionParameter = exceptionParameter;
  }

  /**
   * Exception when writing an enquiry message fails with errors
   * 
   * @param message Error Message
   * @param exceptionParameter {@link CreateEnquiryExceptionInformation}
   */
  public CreateEnquiryException(String message,
      CreateEnquiryExceptionInformation exceptionParameter) {
    super(message);
    this.exceptionParameter = exceptionParameter;
  }

  /**
   * Exception when writing an enquiry message fails with errors
   * 
   * @param exception Exception
   * @param exceptionParameter {@link CreateEnquiryExceptionInformation}
   */
  public CreateEnquiryException(Exception exception,
      CreateEnquiryExceptionInformation exceptionParameter) {
    super(exception);
    this.exceptionParameter = exceptionParameter;
  }
}
