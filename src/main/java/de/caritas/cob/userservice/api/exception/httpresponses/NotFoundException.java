package de.caritas.cob.userservice.api.exception.httpresponses;

import de.caritas.cob.userservice.api.service.LogService;
import java.util.function.Consumer;

public class NotFoundException extends CustomHttpStatusException {

  private static final long serialVersionUID = -4160810917274267037L;

  /**
   * Not found exception.
   *
   * @param message the message
   */
  public NotFoundException(String message) {
    super(message, LogService::logWarn);
  }

  public NotFoundException(String message, String arg) {
    super(String.format(message, arg), LogService::logWarn);
  }

  public NotFoundException(String message, Long arg) {
    super(String.format(message, arg), LogService::logWarn);
  }

  public NotFoundException(String message, String arg1, Long arg2) {
    super(String.format(message, arg1, arg2), LogService::logWarn);
  }

  public NotFoundException(String message, Long arg1, String arg2) {
    super(String.format(message, arg1, arg2), LogService::logWarn);
  }

  /**
   * Not found exception.
   *
   * @param message an additional message
   * @param loggingMethod the method being used to log this exception
   */
  public NotFoundException(String message, Consumer<Exception> loggingMethod) {
    super(message, loggingMethod);
  }
}
