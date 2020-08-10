package de.caritas.cob.UserService.api.exception.httpresponses;

import java.util.function.Consumer;

public abstract class CustomHttpStatusException extends RuntimeException {

  private Consumer<Exception> loggingMethod;

  CustomHttpStatusException(String message, Consumer<Exception> loggingMethod) {
    super(message);
    this.loggingMethod = loggingMethod;
  }

  /**
   * @return the method consumer to log the Exception
   */
  public Consumer<Exception> getLoggingMethod() {
    return loggingMethod;
  }
}
