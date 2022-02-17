package de.caritas.cob.userservice.api.tenant;

/**
 * Holds the tenant_id variable for ongoing thread assigned for HTTP request.
 */
public class TenantContext {

  private TenantContext() {

  }

  private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();


  public static Long getCurrentTenant() {
    return CURRENT_TENANT.get();
  }

  public static void setCurrentTenant(Long tenant) {
    CURRENT_TENANT.set(tenant);
  }

  public static void clear() {
    CURRENT_TENANT.remove();
  }
}
