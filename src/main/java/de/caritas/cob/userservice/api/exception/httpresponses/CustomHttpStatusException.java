package de.caritas.cob.userservice.api.exception.httpresponses;

import static java.util.Objects.nonNull;

import java.util.function.Consumer;
import lombok.Setter;

@Setter
public abstract class CustomHttpStatusException extends RuntimeException {

  private Consumer<Exception> loggingMethod;

  CustomHttpStatusException(String message, Consumer<Exception> loggingMethod) {
    super(message);
    this.loggingMethod = loggingMethod;
  }

  CustomHttpStatusException(String message, Exception ex, Consumer<Exception> loggingMethod) {
    super(message, ex);
    this.loggingMethod = loggingMethod;
  }

  /**
   * Executes the non null logging method.
   */
  public void executeLogging() {
    if (nonNull(this.loggingMethod)) {
      this.loggingMethod.accept(this);
    }
  }
}
