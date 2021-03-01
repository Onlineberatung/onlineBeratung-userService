package de.caritas.cob.userservice.config.apiclient;

import de.caritas.cob.userservice.agencyadminserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.AdminAgencyControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for the AgencyAdminService API client.
 */
@Component
public class AgencyAdminServiceApiClientConfig {

  @Value("${agency.admin.service.api.url}")
  private String agencyAdminServiceApiUrl;

  /**
   * AgencyAdminService controller bean.
   *
   * @param apiClient {@link ApiClient}
   * @return the LiveService controller {@link AdminAgencyControllerApi}
   */
  @Bean
  public AdminAgencyControllerApi adminAgencyControllerApi(ApiClient apiClient) {
    return new AdminAgencyControllerApi(apiClient);
  }

  /**
   * AgencyAdminService API client bean.
   *
   * @param restTemplate {@link RestTemplate}
   * @return the AgencyAdminService {@link ApiClient}
   */
  @Bean
  @Primary
  public ApiClient adminAgencyApiClient(RestTemplate restTemplate) {
    return new ApiClient(restTemplate).setBasePath(this.agencyAdminServiceApiUrl);
  }

}
