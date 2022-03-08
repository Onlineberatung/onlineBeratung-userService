package de.caritas.cob.userservice.api.tenant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantData {
  private Long tenantId;
  private String subdomain;
}
