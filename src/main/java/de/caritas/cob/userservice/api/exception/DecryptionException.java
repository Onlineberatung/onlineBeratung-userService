package de.caritas.cob.userservice.api.exception;

public class DecryptionException extends RuntimeException {

  public DecryptionException(String message) {
    super(message);
  }

  public DecryptionException(String message, Throwable exception) {
    super(message, exception);
  }
}
