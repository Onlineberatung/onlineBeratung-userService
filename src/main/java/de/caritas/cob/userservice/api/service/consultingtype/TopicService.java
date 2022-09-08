package de.caritas.cob.userservice.api.service.consultingtype;

import com.google.common.collect.Maps;
import de.caritas.cob.userservice.api.config.CacheManagerConfig;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.topicservice.generated.ApiClient;
import de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi;
import de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {

  private final @NonNull TopicControllerApi topicControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;

  @Cacheable(cacheNames = CacheManagerConfig.TOPICS_CACHE)
  public List<TopicDTO> getAllTopics() {
    log.info("Calling topic service to get all topics");
    addDefaultHeaders(this.topicControllerApi.getApiClient());
    return topicControllerApi.getAllTopics();
  }

  @Cacheable(cacheNames = CacheManagerConfig.TOPICS_CACHE)
  public List<TopicDTO> getAllActiveTopics() {
    log.info("Calling topic service to get all active topics");
    addDefaultHeaders(this.topicControllerApi.getApiClient());
    return topicControllerApi.getAllActiveTopics();
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

  @Cacheable(cacheNames = CacheManagerConfig.TOPICS_CACHE)
  public Map<Long, TopicDTO> getAllActiveTopicsMap() {
    var allTopics = this.getAllActiveTopics();
    return allTopics.isEmpty() ? Maps.newHashMap() : getAllTopicsMap(allTopics);
  }

  private Map<Long, TopicDTO> getAllTopicsMap(List<TopicDTO> allTopics) {
    return allTopics.stream().collect(Collectors.toMap(TopicDTO::getId, Function.identity()));
  }
}
