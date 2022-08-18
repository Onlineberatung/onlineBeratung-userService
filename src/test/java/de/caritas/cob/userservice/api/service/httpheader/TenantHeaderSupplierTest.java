package de.caritas.cob.userservice.api.service.httpheader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class TenantHeaderSupplierTest {

  @InjectMocks TenantHeaderSupplier tenantHeaderSupplier;

  private Enumeration<String> headers;

  @Mock private ServletRequestAttributes requestAttributes;

  @Mock private HttpServletRequest httpServletRequest;

  @Test
  void getOriginHeaderValue_Should_ReturnValueFromTenantIdHeader() {
    // given
    headers = Collections.enumeration(Lists.newArrayList("tenantId"));
    when(httpServletRequest.getHeader("tenantId")).thenReturn("1");
    givenRequestContextIsSet();
    // when, then
    assertThat(tenantHeaderSupplier.getTenantFromHeader()).isEqualTo(Optional.of(1L));
    resetRequestAttributes();
  }

  private void givenRequestContextIsSet() {
    when(requestAttributes.getRequest()).thenReturn(httpServletRequest);
    RequestContextHolder.setRequestAttributes(requestAttributes);
  }

  private void resetRequestAttributes() {
    RequestContextHolder.setRequestAttributes(null);
  }
}
