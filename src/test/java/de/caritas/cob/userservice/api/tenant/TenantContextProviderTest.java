package de.caritas.cob.userservice.api.tenant;

import static de.caritas.cob.userservice.api.tenant.TenantResolverService.TECHNICAL_TENANT_ID;
import static org.assertj.core.api.Assertions.assertThat;
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
  void
      setTechnicalContextIfMultiTenancyIsEnabled_Should_setTechnicalTenantContext_When_multiTenancyIsEnabled() {
    // given
    setField(tenantContextProvider, "multiTenancyEnabled", true);

    // when
    tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();

    // then
    assertThat(TenantContext.getCurrentTenant()).isEqualTo(TECHNICAL_TENANT_ID);
  }

  @Test
  void
      setTechnicalContextIfMultiTenancyIsEnabled_Should_notSetTechnicalTenantContext_When_multiTenancyIsDisabled() {
    // given
    setField(tenantContextProvider, "multiTenancyEnabled", false);

    // when
    tenantContextProvider.setTechnicalContextIfMultiTenancyIsEnabled();

    // then
    assertThat(TenantContext.getCurrentTenant()).isNull();
  }

  @Test
  void setCurrentTenantContextIfMissing_Should_setTenantContext_When_contextNotSet() {
    // given, when
    tenantContextProvider.setCurrentTenantContextIfMissing(2L);
    // then
    assertThat(TenantContext.getCurrentTenant()).isEqualTo(2L);
  }

  @Test
  void setCurrentTenantContextIfMissing_Should_Not_setTenantContext_When_contextIsSet() {
    // given
    TenantContext.setCurrentTenant(1L);

    // when
    tenantContextProvider.setCurrentTenantContextIfMissing(2L);

    // then
    assertThat(TenantContext.getCurrentTenant()).isEqualTo(1L);
  }
}
