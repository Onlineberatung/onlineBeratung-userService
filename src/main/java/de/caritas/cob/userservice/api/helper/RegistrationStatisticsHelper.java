package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class RegistrationStatisticsHelper {
  private @NonNull Map<Long, TopicDTO> allTopicsMap;

  public List<String> findTopicsInternalAttributes(Collection<Integer> topicsList) {
    return topicsList.stream().map(this::findTopicInternalIdentifier).collect(Collectors.toList());
  }

  public String findTopicInternalIdentifier(Integer topicId) {
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
