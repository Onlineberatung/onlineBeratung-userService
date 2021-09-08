package de.caritas.cob.userservice.api.service.statistic;

import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.statistic.event.StatisticsEvent;
import de.caritas.cob.userservice.config.RabbitMqConfig;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Service class for the processing of statistical events. */
@Service
@RequiredArgsConstructor
public class StatisticsService {

  private final @NotNull AmqpTemplate amqpTemplate;
  @Value("${statistics.enabled}")
  private boolean statisticsEnabled;

  /**
   * Entry point to write statistics event data to the statistics queue.
   *
   * @param statisticsEvent the concrete {@link StatisticsEvent}
   */
  @Async
  public void fireEvent(StatisticsEvent statisticsEvent) {

    if (statisticsEnabled) {
      statisticsEvent
          .getPayload()
          .ifPresentOrElse(
              payload ->
                  amqpTemplate.convertAndSend(
                      RabbitMqConfig.STATISTICS_EXCHANGE_NAME,
                      statisticsEvent.getEventType().toString(),
                      payload),
              () ->
                  LogService.logWarn(
                      String.format(
                          "Empty statistics event message payload for type %s received",
                          statisticsEvent.getClass().getSimpleName())));
    }
  }
}
