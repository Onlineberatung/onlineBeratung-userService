package de.caritas.cob.userservice.api.service.statistics.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.caritas.cob.userservice.api.helper.json.OffsetDateTimeToStringSerializer;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.StopVideoCallStatisticsEventMessage;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StopVideoCallStatisticsEvent implements StatisticsEvent {

  private static final EventType EVENT_TYPE = EventType.STOP_VIDEO_CALL;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final String userId;
  private final UUID videoCallid;

  public StopVideoCallStatisticsEvent(@NonNull String userId, @NonNull UUID videoCallId) {
    this.userId = userId;
    this.videoCallid = videoCallId;

    objectMapper.registerModule(new JavaTimeModule());
    var simpleModule = new SimpleModule();
    simpleModule.addSerializer(OffsetDateTime.class, new OffsetDateTimeToStringSerializer());
    objectMapper.registerModule(simpleModule);
  }

  /** {@inheritDoc} */
  @Override
  @SneakyThrows
  public Optional<String> getPayload() {
    var message =
        new StopVideoCallStatisticsEventMessage()
            .eventType(EVENT_TYPE)
            .userId(userId)
            .userRole(UserRole.CONSULTANT)
            .videoCallUuid(videoCallid.toString())
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC));
    var serializedMessage = objectMapper.writeValueAsString(message);

    return Optional.of(serializedMessage);
  }

  /** {@inheritDoc} */
  @Override
  public EventType getEventType() {
    return EVENT_TYPE;
  }
}
