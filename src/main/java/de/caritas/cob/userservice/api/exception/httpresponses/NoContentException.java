package de.caritas.cob.userservice.api.exception.httpresponses;

import de.caritas.cob.userservice.api.service.LogService;

public class NoContentException extends CustomHttpStatusException {

  private static final long serialVersionUID = -4160810917274267137L;

  /**
   * No content exception.
   *
   * @param message the message
   */
  public NoContentException(String message) {
    super(message, LogService::logWarn);
  }
}
