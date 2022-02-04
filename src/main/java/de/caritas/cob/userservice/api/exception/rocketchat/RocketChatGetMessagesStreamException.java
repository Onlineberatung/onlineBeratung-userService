package de.caritas.cob.userservice.api.exception.rocketchat;

public class RocketChatGetMessagesStreamException extends Exception {

  private static final long serialVersionUID = 6719801722601287770L;

  /**
   * Exception, when a Rocket.Chat API call to get a message stream fails
   *
   * @param message an additional message
   * @param ex the caused exception
   */
  public RocketChatGetMessagesStreamException(String message, Exception ex) {
    super(message, ex);
  }
}
