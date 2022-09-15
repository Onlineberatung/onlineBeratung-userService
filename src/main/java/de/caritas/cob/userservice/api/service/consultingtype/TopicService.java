package de.caritas.cob.userservice.api.service.consultingtype;

import com.google.common.collect.Maps;
import de.caritas.cob.userservice.api.config.CacheManagerConfig;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.topicservice.generated.ApiClient;
import de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi;
import de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {

  private final @NonNull TopicControllerApi topicControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;

  @Value("${consulting.type.service.api.url}")
  private String topicServiceApiUrl;

  @Cacheable(cacheNames = CacheManagerConfig.TOPICS_CACHE)
  public List<TopicDTO> getAllTopics() {
    log.info("Calling topic service to get all topics");
    addDefaultHeaders(this.topicControllerApi.getApiClient());
    return topicControllerApi.getAllTopics();
  }

  public List<TopicDTO> getAllActiveTopics() {
    log.info("Calling topic service to get all active topics");
    // Public endpoints needs to be called without Authentication header as not to cause a 401 error
    return createTopicControllerApiWithoutHeaders().getAllActiveTopics();
  }

  public TopicControllerApi createTopicControllerApiWithoutHeaders() {
    var apiClient = new ApiClient().setBasePath(this.topicServiceApiUrl);
    TopicControllerApi topicControllerApi = new TopicControllerApi(apiClient);
    topicControllerApi.setApiClient(apiClient);
    return topicControllerApi;
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    var headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  @Cacheable(cacheNames = CacheManagerConfig.TOPICS_CACHE)
  public Map<Long, TopicDTO> getAllTopicsMap() {
    var allTopics = this.getAllTopics();
    return allTopics.isEmpty() ? Maps.newHashMap() : getAllTopicsMap(allTopics);
  }

  public Map<Long, TopicDTO> getAllActiveTopicsMap() {
    var allTopics = this.getAllActiveTopics();
    return allTopics.isEmpty() ? Maps.newHashMap() : getAllTopicsMap(allTopics);
  }

  private Map<Long, TopicDTO> getAllTopicsMap(List<TopicDTO> allTopics) {
    return allTopics.stream().collect(Collectors.toMap(TopicDTO::getId, Function.identity()));
  }

  public List<String> findTopicsInternalAttributes(Collection<Integer> topicsList) {
    return topicsList.stream().map(this::findTopicInternalIdentifier).collect(Collectors.toList());
  }

  public String findTopicInternalIdentifier(Integer topicId) {
    return topicId == null ? "" : findTopicInternalIdentifierInTopicsMap(topicId).orElse("");
  }

  private Optional<String> findTopicInternalIdentifierInTopicsMap(Integer topicId) {
    Map<Long, TopicDTO> allTopicsMap = this.getAllActiveTopicsMap();
    Long key = Long.valueOf(topicId);
    if (allTopicsMap.containsKey(key)) {
      return Optional.ofNullable(allTopicsMap.get(key).getInternalIdentifier());
    } else {
      log.warn("No topic found for a given topicId in all topics map {}", topicId);
      return Optional.empty();
    }
  }
}
