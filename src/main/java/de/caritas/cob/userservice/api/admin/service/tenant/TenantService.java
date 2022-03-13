package de.caritas.cob.userservice.api.admin.service.tenant;

import de.caritas.cob.userservice.tenantservice.generated.web.TenantControllerApi;
import de.caritas.cob.userservice.tenantservice.generated.web.model.RestrictedTenantDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantService {

  private final @NonNull TenantControllerApi tenantControllerApi;

  public RestrictedTenantDTO getRestrictedTenantDataBySubdomain(String subdomain) {
    return tenantControllerApi.getRestrictedTenantDataBySubdomainWithHttpInfo(subdomain).getBody();
  }

}
