package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.liveservice.generated.ApiClient;
import de.caritas.cob.userservice.liveservice.generated.web.LiveControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LiveServiceApiControllerFactory {

  @Value("${live.service.api.url}")
  private String liveServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public LiveControllerApi createControllerApi() {
    var apiClient = new ApiClient(restTemplate).setBasePath(this.liveServiceApiUrl);
    return new LiveControllerApi(apiClient);
  }
}
