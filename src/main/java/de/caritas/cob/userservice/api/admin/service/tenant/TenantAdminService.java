package de.caritas.cob.userservice.api.admin.service.tenant;

import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.tenantadminservice.generated.ApiClient;
import de.caritas.cob.userservice.tenantadminservice.generated.web.TenantAdminControllerApi;
import de.caritas.cob.userservice.tenantadminservice.generated.web.model.TenantDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class TenantAdminService {

  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantAdminControllerApi tenantAdminControllerApi;

  public TenantDTO getTenantById() throws RestClientException {
    addDefaultHeaders(this.tenantAdminControllerApi.getApiClient());
    return this.tenantAdminControllerApi.getTenantById(
        TenantContext.getCurrentTenant());
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

}
