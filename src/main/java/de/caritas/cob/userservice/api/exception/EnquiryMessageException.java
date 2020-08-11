package de.caritas.cob.userservice.api.exception;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;

public class EnquiryMessageException extends CreateEnquiryException {

  private static final long serialVersionUID = 3047746831540939060L;

  /**
   * Exception for enquiry message
   * 
   * @param exception Exception
   * @param exceptionInformation {@link CreateEnquiryExceptionInformation}
   */
  public EnquiryMessageException(Exception exception,
      CreateEnquiryExceptionInformation exceptionInformation) {
    super(exception, exceptionInformation);
  }

}
