package de.caritas.cob.UserService.api.exception;

public class MessageHasAlreadyBeenSavedException extends RuntimeException {

  private static final long serialVersionUID = 3067609195162892096L;

  /**
   * Enquiry message conflict exception
   * 
   * @param message
   */
  public MessageHasAlreadyBeenSavedException(String message) {
    super(message);
  }

  /**
   * Enquiry message conflict exception
   */
  public MessageHasAlreadyBeenSavedException() {
    super();
  }

}
