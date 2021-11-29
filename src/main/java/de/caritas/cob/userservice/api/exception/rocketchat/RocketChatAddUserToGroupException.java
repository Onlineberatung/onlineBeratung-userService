package de.caritas.cob.userservice.api.exception.rocketchat;

public class RocketChatAddUserToGroupException extends Exception {

  private static final long serialVersionUID = -8314892688280190524L;

  /**
   * Exception, when a Rocket.Chat API call to add a user to a group fails
   *
   * @param ex
   */
  public RocketChatAddUserToGroupException(Exception ex) {
    super(ex);
  }

  public RocketChatAddUserToGroupException(String message) {
    super(message);
  }
}
