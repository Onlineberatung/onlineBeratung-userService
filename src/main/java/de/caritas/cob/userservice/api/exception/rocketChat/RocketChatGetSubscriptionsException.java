package de.caritas.cob.userservice.api.exception.rocketChat;

public class RocketChatGetSubscriptionsException extends RuntimeException {

  private static final long serialVersionUID = -6467348860210122736L;

  /**
   * Exception, when a Rocket.Chat API call to get rooms fails
   * 
   * @param ex
   */
  public RocketChatGetSubscriptionsException(Exception ex) {
    super(ex);
  }

  public RocketChatGetSubscriptionsException(String message) {
    super(message);
  }

}
