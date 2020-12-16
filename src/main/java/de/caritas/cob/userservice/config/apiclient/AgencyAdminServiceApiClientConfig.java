package de.caritas.cob.userservice.config.apiclient;

import de.caritas.cob.userservice.agencyadminserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.AdminAgencyControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AgencyAdminServiceApiClientConfig {

  @Value("${agency.admin.service.api.url}")
  private String agencyAdminServiceApiUrl;

  @Bean
  public AdminAgencyControllerApi adminAgencyControllerApi(ApiClient apiClient) {
    return new AdminAgencyControllerApi(apiClient);
  }

  @Bean
  @Primary
  public ApiClient adminAgencyApiClient(RestTemplate restTemplate) {
    return new ApiClient(restTemplate).setBasePath(this.agencyAdminServiceApiUrl);
  }

}
