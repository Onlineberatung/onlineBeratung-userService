package de.caritas.cob.userservice.api.exception.rocketChat;

public class RocketChatCleanHistoryException extends RuntimeException {

  private static final long serialVersionUID = 7966120004575237483L;

  /**
   * Exception, when a Rocket.Chat API call to clean a group's history fails
   * 
   * @param ex
   */
  public RocketChatCleanHistoryException(Exception ex) {
    super(ex);
  }

  public RocketChatCleanHistoryException(String message) {
    super(message);
  }

}
