package de.caritas.cob.userservice.api.service.statistics.event;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toIsoTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.helper.CustomOffsetDateTime;
import de.caritas.cob.userservice.api.helper.json.OffsetDateTimeToStringSerializer;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.RegistrationStatisticsEventMessage;
import de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistrationStatisticsEvent implements StatisticsEvent {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final EventType EVENT_TYPE = EventType.REGISTRATION;

  private static @NonNull Map<Long, TopicDTO> allTopicsMap;

  private final UserDTO registeredUser;
  private final User createdUser;
  private final Long sessionId;

  public RegistrationStatisticsEvent(UserDTO registeredUser, User createdUser, Long sessionId) {
    this.registeredUser = registeredUser;
    this.createdUser = createdUser;
    this.sessionId = sessionId;
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.registerModule(buildSimpleModule());
  }

  private static List<String> findTopicsInternalAttributes(Collection<Integer> topicsList) {
    return topicsList.stream()
        .map(RegistrationStatisticsEvent::findTopicInternalIdentifier)
        .collect(Collectors.toList());
  }

  private static String findTopicInternalIdentifier(Integer topicId) {
    return topicId == null ? "" : findTopicInternalIdentifierInTopicsMap(topicId).orElse("");
  }

  private static Optional<String> findTopicInternalIdentifierInTopicsMap(Integer topicId) {
    Long key = Long.valueOf(topicId);
    if (allTopicsMap.containsKey(key)) {
      return Optional.ofNullable(allTopicsMap.get(key).getInternalIdentifier());
    } else {
      log.warn("No topic found for a given topicId in all topics map {}", topicId);
      return Optional.empty();
    }
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
            .registrationDate(toIsoTime(createdUser.getCreateDate()))
            .age(registeredUser.getUserAge())
            .gender(registeredUser.getUserGender())
            .counsellingRelation(registeredUser.getCounsellingRelation())
            .mainTopicInternalAttribute(
                findTopicInternalIdentifier(registeredUser.getMainTopicId()))
            .topicsInternalAttributes(findTopicsInternalAttributes(registeredUser.getTopicIds()))
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
