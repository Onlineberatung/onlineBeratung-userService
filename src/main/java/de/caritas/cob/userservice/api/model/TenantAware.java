package de.caritas.cob.userservice.api.model;

/** Marker interface for entities that need to support tenant feature. */
public interface TenantAware {

  /**
   * Sets the fields tenantId during save operations.
   *
   * @param tenantId
   */
  void setTenantId(Long tenantId);

  Long getTenantId();
}
