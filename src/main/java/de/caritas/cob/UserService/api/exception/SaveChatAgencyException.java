package de.caritas.cob.UserService.api.exception;

public class SaveChatAgencyException extends RuntimeException {

  private static final long serialVersionUID = 5563690206628141695L;

  /**
   * Exception when saving the chat agency to database fails
   * 
   * @param ex
   */
  public SaveChatAgencyException(String message, Exception ex) {
    super(message, ex);
  }

}
