package de.caritas.cob.userservice.api.tenant;

import static de.caritas.cob.userservice.api.tenant.TenantResolver.TECHNICAL_TENANT_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantContextProviderTest {

  private final TenantContextProvider tenantContextProvider = new TenantContextProvider();

  @BeforeEach
  void clearTenantContext() {
    TenantContext.clear();
  }

  @Test
  void setTechnicalContextIfMultiTenancyIsEnabled_Should_setTechnicalTenantContext_When_multiTenancyIsEnabled() {
    setField(tenantContextProvider, "multiTenancyEnabled", true);

    tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();

    assertThat(TenantContext.getCurrentTenant(), is(TECHNICAL_TENANT_ID));
  }

  @Test
  void setTechnicalContextIfMultiTenancyIsEnabled_Should_notSetTechnicalTenantContext_When_multiTenancyIsDisabled() {
    setField(tenantContextProvider, "multiTenancyEnabled", false);

    tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();

    assertNull(TenantContext.getCurrentTenant());
  }

}
