package de.caritas.cob.userservice.api.service.httpheader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantHeaderSupplierTest {

  @InjectMocks TenantHeaderSupplier tenantHeaderSupplier;

  @Mock private HttpHeadersResolver httpHeadersResolver;

  @Test
  void getOriginHeaderValue_Should_ReturnValueFromTenantIdHeader() {
    // given
    when(httpHeadersResolver.findHeaderValue("tenantId")).thenReturn(Optional.of(1L));
    // when, then
    assertThat(tenantHeaderSupplier.getTenantFromHeader()).isEqualTo(Optional.of(1L));
  }

  @Test
  void getOriginHeaderValue_Should_NotAddTenantIdHeaderIfTenantIdIsEmpty() {
    // given
    when(httpHeadersResolver.findHeaderValue("tenantId")).thenReturn(Optional.empty());
    // when, then
    assertThat(tenantHeaderSupplier.getTenantFromHeader()).isEmpty();
  }
}
