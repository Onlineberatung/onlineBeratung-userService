package de.caritas.cob.userservice.api.service.statistics.event;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toIsoTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.caritas.cob.userservice.api.helper.json.OffsetDateTimeToStringSerializer;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteAccountStatisticsEvent implements StatisticsEvent {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final EventType EVENT_TYPE = EventType.DELETE_ACCOUNT;
  private final User user;

  private final LocalDateTime accountDeletionDate;

  public DeleteAccountStatisticsEvent(User user, LocalDateTime accountDeletionDate) {
    this.user = user;
    this.accountDeletionDate = accountDeletionDate;
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
        new de.caritas.cob.userservice.statisticsservice.generated.web.model
                .DeleteAccountStatisticsEventMessage()
            .eventType(EVENT_TYPE)
            .tenantId(this.user.getTenantId())
            .userId(user.getUserId())
            .userRole(UserRole.ASKER)
            .deleteDate(toIsoTime(accountDeletionDate))
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC));

    try {
      return Optional.of(OBJECT_MAPPER.writeValueAsString(registrationStatisticsEventMessage));
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("StatisticsEventProcessing error: ", jsonProcessingException);
    }

    return Optional.empty();
  }
}
