package de.caritas.cob.userservice.api.tenant;

/** Holds the tenant_id variable for ongoing thread assigned for HTTP request. */
public class TenantContext {

  public static final Long TECHNICAL_TENANT_ID = 0L;

  private TenantContext() {}

  private static final ThreadLocal<TenantData> CURRENT_TENANT_DATA = new ThreadLocal<>();

  public static Long getCurrentTenant() {
    return CURRENT_TENANT_DATA.get() != null ? CURRENT_TENANT_DATA.get().getTenantId() : null;
  }

  public static TenantData getCurrentTenantData() {
    return CURRENT_TENANT_DATA.get();
  }

  public static void setCurrentTenantData(TenantData tenantData) {
    CURRENT_TENANT_DATA.set(tenantData);
  }

  public static void setCurrentTenant(Long tenantId) {
    initializeCurrentTenantDataIfNotExist();
    CURRENT_TENANT_DATA.get().setTenantId(tenantId);
  }

  public static void setCurrentSubdomain(String subdomain) {
    initializeCurrentTenantDataIfNotExist();
    CURRENT_TENANT_DATA.get().setSubdomain(subdomain);
  }

  private static void initializeCurrentTenantDataIfNotExist() {
    if (CURRENT_TENANT_DATA.get() == null) {
      CURRENT_TENANT_DATA.set(new TenantData());
    }
  }

  public static void clear() {
    CURRENT_TENANT_DATA.remove();
  }

  public static boolean contextIsSet() {
    return getCurrentTenant() != null;
  }

  public static boolean isTechnicalOrSuperAdminContext() {
    return TECHNICAL_TENANT_ID.equals(getCurrentTenant());
  }
}
