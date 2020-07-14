package de.caritas.cob.UserService.api.exception.httpresponses;

public class ConflictException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Conflict exception
   * 
   * @param message
   */
  public ConflictException(String message) {
    super(message);
  }

}
