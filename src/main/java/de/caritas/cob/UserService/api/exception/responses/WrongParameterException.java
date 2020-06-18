package de.caritas.cob.UserService.api.exception.responses;

public class WrongParameterException extends RuntimeException {

  private static final long serialVersionUID = -8401912145454084348L;

  /**
   * Bad request exception
   * 
   * @param message
   */
  public WrongParameterException(String message) {
    super(message);
  }

  /**
   * ClassCastException: Bad request exception
   * 
   * @param message
   */
  public WrongParameterException(String message, ClassCastException exception) {
    super(message, exception);
  }

}
