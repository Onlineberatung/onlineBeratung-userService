package de.caritas.cob.userservice.localdatetime;

import static java.util.Objects.nonNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Local date time class providing now and epoch seconds with zone offset utc.
 */
public class CustomLocalDateTime {

  private static String isoDateFormat = "uuuu-MM-dd'T'HH:mm:ssX";

  private CustomLocalDateTime() {
  }

  /**
   * Creates a current {@link LocalDateTime} instance with {@link ZoneOffset} utc.
   *
   * @return the {@link LocalDateTime} instance
   */
  public static LocalDateTime nowInUtc() {
    return LocalDateTime.now(ZoneOffset.UTC);
  }

  /**
   * Converts the given {@link LocalDateTime} to unix time.
   *
   * @param localDateTime {@link LocalDateTime}
   * @return unix time
   */
  public static long toUnixTime(LocalDateTime localDateTime) {
    return nonNull(localDateTime) ? LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
        : 0;
  }

  /**
   * Converts the given {@link LocalDateTime} to ISO 8601 string.
   *
   * @param localDateTime {@link LocalDateTime}
   * @return converted time in ISO 8601
   */
  public static String toIsoTime(LocalDateTime localDateTime) {
    return localDateTime
        .atOffset(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern(isoDateFormat));
  }
}
