package de.caritas.cob.UserService.api.exception;

import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;

/**
 * 
 * Exception for occurring errors in the {@link AgencyServiceHelper}
 *
 */
public class AgencyServiceHelperException extends RuntimeException {
  private static final long serialVersionUID = 6092958218475107608L;

  public AgencyServiceHelperException(Exception ex) {
    super(ex);
  }
}
