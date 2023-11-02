package de.caritas.cob.userservice.api.admin.service.tenant;

import de.caritas.cob.userservice.api.config.CacheManagerConfig;
import de.caritas.cob.userservice.api.config.apiclient.TenantServiceApiControllerFactory;
import de.caritas.cob.userservice.tenantservice.generated.web.model.RestrictedTenantDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

  private final @NonNull TenantServiceApiControllerFactory tenantServiceApiControllerFactory;

  @Cacheable(cacheNames = CacheManagerConfig.TENANT_CACHE, key = "#subdomain")
  public RestrictedTenantDTO getRestrictedTenantData(String subdomain) {
    log.info("Calling tenant service to get tenant data for subdomain {}", subdomain);
    return tenantServiceApiControllerFactory
        .createControllerApi()
        .getRestrictedTenantDataBySubdomain(subdomain);
  }

  @Cacheable(cacheNames = CacheManagerConfig.TENANT_CACHE, key = "#tenantId")
  public RestrictedTenantDTO getRestrictedTenantData(Long tenantId) {
    log.info("Calling tenant service to get tenant data for tenantId {}", tenantId);

    return tenantServiceApiControllerFactory
        .createControllerApi()
        .getRestrictedTenantDataByTenantId(tenantId);
  }
}
