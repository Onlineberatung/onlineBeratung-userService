package de.caritas.cob.userservice.api.exception.rocketChat;

public class RocketChatGetUserInfoException extends RuntimeException {

  private static final long serialVersionUID = 6187456743321504556L;

  /**
   * Exception, when a Rocket.Chat API call to get the user's info fails
   * 
   * @param message Error message
   * @param exception Exception
   */
  public RocketChatGetUserInfoException(String message, Exception exception) {
    super(message, exception);
  }

  /**
   * Exception, when a Rocket.Chat API call to get the user's info fails
   * 
   * @param message Error message
   */
  public RocketChatGetUserInfoException(String message) {
    super(message);
  }
}
