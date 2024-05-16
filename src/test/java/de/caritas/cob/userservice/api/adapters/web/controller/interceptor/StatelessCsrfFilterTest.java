package de.caritas.cob.userservice.api.adapters.web.controller.interceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.config.CsrfSecurityProperties;
import de.caritas.cob.userservice.api.config.CsrfSecurityProperties.ConfigProperty;
import de.caritas.cob.userservice.api.config.CsrfSecurityProperties.Whitelist;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.web.access.AccessDeniedHandler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class StatelessCsrfFilterTest {

  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_COOKIE = "csrfCookie";
  private static final String CSRF_WHITELIST_COOKIE = "csrfWhitelistHeader";
  private static final String ADMIN_URI_ON_WHITE_LIST = "/useradmin";

  private StatelessCsrfFilter csrfFilter;

  @Mock private CsrfSecurityProperties csrfSecurityProperties;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @Mock private AccessDeniedHandler accessDeniedHandler;

  @BeforeEach
  public void setup() {
    ConfigProperty cookieProperty = new ConfigProperty();
    cookieProperty.setProperty(CSRF_COOKIE);
    ConfigProperty headerProperty = new ConfigProperty();
    headerProperty.setProperty(CSRF_HEADER);
    ConfigProperty whitelistProperty = new ConfigProperty();
    whitelistProperty.setProperty(CSRF_WHITELIST_COOKIE);

    Whitelist whitelist = new Whitelist();
    whitelist.setAdminUris(new String[] {ADMIN_URI_ON_WHITE_LIST});
    whitelist.setConfigUris(new String[] {});
    whitelist.setHeader(whitelistProperty);

    when(csrfSecurityProperties.getHeader()).thenReturn(headerProperty);
    when(csrfSecurityProperties.getCookie()).thenReturn(cookieProperty);
    when(csrfSecurityProperties.getWhitelist()).thenReturn(whitelist);
    csrfFilter = new StatelessCsrfFilter(csrfSecurityProperties);

    setField(csrfFilter, "accessDeniedHandler", accessDeniedHandler);
  }

  @Test
  public void doFilterInternal_Should_executeFilterChain_When_requestMethodIsAllowed()
      throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("uri");
    when(request.getMethod()).thenReturn("OPTIONS");

    this.csrfFilter.doFilterInternal(request, response, filterChain);

    verify(this.filterChain, times(1)).doFilter(request, response);
  }

  @Test
  public void doFilterInternal_Should_executeFilterChain_When_requestUriIsInWhiteList()
      throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn(ADMIN_URI_ON_WHITE_LIST);

    this.csrfFilter.doFilterInternal(request, response, filterChain);

    verify(this.filterChain, times(1)).doFilter(request, response);
  }

  @Test
  public void doFilterInternal_Should_executeFilterChain_When_requestHasCsrfWhitelistHeader()
      throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("uri");
    when(request.getHeader(CSRF_WHITELIST_COOKIE)).thenReturn("whitelisted");

    this.csrfFilter.doFilterInternal(request, response, filterChain);

    verify(this.filterChain, times(1)).doFilter(request, response);
  }

  @Test
  public void doFilterInternal_Should_executeFilterChain_When_requestCsrfHeaderAndCookieAreEqual()
      throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("uri");
    when(request.getMethod()).thenReturn("POST");
    when(request.getHeader(CSRF_HEADER)).thenReturn("csrfTokenValue");
    Cookie[] cookies = {new Cookie(CSRF_COOKIE, "csrfTokenValue")};
    when(request.getCookies()).thenReturn(cookies);

    this.csrfFilter.doFilterInternal(request, response, filterChain);

    verify(this.filterChain, times(1)).doFilter(request, response);
  }

  @Test
  public void doFilterInternal_Should_callAccessDeniedHandler_When_csrfHeaderIsNull()
      throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("uri");
    when(request.getMethod()).thenReturn("POST");
    Cookie[] cookies = {new Cookie(CSRF_COOKIE, "csrfTokenValue")};
    when(request.getCookies()).thenReturn(cookies);

    this.csrfFilter.doFilterInternal(request, response, filterChain);

    verify(this.accessDeniedHandler, times(1)).handle(any(), any(), any());
    verifyNoMoreInteractions(this.filterChain);
  }

  @Test
  public void doFilterInternal_Should_callAccessDeniedHandler_When_cookiesAreNull()
      throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("uri");
    when(request.getMethod()).thenReturn("POST");
    when(request.getHeader(CSRF_HEADER)).thenReturn("csrfHeaderTokenValue");

    this.csrfFilter.doFilterInternal(request, response, filterChain);

    verify(this.accessDeniedHandler, times(1)).handle(any(), any(), any());
    verifyNoMoreInteractions(this.filterChain);
  }

  @Test
  public void
      doFilterInternal_Should_callAccessDeniedHandler_When_csrfHeaderIsNotEqualToCookieToken()
          throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("uri");
    when(request.getMethod()).thenReturn("POST");
    when(request.getHeader(CSRF_HEADER)).thenReturn("csrfHeaderTokenValue");
    Cookie[] cookies = {new Cookie(CSRF_COOKIE, "csrfCookieTokenValue")};
    when(request.getCookies()).thenReturn(cookies);

    this.csrfFilter.doFilterInternal(request, response, filterChain);

    verify(this.accessDeniedHandler, times(1)).handle(any(), any(), any());
    verifyNoMoreInteractions(this.filterChain);
  }
}
