package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.topicservice.generated.web.TopicControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TopicServiceApiControllerFactory {

  @Value("${tenant.service.api.url}")
  private String tenantServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public TopicControllerApi createControllerApi() {
    var apiClient = new TopicServiceApiClient(restTemplate).setBasePath(this.tenantServiceApiUrl);
    TopicControllerApi controllerApi = new TopicControllerApi(apiClient);
    controllerApi.setApiClient(apiClient);
    return controllerApi;
  }
}
