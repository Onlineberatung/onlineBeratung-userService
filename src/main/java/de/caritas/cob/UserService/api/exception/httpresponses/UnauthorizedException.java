package de.caritas.cob.UserService.api.exception.httpresponses;

public class UnauthorizedException extends RuntimeException {
  private static final long serialVersionUID = -3553609955386498237L;

  /**
   * Unauthorized exception
   * 
   * @param message
   */
  public UnauthorizedException(String message) {
    super(message);
  }
}
