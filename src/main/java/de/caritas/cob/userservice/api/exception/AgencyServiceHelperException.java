package de.caritas.cob.userservice.api.exception;

import de.caritas.cob.userservice.api.service.helper.AgencyServiceHelper;

/**
 * 
 * Exception for occurring errors in the {@link AgencyServiceHelper}
 *
 */
public class AgencyServiceHelperException extends Exception {
  private static final long serialVersionUID = 6092958218475107608L;

  public AgencyServiceHelperException(Exception ex) {
    super(ex);
  }
}
