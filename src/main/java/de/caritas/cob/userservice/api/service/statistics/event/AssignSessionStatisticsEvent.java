package de.caritas.cob.userservice.api.service.statistics.event;

import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.helper.Serializer;
import de.caritas.cob.userservice.localdatetime.CustomLocalDateTime;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.AssignSessionStatisticsEventMessage;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssignSessionStatisticsEvent implements StatisticsEvent {

  private static final EventType eventType = EventType.ASSIGN_SESSION;
  private final String timestamp = CustomLocalDateTime.nowAsFullQualifiedTimestamp();

  private @NonNull String consultantId;
  private @NonNull Long sessionId;

  /** {@inheritDoc} */
  @Override
  public EventType getEventType() {
    return eventType;
  }

  /** {@inheritDoc} */
  @Override
  public String getTimestamp() {
    return this.timestamp;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> getPayload() {
    return Serializer.serialize(
        createAssignSessionStatisticsEventMessage(), LogService::logStatisticsEventError);
  }

  private AssignSessionStatisticsEventMessage createAssignSessionStatisticsEventMessage() {
    return new AssignSessionStatisticsEventMessage()
        .eventType(eventType)
        .consultantId(this.consultantId)
        .sessionId(this.sessionId)
        .timestamp(timestamp);
  }
}
