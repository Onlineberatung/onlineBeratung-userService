package de.caritas.cob.userservice.api.service.statistics.event;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toIsoTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.helper.json.OffsetDateTimeToStringSerializer;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.RegistrationStatisticsEventMessage;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistrationStatisticsEvent implements StatisticsEvent {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final EventType EVENT_TYPE = EventType.REGISTRATION;

  private final UserDTO registeredUser;
  private final User createdUser;
  private final Long sessionId;
  private final String mainTopicInternalAttribute;
  private final List<String> topicsInternalAttributes;

  private final String tenantName;

  private final String agencyName;

  public RegistrationStatisticsEvent(
      UserDTO registeredUser,
      User createdUser,
      Long sessionId,
      String mainTopicInternalAttribute,
      List<String> topicsInternalAttributes,
      String tenantName,
      String agencyName) {
    this.registeredUser = registeredUser;
    this.createdUser = createdUser;
    this.sessionId = sessionId;
    this.mainTopicInternalAttribute = mainTopicInternalAttribute;
    this.topicsInternalAttributes = topicsInternalAttributes;
    this.tenantName = tenantName;
    this.agencyName = agencyName;

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
        new RegistrationStatisticsEventMessage()
            .eventType(EVENT_TYPE)
            .sessionId(sessionId)
            .tenantId(createdUser.getTenantId())
            .userId(createdUser.getUserId())
            .userRole(UserRole.ASKER)
            .registrationDate(toIsoTime(createdUser.getCreateDate()))
            .age(registeredUser.getUserAge())
            .gender(registeredUser.getUserGender())
            .counsellingRelation(registeredUser.getCounsellingRelation())
            .referer(registeredUser.getReferer())
            .mainTopicInternalAttribute(mainTopicInternalAttribute)
            .topicsInternalAttributes(topicsInternalAttributes)
            .postalCode(registeredUser.getPostcode())
            .tenantName(tenantName)
            .agencyName(agencyName)
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC));

    try {
      return Optional.of(OBJECT_MAPPER.writeValueAsString(registrationStatisticsEventMessage));
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("StatisticsEventProcessing error: ", jsonProcessingException);
    }

    return Optional.empty();
  }
}
