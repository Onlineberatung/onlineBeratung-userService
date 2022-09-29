package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.AdminAgencyControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AgencyAdminServiceApiClientFactory {

  @Value("${agency.admin.service.api.url}")
  private String agencyAdminServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public AdminAgencyControllerApi createControllerApi() {
    var apiClient =
        new de.caritas.cob.userservice.agencyadminserivce.generated.ApiClient(restTemplate)
            .setBasePath(this.agencyAdminServiceApiUrl);
    AdminAgencyControllerApi adminAgencyControllerApi = new AdminAgencyControllerApi(apiClient);
    adminAgencyControllerApi.setApiClient(apiClient);
    return adminAgencyControllerApi;
  }
}
