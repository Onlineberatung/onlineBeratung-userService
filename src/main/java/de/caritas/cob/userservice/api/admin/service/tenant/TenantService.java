package de.caritas.cob.userservice.api.admin.service.tenant;

import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.tenantservice.generated.ApiClient;
import de.caritas.cob.userservice.tenantservice.generated.web.TenantControllerApi;
import de.caritas.cob.userservice.tenantservice.generated.web.model.TenantDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class TenantService {

  private final @NonNull TenantControllerApi apiClient;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;

  public TenantDTO getTenantById() throws RestClientException {
    addDefaultHeaders(this.apiClient.getApiClient());
    return this.apiClient.getTenantById(
        TenantContext.getCurrentTenant());
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

}
