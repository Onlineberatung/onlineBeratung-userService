package de.caritas.cob.userservice.api.exception;

public class MissingConsultingTypeException extends RuntimeException {

  private static final long serialVersionUID = -6127271234647444277L;

  /**
   * Exception, when settings for an requested consulting type are missing
   *
   * @param message
   */
  public MissingConsultingTypeException(String message) {
    super(message);
  }
}
