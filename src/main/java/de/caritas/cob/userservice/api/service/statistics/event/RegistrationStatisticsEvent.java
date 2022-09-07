package de.caritas.cob.userservice.api.service.statistics.event;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toIsoTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.helper.CustomOffsetDateTime;
import de.caritas.cob.userservice.api.helper.RegistrationStatisticsHelper;
import de.caritas.cob.userservice.api.helper.json.OffsetDateTimeToStringSerializer;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.RegistrationStatisticsEventMessage;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.UserRole;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistrationStatisticsEvent implements StatisticsEvent {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final EventType EVENT_TYPE = EventType.REGISTRATION;

  private final UserDTO registeredUser;
  private final User createdUser;
  private final Long sessionId;
  private final RegistrationStatisticsHelper registrationStatisticsHelper;

  public RegistrationStatisticsEvent(
      UserDTO registeredUser,
      User createdUser,
      Long sessionId,
      RegistrationStatisticsHelper registrationStatisticsHelper) {
    this.registeredUser = registeredUser;
    this.createdUser = createdUser;
    this.sessionId = sessionId;
    this.registrationStatisticsHelper = registrationStatisticsHelper;
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
            .userId(createdUser.getUserId())
            .userRole(UserRole.ASKER)
            .registrationDate(toIsoTime(createdUser.getCreateDate()))
            .age(registeredUser.getUserAge())
            .gender(registeredUser.getUserGender())
            .counsellingRelation(registeredUser.getCounsellingRelation())
            .mainTopicInternalAttribute(
                registrationStatisticsHelper.findTopicInternalIdentifier(
                    registeredUser.getMainTopicId()))
            .topicsInternalAttributes(
                registrationStatisticsHelper.findTopicsInternalAttributes(
                    registeredUser.getTopicIds()))
            .postalCode(registeredUser.getPostcode())
            .timestamp(CustomOffsetDateTime.nowInUtc());

    try {
      return Optional.of(OBJECT_MAPPER.writeValueAsString(registrationStatisticsEventMessage));
    } catch (JsonProcessingException jsonProcessingException) {
      log.error("StatisticsEventProcessing error: ", jsonProcessingException);
    }

    return Optional.empty();
  }
}
