package de.caritas.cob.userservice.api.exception.rocketChat;

public class RocketChatDeleteGroupException extends RuntimeException {

  private static final long serialVersionUID = -7858913574003162918L;

  /**
   * Exception, when a Rocket.Chat API call for group deletion fails
   * 
   * @param ex
   */
  public RocketChatDeleteGroupException(Exception ex) {
    super(ex);
  }

}
