package de.caritas.cob.userservice.api.exception.rocketChat;

public class RocketChatCreateGroupException extends RuntimeException {

  private static final long serialVersionUID = -2247287831013110339L;

  /**
   * Exception, when a Rocket.Chat API call for group creation fails
   * 
   * @param ex
   */
  public RocketChatCreateGroupException(Exception ex) {
    super(ex);
  }

  /**
   * Exception, when a Rocket.Chat API call for group creation fails
   * 
   * @param message
   */
  public RocketChatCreateGroupException(String message) {
    super(message);
  }

}
