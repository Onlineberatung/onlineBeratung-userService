package de.caritas.cob.userservice.api.tenant;

import static de.caritas.cob.userservice.api.config.auth.UserRole.TECHNICAL;
import static de.caritas.cob.userservice.api.config.auth.UserRole.TENANT_ADMIN;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Component;

@Component
public class TechnicalOrSuperAdminUserTenantResolver implements TenantResolver {

  @Override
  public Optional<Long> resolve(HttpServletRequest request) {
    return isTechnicalOrTenantSuperAdminUserRole(request) ? Optional.of(0L) : Optional.empty();
  }

  private boolean isTechnicalOrTenantSuperAdminUserRole(HttpServletRequest request) {
    return containsAnyRole(request, TECHNICAL.getValue(), TENANT_ADMIN.getValue());
  }

  private boolean containsAnyRole(HttpServletRequest request, String... expectedRoles) {
    AccessToken token =
        ((KeycloakAuthenticationToken) request.getUserPrincipal())
            .getAccount()
            .getKeycloakSecurityContext()
            .getToken();
    if (hasRoles(token)) {
      Set<String> roles = token.getRealmAccess().getRoles();
      return containsAny(roles, expectedRoles);
    } else {
      return false;
    }
  }

  private boolean containsAny(Set<String> roles, String... expectedRoles) {
    return Arrays.stream(expectedRoles).anyMatch(roles::contains);
  }

  private boolean hasRoles(AccessToken accessToken) {
    return accessToken.getRealmAccess() != null && accessToken.getRealmAccess().getRoles() != null;
  }

  @Override
  public boolean canResolve(HttpServletRequest request) {
    return resolve(request).isPresent();
  }
}
