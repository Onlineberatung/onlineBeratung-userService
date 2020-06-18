package de.caritas.cob.UserService.api.exception.rocketChat;

public class RocketChatPostMessageException extends RuntimeException {

  private static final long serialVersionUID = -2247287831013110339L;

  /**
   * Exception, when a Rocket.Chat API call for message posting fails
   * 
   * @param ex
   */
  public RocketChatPostMessageException(Exception ex) {
    super(ex);
  }

}
