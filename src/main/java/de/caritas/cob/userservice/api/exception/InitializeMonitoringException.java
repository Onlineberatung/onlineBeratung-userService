package de.caritas.cob.userservice.api.exception;

public class InitializeMonitoringException extends RuntimeException {

  private static final long serialVersionUID = 8355368764968370207L;

  /**
   * Exception, when the initialization of the monitoring data fails
   * 
   * @param ex
   */
  public InitializeMonitoringException(Exception ex) {
    super(ex);
  }
}
