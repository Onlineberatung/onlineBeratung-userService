package de.caritas.cob.UserService.api.exception;

public class EnquiryMessageException extends RuntimeException {

  private static final long serialVersionUID = 3047746831540939060L;

  /**
   * Exception for enquiry message
   * 
   * @param ex
   */
  public EnquiryMessageException(Exception ex) {
    super(ex);
  }

}
