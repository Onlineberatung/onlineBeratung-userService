package de.caritas.cob.UserService.api.exception.httpresponses;

import de.caritas.cob.UserService.api.service.LogService;
import java.util.function.Consumer;

public class ConflictException extends CustomHttpStatusException {

  private static final long serialVersionUID = 1L;

  /**
   * Conflict exception
   * 
   * @param message
   */
  public ConflictException(String message) {
    super(message, new LogService()::logInternalServerError);
  }

  /**
   * Conflict exception
   *
   * @param message an additional message
   * @param loggingMethod the method being used to log this exception
   */
  public ConflictException(String message, Consumer<Exception> loggingMethod) {
    super(message, loggingMethod);
  }

}
