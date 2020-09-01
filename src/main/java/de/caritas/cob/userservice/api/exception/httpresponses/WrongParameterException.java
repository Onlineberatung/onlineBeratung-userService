package de.caritas.cob.userservice.api.exception.httpresponses;

public class WrongParameterException extends Exception {

  private static final long serialVersionUID = -8401912145454084348L;

  /**
   * Bad request exception.
   * 
   * @param message an additional message
   */
  public WrongParameterException(String message) {
    super(message);
  }

  /**
   * ClassCastException: Bad request exception.
   * 
   * @param message an additional message
   */
  public WrongParameterException(String message, ClassCastException exception) {
    super(message, exception);
  }

}
