package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AgencyServiceApiClientFactory {

  @Value("${agency.service.api.url}")
  private String agencyServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public AgencyControllerApi createControllerApi() {
    var apiClient =
        new de.caritas.cob.userservice.agencyserivce.generated.ApiClient(restTemplate)
            .setBasePath(this.agencyServiceApiUrl);
    AgencyControllerApi adminAgencyControllerApi = new AgencyControllerApi(apiClient);
    adminAgencyControllerApi.setApiClient(apiClient);
    return adminAgencyControllerApi;
  }
}
