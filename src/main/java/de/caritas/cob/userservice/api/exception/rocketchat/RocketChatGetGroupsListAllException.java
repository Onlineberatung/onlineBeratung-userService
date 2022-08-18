package de.caritas.cob.userservice.api.exception.rocketchat;

public class RocketChatGetGroupsListAllException extends Exception {

  private static final long serialVersionUID = -6467348860210122736L;

  /**
   * Exception, when a Rocket.Chat API call to get all groups fails.
   *
   * @param ex the caused exception
   */
  public RocketChatGetGroupsListAllException(Exception ex) {
    super(ex);
  }

  /**
   * Exception, when a Rocket.Chat API call to get all groups fails.
   *
   * @param message an additional message
   */
  public RocketChatGetGroupsListAllException(String message) {
    super(message);
  }

  /**
   * Exception, when a Rocket.Chat API call to get all groups fails.
   *
   * @param message an additional message
   * @param ex the caused exception
   */
  public RocketChatGetGroupsListAllException(String message, Exception ex) {
    super(message, ex);
  }
}
