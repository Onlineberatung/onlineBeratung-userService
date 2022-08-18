package de.caritas.cob.userservice.api.exception.httpresponses;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.service.LogService;
import java.util.function.Consumer;

public abstract class CustomHttpStatusException extends RuntimeException {

  private final Consumer<Exception> loggingMethod;

  CustomHttpStatusException() {
    super();
    this.loggingMethod = LogService::logInfo;
  }

  CustomHttpStatusException(String message, Consumer<Exception> loggingMethod) {
    super(message);
    this.loggingMethod = loggingMethod;
  }

  CustomHttpStatusException(String message, Exception ex, Consumer<Exception> loggingMethod) {
    super(message, ex);
    this.loggingMethod = loggingMethod;
  }

  /** Executes the non null logging method. */
  public void executeLogging() {
    if (nonNull(this.loggingMethod)) {
      this.loggingMethod.accept(this);
    }
  }
}
