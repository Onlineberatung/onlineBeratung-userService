package de.caritas.cob.UserService.api.exception;

import de.caritas.cob.UserService.api.container.CreateEnquiryExceptionInformation;

public class EnquiryMessageException extends CreateEnquiryException {

  private static final long serialVersionUID = 3047746831540939060L;

  /**
   * Exception for enquiry message
   * 
   * @param exception Exception
   * @param exceptionParameter {@link CreateEnquiryExceptionInformation}
   */
  public EnquiryMessageException(Exception exception,
      CreateEnquiryExceptionInformation exceptionParameter) {
    super(exception, exceptionParameter);
  }

}
