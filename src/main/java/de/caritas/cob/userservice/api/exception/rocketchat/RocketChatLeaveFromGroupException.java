package de.caritas.cob.userservice.api.exception.rocketchat;

public class RocketChatLeaveFromGroupException extends Exception {

  private static final long serialVersionUID = 2106829666296656057L;

  /**
   * Exception, when a Rocket.Chat API call to leave a group fails
   *
   * @param ex
   */
  public RocketChatLeaveFromGroupException(String message) {
    super(message);
  }
}
