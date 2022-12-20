package de.caritas.cob.userservice.api.exception.httpresponses;

import de.caritas.cob.userservice.api.service.LogService;
import java.util.function.Consumer;

public class ForbiddenException extends CustomHttpStatusException {

  private static final long serialVersionUID = 7560597708504748234L;

  /**
   * Forbidden exception.
   *
   * @param message the message
   */
  public ForbiddenException(String message) {
    super(message, LogService::logForbidden);
  }

  public ForbiddenException(String message, Long arg) {
    this(String.format(message, arg));
  }

  /**
   * Forbidden exception.
   *
   * @param message an additional message
   * @param loggingMethod the method being used to log this exception
   */
  public ForbiddenException(String message, Consumer<Exception> loggingMethod) {
    super(message, loggingMethod);
  }
}
