package de.caritas.cob.userservice.api.adapters.web.controller.interceptor;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.config.CsrfSecurityProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/** This custom filter checks CSRF cookie and header token for equality. */
public class StatelessCsrfFilter extends OncePerRequestFilter {

  private final RequestMatcher requireCsrfProtectionMatcher;
  private final AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
  private final CsrfSecurityProperties csrfSecurityProperties;

  public StatelessCsrfFilter(CsrfSecurityProperties csrfSecurityProperties) {
    this.csrfSecurityProperties = csrfSecurityProperties;
    this.requireCsrfProtectionMatcher = new DefaultRequiresCsrfMatcher(this.csrfSecurityProperties);
  }

  @Override
  public void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (requireCsrfProtectionMatcher.matches(request)) {
      final String csrfTokenValue =
          request.getHeader(this.csrfSecurityProperties.getHeader().getProperty());
      String csrfCookieValue = retrieveCsrfCookieValue(request);

      if (isNull(csrfTokenValue) || !csrfTokenValue.equals(csrfCookieValue)) {
        accessDeniedHandler.handle(
            request, response, new AccessDeniedException("Missing or non-matching CSRF-token"));
        return;
      }
    }
    filterChain.doFilter(request, response);
  }

  private String retrieveCsrfCookieValue(HttpServletRequest request) {
    final Cookie[] cookies = request.getCookies();
    return isNull(cookies)
        ? null
        : Stream.of(cookies)
            .filter(
                cookie ->
                    cookie.getName().equals(this.csrfSecurityProperties.getCookie().getProperty()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
  }

  @RequiredArgsConstructor
  private static final class DefaultRequiresCsrfMatcher implements RequestMatcher {

    private final Pattern allowedMethods = Pattern.compile("^(HEAD|TRACE|OPTIONS)$");
    private final @NonNull CsrfSecurityProperties csrfSecurityProperties;

    @Override
    public boolean matches(HttpServletRequest request) {
      return !(isWhiteListUrl(request) || isWhiteListHeader(request) || isAllowedMethod(request));
    }

    private boolean isWhiteListUrl(HttpServletRequest request) {
      List<String> csrfWhitelist =
          new ArrayList<>(Arrays.asList(csrfSecurityProperties.getWhitelist().getConfigUris()));
      csrfWhitelist.addAll(Arrays.asList(csrfSecurityProperties.getWhitelist().getAdminUris()));
      return csrfWhitelist.parallelStream()
          .anyMatch(request.getRequestURI().toLowerCase()::contains);
    }

    private boolean isWhiteListHeader(HttpServletRequest request) {
      return isNotBlank(
          request.getHeader(this.csrfSecurityProperties.getWhitelist().getHeader().getProperty()));
    }

    private boolean isAllowedMethod(HttpServletRequest request) {
      return allowedMethods.matcher(request.getMethod()).matches();
    }
  }
}
