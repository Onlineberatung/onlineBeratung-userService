package de.caritas.cob.userservice.api.exception.rocketchat;

public class RocketChatRemoveSystemMessagesException extends Exception {

  private static final long serialVersionUID = 7966120004575237483L;

  /**
   * Exception, when a Rocket.Chat API call to remove system messages of a group fails
   *
   * @param ex
   */
  public RocketChatRemoveSystemMessagesException(Exception ex) {
    super(ex);
  }

  public RocketChatRemoveSystemMessagesException(String message) {
    super(message);
  }
}
