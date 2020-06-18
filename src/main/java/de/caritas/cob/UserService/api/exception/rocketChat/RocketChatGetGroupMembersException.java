package de.caritas.cob.UserService.api.exception.rocketChat;

public class RocketChatGetGroupMembersException extends RuntimeException {

  private static final long serialVersionUID = -6467348860210122736L;

  /**
   * Exception, when a Rocket.Chat API call to get group m embers fails
   * 
   * @param ex
   */
  public RocketChatGetGroupMembersException(Exception ex) {
    super(ex);
  }

  public RocketChatGetGroupMembersException(String message) {
    super(message);
  }

}
