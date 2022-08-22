package de.caritas.cob.userservice.api.exception.httpresponses;

import de.caritas.cob.userservice.api.service.LogService;
import java.util.function.Consumer;

public class ConflictException extends CustomHttpStatusException {

  private static final long serialVersionUID = 1L;

  /**
   * Conflict exception.
   *
   * @param message the message
   */
  public ConflictException(String message) {
    super(message, LogService::logInternalServerError);
  }

  /**
   * Conflict exception.
   *
   * @param message an additional message
   * @param loggingMethod the method being used to log this exception
   */
  public ConflictException(String message, Consumer<Exception> loggingMethod) {
    super(message, loggingMethod);
  }
}
