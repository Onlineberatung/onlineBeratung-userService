package de.caritas.cob.userservice.api.exception.rocketChat;

public class RocketChatApiException extends RuntimeException {

  private static final long serialVersionUID = -1140277378757000304L;

  /**
   * Exception when a Rocket.Chat API call (via RestTemplate) fails
   * 
   * @param message
   */
  public RocketChatApiException(String message) {
    super(message);
  }
}
