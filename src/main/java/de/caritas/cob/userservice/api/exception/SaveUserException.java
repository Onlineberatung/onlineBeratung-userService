package de.caritas.cob.userservice.api.exception;

public class SaveUserException extends RuntimeException {

  private static final long serialVersionUID = 1069524178563839717L;

  /**
   * Exception when saving the user to database fails
   * 
   * @param ex
   */
  public SaveUserException(String message, Exception ex) {
    super(message, ex);
  }
}
