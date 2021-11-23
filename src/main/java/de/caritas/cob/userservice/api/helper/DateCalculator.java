package de.caritas.cob.userservice.api.helper;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Calculation methods for dates.
 */
public class DateCalculator {

  private DateCalculator() {
  }

  /**
   * Calculates a {@link LocalDateTime} from now minus given days in the past. The time is
   * midnight.
   *
   * @param daysInThePast days in the past
   * @return a {@link LocalDateTime} instance
   */
  public static LocalDateTime calculateDateInThePastAtMidnight(int daysInThePast) {
    return LocalDateTime
        .now()
        .with(LocalTime.MIDNIGHT)
        .minusDays(daysInThePast);
  }

}
