package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TopicServiceApiControllerFactory {

  @Value("${consulting.type.service.api.url}")
  private String topicServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public TopicControllerApi createControllerApi() {
    var apiClient = new TopicServiceApiClient(restTemplate).setBasePath(this.topicServiceApiUrl);
    TopicControllerApi controllerApi = new TopicControllerApi(apiClient);
    controllerApi.setApiClient(apiClient);
    return controllerApi;
  }
}
