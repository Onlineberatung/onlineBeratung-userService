package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.messageservice.generated.ApiClient;
import de.caritas.cob.userservice.messageservice.generated.web.MessageControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MessageServiceApiControllerFactory {

  @Value("${message.service.api.url}")
  private String messageServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public MessageControllerApi createControllerApi() {
    var apiClient = new ApiClient(restTemplate).setBasePath(this.messageServiceApiUrl);
    return new MessageControllerApi(apiClient);
  }
}
