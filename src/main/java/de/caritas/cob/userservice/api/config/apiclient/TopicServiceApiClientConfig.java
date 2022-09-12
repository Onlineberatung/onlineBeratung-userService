package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.topicservice.generated.ApiClient;
import de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TopicServiceApiClientConfig {

  @Value("${consulting.type.service.api.url}")
  private String topicServiceApiUrl;

  @Bean
  @Qualifier("topicControllerApiPrimary")
  public TopicControllerApi topicControllerApi(ApiClient apiClient) {
    return new TopicControllerApi(apiClient);
  }

  @Bean
  @Primary
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public de.caritas.cob.userservice.topicservice.generated.ApiClient topicApiClient(
      RestTemplate restTemplate) {
    ApiClient apiClient = new TopicServiceApiClient(restTemplate);
    apiClient.setBasePath(this.topicServiceApiUrl);
    return apiClient;
  }
}
