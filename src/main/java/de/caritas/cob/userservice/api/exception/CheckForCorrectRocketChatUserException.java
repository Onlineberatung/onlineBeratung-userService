package de.caritas.cob.userservice.api.exception;

public class CheckForCorrectRocketChatUserException extends RuntimeException {

  private static final long serialVersionUID = -5776764841743979032L;

  /**
   * Parallel enquiry message check exception
   * 
   * @param message
   */
  public CheckForCorrectRocketChatUserException(String message) {
    super(message);
  }
}
