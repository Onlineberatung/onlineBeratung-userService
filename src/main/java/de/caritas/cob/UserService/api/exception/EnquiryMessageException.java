package de.caritas.cob.UserService.api.exception;

import de.caritas.cob.UserService.api.container.CreateEnquiryExceptionParameter;

public class EnquiryMessageException extends CreateEnquiryException {

  private static final long serialVersionUID = 3047746831540939060L;

  /**
   * Exception for enquiry message
   * 
   * @param exception Exception
   * @param exceptionParameter {@link CreateEnquiryExceptionParameter}
   */
  public EnquiryMessageException(Exception exception,
      CreateEnquiryExceptionParameter exceptionParameter) {
    super(exception, exceptionParameter);
  }

}
