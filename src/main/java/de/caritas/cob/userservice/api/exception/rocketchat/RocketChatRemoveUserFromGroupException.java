package de.caritas.cob.userservice.api.exception.rocketchat;

public class RocketChatRemoveUserFromGroupException extends Exception {

  private static final long serialVersionUID = 2106829666296656057L;

  /**
   * Exception, when a Rocket.Chat API call to remove a user to a group fails
   *
   * @param ex
   */
  public RocketChatRemoveUserFromGroupException(Exception ex) {
    super(ex);
  }

  public RocketChatRemoveUserFromGroupException(String message) {
    super(message);
  }
}
