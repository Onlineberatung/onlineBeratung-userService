package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.toIsoTime;

import de.caritas.cob.userservice.api.adapters.web.dto.RegistrationStatisticsListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.RegistrationStatisticsResponseDTO;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class RegistrationStatisticsResponseDTOConverter {

  private @NonNull Map<Long, TopicDTO> allTopicsMap;

  public RegistrationStatisticsListResponseDTO convert(List<Session> sessions) {
    var responseDTO = new RegistrationStatisticsListResponseDTO();
    responseDTO.registrationStatistics(convertToRegistrationStatisticsDTO(sessions));
    return responseDTO;
  }

  private List<RegistrationStatisticsResponseDTO> convertToRegistrationStatisticsDTO(
      List<Session> sessions) {
    return sessions.stream().map(this::toRegistrationStatisticsDTO).collect(Collectors.toList());
  }

  private RegistrationStatisticsResponseDTO toRegistrationStatisticsDTO(Session session) {
    return new RegistrationStatisticsResponseDTO()
        .userId(session.getUser().getUserId())
        .registrationDate(toIsoTime(session.getCreateDate()))
        .age(session.getUserAge())
        .gender(session.getUserGender())
        .postalCode(session.getPostcode())
        .mainTopicInternalAttribute(findTopicInternalIdentifier(session.getMainTopicId()))
        .topicsInternalAttributes(findTopicsInternalAttributes(session))
        .counsellingRelation(session.getCounsellingRelation());
  }

  private List<String> findTopicsInternalAttributes(Session session) {
    return session.getSessionTopics().stream()
        .map(sessionTopic -> findTopicInternalIdentifier(sessionTopic.getTopicId()))
        .collect(Collectors.toList());
  }

  private String findTopicInternalIdentifier(Integer topicId) {
    return topicId == null ? "" : findTopicInternalIdentifierInTopicsMap(topicId).orElse("");
  }

  private Optional<String> findTopicInternalIdentifierInTopicsMap(Integer topicId) {
    Long key = Long.valueOf(topicId);
    if (allTopicsMap.containsKey(key)) {
      return Optional.ofNullable(allTopicsMap.get(key).getInternalIdentifier());
    } else {
      log.warn("No topic found for a given topicId in all topics map {}", topicId);
      return Optional.empty();
    }
  }
}
