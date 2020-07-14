package de.caritas.cob.UserService.api.exception.rocketChat;

public class RocketChatUserNotInitializedException extends RuntimeException {
  private static final long serialVersionUID = -6444815503348502528L;

  public RocketChatUserNotInitializedException(String message) {
    super(message);
  }

}
