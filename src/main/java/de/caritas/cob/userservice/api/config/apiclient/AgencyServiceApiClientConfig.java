package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** Configuration class for the AgencyAdminService API client. */
@Component
public class AgencyServiceApiClientConfig {

  @Value("${agency.service.api.url}")
  private String agencyServiceApiUrl;

  /**
   * AgencyService controller bean.
   *
   * @param apiClient {@link ApiClient}
   * @return the AgencyService controller {@link AgencyControllerApi}
   */
  @Bean
  public AgencyControllerApi agencyControllerApi(ApiClient apiClient) {
    return new AgencyControllerApi(apiClient);
  }

  /**
   * AgencyService API client bean.
   *
   * @param restTemplate {@link RestTemplate}
   * @return the AgencyService {@link ApiClient}
   */
  @Bean
  @Primary
  @Scope("prototype")
  public ApiClient agencyApiClient(RestTemplate restTemplate) {
    return new ApiClient(restTemplate).setBasePath(this.agencyServiceApiUrl);
  }
}
