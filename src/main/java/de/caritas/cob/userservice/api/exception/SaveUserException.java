package de.caritas.cob.userservice.api.exception;

public class SaveUserException extends Exception {

  private static final long serialVersionUID = 1069524178563839717L;

  /**
   * Exception when saving the user to database fails.
   *
   * @param message Error message
   * @param ex      Thrown exception
   */
  public SaveUserException(String message, Exception ex) {
    super(message, ex);
  }

  /**
   * Exception when saving the user to database fails.
   *
   * @param message Error message
   */
  public SaveUserException(String message) {
    super(message);
  }
}
