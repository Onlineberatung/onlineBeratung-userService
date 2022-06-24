package de.caritas.cob.userservice.api.service.session;

import com.google.common.collect.Maps;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionTopicDTO;
import de.caritas.cob.userservice.api.service.consultingtype.TopicService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO;

@Service
@RequiredArgsConstructor
@ConditionalOnExpression("${feature.topics.enabled:true}")
@Slf4j
public class SessionTopicEnrichmentService {

  private final @NonNull TopicService topicService;

  public SessionDTO enrichSessionWithTopicData(SessionDTO session) {
    if (session.getTopic() != null && session.getTopic().getId() != null) {
      log.debug("Enriching session with topics");
      var availableTopics = getAvailableTopicsMap();
      log.debug("Enriching session with id: {} with information about the topics", session.getId());
      log.debug("Available topics list has size: {} ", availableTopics.size());
      enrichSession(availableTopics, session);
    } else {
      log.debug("Skipping topic enrichment, topic id is not set for session with id: {}",
          session.getId());
    }
    return session;
  }

  private void enrichSession(Map<Long, TopicDTO> availableTopics, SessionDTO sessionDTO) {
    var topicData = availableTopics.get(Long.valueOf(sessionDTO.getTopic().getId()));
    if (topicData != null) {
      log.debug("Enriching session with id {} with topicData {}", sessionDTO.getId(), topicData);
      sessionDTO.setTopic(convertToSessionTopicDTO(topicData));
    } else {
      log.warn("Did not find matching topic for id: {} in the available topic list",
          sessionDTO.getTopic().getId());
    }
  }

  private Map<Long, TopicDTO> getAvailableTopicsMap() {
    var allTopics = topicService.getAllTopics();
    return allTopics.isEmpty() ? Maps.newHashMap() : getAvailableTopicsMap(allTopics);
  }

  private Map<Long, TopicDTO> getAvailableTopicsMap(List<TopicDTO> allTopics) {
    return allTopics.stream()
        .collect(Collectors.toMap(TopicDTO::getId, Function.identity()));
  }

  private SessionTopicDTO convertToSessionTopicDTO(TopicDTO source) {
    SessionTopicDTO sessionTopicDTO = new SessionTopicDTO();
    sessionTopicDTO.setDescription(source.getDescription());
    sessionTopicDTO.setName(source.getName());
    sessionTopicDTO.setStatus(source.getStatus());
    return sessionTopicDTO;
  }
}
