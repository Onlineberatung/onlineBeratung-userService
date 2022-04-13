package de.caritas.cob.userservice.api.exception.keycloak;

public class KeycloakException extends RuntimeException {

  private static final long serialVersionUID = -5083156826149548581L;

  /**
   * Keycloak 409 Conflict exception
   *
   * @param message
   */
  public KeycloakException(String message) {
    super(message);
  }

  public KeycloakException(String message, Throwable cause) {
    super(message, cause);
  }
}
