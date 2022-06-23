package de.caritas.cob.userservice.api.service.consultingtype;

import de.caritas.cob.userservice.api.config.CacheManagerConfig;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.topicservice.generated.ApiClient;

@Service
@RequiredArgsConstructor
public class TopicService {

  private final @NonNull de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi topicControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;

  @Cacheable(cacheNames = CacheManagerConfig.TOPICS_CACHE)
  public List<de.caritas.cob.userservice.topicservice.generated.web.model.TopicDTO> getAllTopics() {
    addDefaultHeaders(this.topicControllerApi.getApiClient());
    return topicControllerApi.getAllTopics();
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    var headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

}
