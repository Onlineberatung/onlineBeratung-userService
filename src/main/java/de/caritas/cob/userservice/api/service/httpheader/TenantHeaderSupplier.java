package de.caritas.cob.userservice.api.service.httpheader;

import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantHeaderSupplier {

  @NonNull HttpHeadersResolver httpHeadersResolver;

  @Value("${multitenancy.enabled}")
  private boolean multitenancy;

  public void addTenantHeader(HttpHeaders headers) {
    if (multitenancy) {
      if (TenantContext.getCurrentTenant() != null) {
        headers.add("tenantId", TenantContext.getCurrentTenant().toString());
      } else {
        log.warn(
            "Not setting tenantId header, because tenant context was not set. It's okay only for non-auth user context.'");
      }
    }
  }

  public Optional<Long> getTenantFromHeader() {
    return httpHeadersResolver.findHeaderValue("tenantId");
  }
}
