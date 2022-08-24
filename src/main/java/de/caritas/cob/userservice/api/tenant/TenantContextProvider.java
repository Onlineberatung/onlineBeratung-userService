package de.caritas.cob.userservice.api.tenant;

import static de.caritas.cob.userservice.api.tenant.TenantResolverService.TECHNICAL_TENANT_ID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TenantContextProvider {

  @Value("${multitenancy.enabled}")
  private boolean multiTenancyEnabled;

  public void setTechnicalContextIfMultiTenancyIsEnabled() {
    if (multiTenancyEnabled) {
      TenantContext.setCurrentTenant(TECHNICAL_TENANT_ID);
    }
  }

  public void setCurrentTenantContextIfMissing(Long currentTenantId) {
    if (!TenantContext.contextIsSet()) {
      TenantContext.setCurrentTenant(currentTenantId);
    }
  }
}
