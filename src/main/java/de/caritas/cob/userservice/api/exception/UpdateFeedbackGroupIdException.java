package de.caritas.cob.userservice.api.exception;

public class UpdateFeedbackGroupIdException extends Exception {

  private static final long serialVersionUID = -6538582440490471213L;

  /**
   * Exception when the update of the feedback group id of a session fails.
   *
   * @param message   an additional message
   * @param exception the caused exception
   */
  public UpdateFeedbackGroupIdException(String message, Exception exception) {
    super(message, exception);
  }

}
