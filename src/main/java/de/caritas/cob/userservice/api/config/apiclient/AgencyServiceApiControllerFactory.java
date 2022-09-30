package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AgencyServiceApiControllerFactory {

  @Value("${agency.service.api.url}")
  private String agencyServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public AgencyControllerApi createControllerApi() {
    var apiClient = new ApiClient(restTemplate).setBasePath(this.agencyServiceApiUrl);
    return new AgencyControllerApi(apiClient);
  }
}
