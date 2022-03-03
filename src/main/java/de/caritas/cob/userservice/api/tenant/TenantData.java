package de.caritas.cob.userservice.api.tenant;

import lombok.Data;

@Data
public class TenantData {
  private Long tenantId;
  private String subdomain;
}
