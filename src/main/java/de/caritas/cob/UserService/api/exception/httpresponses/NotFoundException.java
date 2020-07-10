package de.caritas.cob.UserService.api.exception.httpresponses;

public class NotFoundException extends RuntimeException {

  private static final long serialVersionUID = -4160810917274267037L;

  /**
   * Not found exception
   * 
   * @param message
   */
  public NotFoundException(String message) {
    super(message);
  }

}
