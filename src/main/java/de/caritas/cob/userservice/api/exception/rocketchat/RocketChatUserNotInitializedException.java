package de.caritas.cob.userservice.api.exception.rocketchat;

public class RocketChatUserNotInitializedException extends Exception {

  private static final long serialVersionUID = -6444815503348502528L;

  public RocketChatUserNotInitializedException(String message) {
    super(message);
  }
}
