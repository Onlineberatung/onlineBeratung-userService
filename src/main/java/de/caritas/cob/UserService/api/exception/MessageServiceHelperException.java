package de.caritas.cob.UserService.api.exception;

import de.caritas.cob.UserService.api.service.helper.MessageServiceHelper;

/**
 * 
 * Exception for occurring errors in the {@link MessageServiceHelper}
 *
 */
public class MessageServiceHelperException extends RuntimeException {
  private static final long serialVersionUID = -8724761270052371308L;

  public MessageServiceHelperException(Exception ex) {
    super(ex);
  }
}
