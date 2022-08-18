package de.caritas.cob.userservice.api.exception;

public class NoUserSessionException extends Exception {

  private static final long serialVersionUID = -5465524493469707522L;

  /**
   * Exception for enquiry message, when no user session is available.
   *
   * @param message the exception message
   */
  public NoUserSessionException(String message) {
    super(message);
  }
}
