package de.caritas.cob.UserService.api.exception.rocketChat;

public class RocketChatGetUserInfoException extends RuntimeException {

  private static final long serialVersionUID = 6187456743321504556L;

  /**
   * Exception, when a Rocket.Chat API call to get the user's info fails
   * 
   * @param ex
   */
  public RocketChatGetUserInfoException(Exception ex) {
    super(ex);
  }

  public RocketChatGetUserInfoException(String message) {
    super(message);
  }

}
