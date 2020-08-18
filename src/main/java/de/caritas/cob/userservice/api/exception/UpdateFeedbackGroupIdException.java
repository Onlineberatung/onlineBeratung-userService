package de.caritas.cob.userservice.api.exception;

public class UpdateFeedbackGroupIdException extends RuntimeException {
  private static final long serialVersionUID = -6538582440490471213L;

  /**
   * Exception when the update of the feedback group id of a session fails
   * 
   * @param exception
   */
  public UpdateFeedbackGroupIdException(Exception exception) {
    super(exception);
  }

}
