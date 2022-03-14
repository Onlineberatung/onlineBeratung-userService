package de.caritas.cob.userservice.config.apiclient;

import de.caritas.cob.userservice.tenantadminservice.generated.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import de.caritas.cob.userservice.tenantadminservice.generated.web.TenantAdminControllerApi;

@Configuration
public class TenantAdminServiceApiClientConfig {

  @Value("${tenant.service.api.url}")
  private String tenantServiceApiUrl;

  @Bean
  public TenantAdminControllerApi tenantAdminControllerApi(ApiClient tenantAdminApiClient) {
    return new TenantAdminControllerApi(tenantAdminApiClient);
  }

  @Bean
  @Primary
  public ApiClient tenantAdminApiClient(RestTemplate restTemplate) {
    ApiClient apiClient = new TenantAdminServiceApiClient(
        restTemplate);
    apiClient.setBasePath(this.tenantServiceApiUrl);
    return apiClient;
  }
}