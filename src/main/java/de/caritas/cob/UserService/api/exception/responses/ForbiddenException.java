package de.caritas.cob.UserService.api.exception.responses;

public class ForbiddenException extends RuntimeException {

  private static final long serialVersionUID = 7560597708504748234L;

  /**
   * Forbidden exception
   * 
   * @param message
   */
  public ForbiddenException(String message) {
    super(message);
  }

}
