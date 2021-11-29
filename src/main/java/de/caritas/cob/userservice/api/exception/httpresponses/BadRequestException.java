package de.caritas.cob.userservice.api.exception.httpresponses;

import de.caritas.cob.userservice.api.service.LogService;
import java.util.function.Consumer;

public class BadRequestException extends CustomHttpStatusException {

  private static final long serialVersionUID = -3553609955386498237L;

  /**
   * BadRequest exception.
   *
   * @param message       an additional message
   * @param loggingMethod the method being used to log this exception
   */
  public BadRequestException(String message, Consumer<Exception> loggingMethod) {
    super(message, loggingMethod);
  }

  /**
   * BadRequest exception.
   *
   * @param message an additional message
   */
  public BadRequestException(String message) {
    super(message, LogService::logWarn);
  }

}
