package de.caritas.cob.userservice.config.apiclient;

import de.caritas.cob.userservice.tenantservice.generated.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TenantServiceApiClientConfig {

  @Value("${tenant.service.api.url}")
  private String tenantServiceApiUrl;

  @Bean
  @Primary
  public ApiClient tenantApiClient(RestTemplate restTemplate) {
    ApiClient apiClient = new TenantServiceApiClient(
        restTemplate);
    apiClient.setBasePath(this.tenantServiceApiUrl);
    return apiClient;
  }
}