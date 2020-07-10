package de.caritas.cob.UserService.api.exception;

import de.caritas.cob.UserService.api.container.CreateEnquiryExceptionParameter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEnquiryException extends Exception {

  private static final long serialVersionUID = -4322443849222920981L;
  private CreateEnquiryExceptionParameter exceptionParameter;

  /**
   * CreateEnquiryException constructor
   * 
   * @param message Error Message
   * @param exception Exception
   * @param exceptionParameter {@link CreateEnquiryExceptionParameter}
   */
  public CreateEnquiryException(String message, Exception exception,
      CreateEnquiryExceptionParameter exceptionParameter) {
    super(message, exception);
    this.exceptionParameter = exceptionParameter;
  }

  /**
   * CreateEnquiryException constructor
   * 
   * @param message Error Message
   * @param exceptionParameter {@link CreateEnquiryExceptionParameter}
   */
  public CreateEnquiryException(String message,
      CreateEnquiryExceptionParameter exceptionParameter) {
    super(message);
    this.exceptionParameter = exceptionParameter;
  }

  /**
   * CreateEnquiryException constructor
   * 
   * @param exception Exception
   * @param exceptionParameter {@link CreateEnquiryExceptionParameter}
   */
  public CreateEnquiryException(Exception exception,
      CreateEnquiryExceptionParameter exceptionParameter) {
    super(exception);
    this.exceptionParameter = exceptionParameter;
  }
}
