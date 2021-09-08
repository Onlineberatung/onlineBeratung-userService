package de.caritas.cob.userservice.api.service.statistic.event;

import java.util.Optional;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;

/**
 * Interface for statistics event.
 */
public interface StatisticsEvent {

  /**
   * Provides the payload for the statistics event message.
   *
   * @return the payload as {@link Optional}
   */
  Optional<String> getPayload();

  /**
   * The event type of the statistics event.
   *
   * @return the {@link EventType}
   */
  EventType getEventType();

}
