package de.caritas.cob.userservice.api.exception.rocketchat;

public class RocketChatGetUserIdException extends Exception {

  private static final long serialVersionUID = -6467348860210122736L;

  /**
   * Exception, when a Rocket.Chat API call to get all groups fails.
   *
   * @param ex the caused exception
   */
  public RocketChatGetUserIdException(Exception ex) {
    super(ex);
  }

  /**
   * Exception, when a Rocket.Chat API call to get all groups fails.
   *
   * @param message an additional message
   */
  public RocketChatGetUserIdException(String message) {
    super(message);
  }

  /**
   * Exception, when a Rocket.Chat API call to get all groups fails.
   *
   * @param message an additional message
   * @param ex the caused exception
   */
  public RocketChatGetUserIdException(String message, Exception ex) {
    super(message, ex);
  }
}
