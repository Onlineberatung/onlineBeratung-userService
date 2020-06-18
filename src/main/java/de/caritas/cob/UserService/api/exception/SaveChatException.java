package de.caritas.cob.UserService.api.exception;

public class SaveChatException extends RuntimeException {

  private static final long serialVersionUID = 5563690206628141695L;

  /**
   * Exception when saving the chat to database fails
   * 
   * @param ex
   */
  public SaveChatException(String message, Exception ex) {
    super(message, ex);
  }

}
