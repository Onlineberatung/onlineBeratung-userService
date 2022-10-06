package de.caritas.cob.userservice.api.config.apiclient;

import de.caritas.cob.userservice.tenantadminservice.generated.web.TenantAdminControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TenantAdminServiceApiControllerFactory {

  @Value("${tenant.service.api.url}")
  private String tenantServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public TenantAdminControllerApi createControllerApi() {
    var apiClient =
        new TenantAdminServiceApiClient(restTemplate).setBasePath(this.tenantServiceApiUrl);
    return new TenantAdminControllerApi(apiClient);
  }
}
