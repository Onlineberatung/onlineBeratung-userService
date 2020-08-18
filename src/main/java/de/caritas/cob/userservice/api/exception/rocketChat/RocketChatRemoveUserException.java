package de.caritas.cob.userservice.api.exception.rocketChat;

public class RocketChatRemoveUserException extends RuntimeException {

  private static final long serialVersionUID = 7966120004575237483L;

  /**
   * Exception, when a Rocket.Chat API call to remove an user fails
   * 
   * @param ex
   */
  public RocketChatRemoveUserException(Exception ex) {
    super(ex);
  }

  public RocketChatRemoveUserException(String message) {
    super(message);
  }
}
