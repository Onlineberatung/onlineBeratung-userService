package de.caritas.cob.userservice.api.service.statistics.event;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toIsoTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.caritas.cob.userservice.api.helper.json.OffsetDateTimeToStringSerializer;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.ArchiveOrDeleteSessionStatisticsEventMessage;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArchiveOrDeleteSessionStatisticsEvent implements StatisticsEvent {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final EventType EVENT_TYPE = EventType.ARCHIVE_SESSION;
  private final Long sessionId;
  private final User user;

  private final LocalDateTime sessionEndDate;

  public ArchiveOrDeleteSessionStatisticsEvent(
      User user, Long sessionId, LocalDateTime sesionEndDate) {
    this.sessionId = sessionId;
    this.user = user;
    this.sessionEndDate = sesionEndDate;
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.registerModule(buildSimpleModule());
  }

  private static SimpleModule buildSimpleModule() {
    return new SimpleModule()
        .addSerializer(OffsetDateTime.class, new OffsetDateTimeToStringSerializer());
  }

  /** {@inheritDoc} */
  public EventType getEventType() {
    return EVENT_TYPE;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> getPayload() {
    var registrationStatisticsEventMessage =
        new ArchiveOrDeleteSessionStatisticsEventMessage()
            .eventType(EVENT_TYPE)
            .sessionId(sessionId)
            .tenantId(this.user.getTenantId())
            .userId(user.getUserId())
            .userRole(UserRole.ASKER)
            .endDate(toIsoTime(sessionEndDate))
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC));

    try {
      return Optional.of(OBJECT_MAPPER.writeValueAsString(registrationStatisticsEventMessage));
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("StatisticsEventProcessing error: ", jsonProcessingException);
    }

    return Optional.empty();
  }
}
