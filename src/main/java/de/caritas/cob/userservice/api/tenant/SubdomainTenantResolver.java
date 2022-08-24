package de.caritas.cob.userservice.api.tenant;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import de.caritas.cob.userservice.api.adapters.web.controller.interceptor.SubdomainExtractor;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class SubdomainTenantResolver implements TenantResolver {

  private final @NonNull SubdomainExtractor subdomainExtractor;

  private final @NonNull TenantService tenantService;

  @Override
  public Optional<Long> resolve(HttpServletRequest request) {
    return resolveTenantFromSubdomain();
  }

  private Optional<Long> resolveTenantFromSubdomain() {
    Optional<String> currentSubdomain = subdomainExtractor.getCurrentSubdomain();
    if (currentSubdomain.isPresent()) {
      return of(getTenantIdBySubdomain(currentSubdomain.get()));
    } else {
      return empty();
    }
  }

  private Long getTenantIdBySubdomain(String currentSubdomain) {
    return tenantService.getRestrictedTenantData(currentSubdomain).getId();
  }

  @Override
  public boolean canResolve(HttpServletRequest request) {
    return resolve(request).isPresent();
  }
}
