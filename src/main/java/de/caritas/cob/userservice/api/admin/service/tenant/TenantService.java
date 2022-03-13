package de.caritas.cob.userservice.api.admin.service.tenant;

import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.tenantservice.generated.ApiClient;
import de.caritas.cob.userservice.tenantservice.generated.web.TenantControllerApi;
import de.caritas.cob.userservice.tenantservice.generated.web.model.RestrictedTenantDTO;
import de.caritas.cob.userservice.tenantservice.generated.web.model.TenantDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class TenantService {

  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantControllerApi tenantControllerApi;

  public TenantDTO getTenantById() throws RestClientException {
    addDefaultHeaders(this.tenantControllerApi.getApiClient());
    return this.tenantControllerApi.getTenantById(
        TenantContext.getCurrentTenant());
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  public RestrictedTenantDTO getRestrictedTenantDataBySubdomain(String subdomain) {
    return tenantControllerApi.getRestrictedTenantDataBySubdomainWithHttpInfo(subdomain).getBody();
  }

}
