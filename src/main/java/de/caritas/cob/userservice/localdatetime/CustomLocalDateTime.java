package de.caritas.cob.userservice.localdatetime;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Local date time class providing now with zone offset utc.
 */
public class CustomLocalDateTime {

  private CustomLocalDateTime() {
  }

  /**
   * Creates a current {@link LocalDateTime} instance with {@link ZoneOffset} utc.
   *
   * @return the {@link LocalDateTime} instance
   */
  public static LocalDateTime nowInUtc() {
    LocalDateTime localDateTime = LocalDateTime.MAX;
    return LocalDateTime.now(ZoneOffset.UTC);
  }

}
