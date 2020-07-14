package de.caritas.cob.UserService.api.exception.httpresponses;

public class BadRequestException extends RuntimeException {
  private static final long serialVersionUID = -3553609955386498237L;

  /**
   * BadRequest exception
   * 
   * @param message
   */
  public BadRequestException(String message) {
    super(message);
  }

}
