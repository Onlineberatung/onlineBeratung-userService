package de.caritas.cob.UserService.api.exception;

public class UpdateSessionException extends RuntimeException {

  private static final long serialVersionUID = -3666710126372746391L;

  /**
   * Exception when update of session fails
   * 
   * @param exception
   */
  public UpdateSessionException(Exception exception) {
    super(exception);
  }
}
