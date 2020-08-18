package de.caritas.cob.userservice.api.exception.rocketChat;

public class RocketChatGroupCountersException extends RuntimeException {

  private static final long serialVersionUID = -686228997546928427L;

  /**
   * Exception, when a Rocket.Chat API call to get group counters fails
   * 
   * @param ex
   */
  public RocketChatGroupCountersException(Exception ex) {
    super(ex);
  }

  public RocketChatGroupCountersException(String message) {
    super(message);
  }
}
