package de.caritas.cob.UserService.api.exception.httpresponses;

import de.caritas.cob.UserService.api.service.LogService;
import java.util.function.Consumer;

public class UnauthorizedException extends CustomHttpStatusException {
  private static final long serialVersionUID = -3553609955386498237L;

  /**
   * Unauthorized exception
   *
   * @param message
   */
  public UnauthorizedException(String message) {
    super(message, new LogService()::logUnauthorized);
  }

  /**
   * Unauthorized exception
   *
   * @param message an additional message
   * @param loggingMethod the method being used to log this exception
   */
  public UnauthorizedException(String message, Consumer<Exception> loggingMethod) {
    super(message, loggingMethod);
  }
}
