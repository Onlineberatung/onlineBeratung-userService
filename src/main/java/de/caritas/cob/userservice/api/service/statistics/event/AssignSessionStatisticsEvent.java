package de.caritas.cob.userservice.api.service.statistics.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.caritas.cob.userservice.api.helper.json.OffsetDateTimeToStringSerializer;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.AssignSessionStatisticsEventMessage;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignSessionStatisticsEvent implements StatisticsEvent {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final EventType EVENT_TYPE = EventType.ASSIGN_SESSION;

  private final @NonNull String userId;
  private final @NonNull UserRole userRole;
  private final @NonNull Long sessionId;
  @Getter @Setter private String requestUri;
  @Getter @Setter private String requestReferer;
  @Getter @Setter private String requestUserId;

  public AssignSessionStatisticsEvent(
      @NonNull String userId, @NonNull UserRole userRole, @NonNull Long sessionId) {
    this.userId = userId;
    this.userRole = userRole;
    this.sessionId = sessionId;
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.registerModule(buildSimpleModule());
  }

  /** {@inheritDoc} */
  public EventType getEventType() {
    return EVENT_TYPE;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> getPayload() {
    var assignSessionStatisticsEventMessage =
        new AssignSessionStatisticsEventMessage()
            .eventType(EVENT_TYPE)
            .userId(userId)
            .userRole(userRole)
            .sessionId(sessionId)
            .requestUri(requestUri)
            .requestReferer(requestReferer)
            .requestUserId(requestUserId)
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC));

    try {
      return Optional.of(OBJECT_MAPPER.writeValueAsString(assignSessionStatisticsEventMessage));
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("StatisticsEventProcessing error: ", jsonProcessingException);
    }

    return Optional.empty();
  }

  private static SimpleModule buildSimpleModule() {
    return new SimpleModule()
        .addSerializer(OffsetDateTime.class, new OffsetDateTimeToStringSerializer());
  }
}
