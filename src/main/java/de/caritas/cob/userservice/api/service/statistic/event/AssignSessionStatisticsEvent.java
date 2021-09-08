package de.caritas.cob.userservice.api.service.statistic.event;

import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.helper.Serializer;
import de.caritas.cob.userservice.localdatetime.CustomLocalDateTime;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.AssignSessionStatisticEventMessage;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssignSessionStatisticsEvent implements StatisticsEvent {

  private @NonNull String consultantId;
  private static final EventType eventType = EventType.ASSIGN_SESSION;

  /**
   * {@inheritDoc}
   */
  @Override
  public EventType getEventType() {
    return eventType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getPayload() {
    return Serializer.serialize(createAssignSessionStatisticsEventMessage(), LogService::logStatisticsEventError);
  }

  private AssignSessionStatisticEventMessage createAssignSessionStatisticsEventMessage() {
    return new AssignSessionStatisticEventMessage()
                .eventType(eventType)
                .consultantId(this.consultantId)
                .timestamp(CustomLocalDateTime.nowAsFullQualifiedTimestamp());
  }

}
