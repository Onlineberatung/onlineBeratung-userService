package de.caritas.cob.userservice.api.adapters.web.controller.interceptor;

import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import io.swagger.models.HttpMethod;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class ApiTokenFilter extends OncePerRequestFilter {

  @Value("${external.user.create.tenant.api.token}")
  private String externalUserCreateTenantApiToken;

  public ApiTokenFilter() {
    // Empty constructor
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    String method = request.getMethod();
    return !path.endsWith("/tenantadmins") || !method.equalsIgnoreCase(HttpMethod.POST.toString());
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = request.getHeader("api-token");

    if (validateExternalUserCreateTenantApiToken(token)) {
      // Create an authentication token
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(
              "ExternalTechnicalAdmin",
              null,
              Collections.singletonList(new SimpleGrantedAuthority(AuthorityValue.TENANT_ADMIN)));
      // Set the authentication in the SecurityContext
      SecurityContextHolder.getContext().setAuthentication(auth);
    }

    filterChain.doFilter(request, response);
  }

  private boolean validateExternalUserCreateTenantApiToken(String token) {
    return Objects.equals(externalUserCreateTenantApiToken, token);
  }
}
