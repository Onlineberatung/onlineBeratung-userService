package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.tenantservice.generated.ApiClient;
import de.caritas.cob.userservice.tenantservice.generated.web.TenantControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TenantServiceApiClientConfig {

  @Value("${tenant.service.api.url}")
  private String tenantServiceApiUrl;

  @Bean
  public TenantControllerApi tenantControllerApi(ApiClient apiClient) {
    return new TenantControllerApi(apiClient);
  }

  @Bean
  @Primary
  public ApiClient tenantApiClient(RestTemplate restTemplate) {
    ApiClient apiClient = new TenantServiceApiClient(
        restTemplate);
    apiClient.setBasePath(this.tenantServiceApiUrl);
    return apiClient;
  }
}