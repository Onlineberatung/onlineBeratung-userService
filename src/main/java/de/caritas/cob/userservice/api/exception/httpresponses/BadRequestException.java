package de.caritas.cob.userservice.api.exception.httpresponses;

public class BadRequestException extends RuntimeException {

  private static final long serialVersionUID = -3553609955386498237L;

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(String message, Throwable exception) {
    super(message, exception);
  }
}
