package de.caritas.cob.userservice.api.service.httpheader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class OriginHeaderSupplierTest {

  @InjectMocks
  OriginHeaderSupplier originHeaderSupplier;

  private Enumeration<String> headers;

  @Mock
  private ServletRequestAttributes requestAttributes;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Test
  void getOriginHeaderValue_Should_ReturnPassedRequestServerName() {
    assertThat(originHeaderSupplier.getOriginHeaderValue("request server name")).isEqualTo("request server name");
  }

  @Test
  void getOriginHeaderValue_Should_ReturnValueFromHostRequestParameter() {
    // given
    headers = Collections.enumeration(Lists.newArrayList("host"));
    when(httpServletRequest.getHeader("host")).thenReturn("host header value");
    givenRequestContextIsSet();
    // when, then
    assertThat(originHeaderSupplier.getOriginHeaderValue()).isEqualTo("host header value");
  }

  private void givenRequestContextIsSet() {
    when(requestAttributes.getRequest()).thenReturn(httpServletRequest);
    when(httpServletRequest.getHeaderNames()).thenReturn(headers);
    RequestContextHolder.setRequestAttributes(requestAttributes);
  }

}