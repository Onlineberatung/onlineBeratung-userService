package de.caritas.cob.userservice.api.tenant;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import de.caritas.cob.userservice.filter.SubdomainExtractor;
import de.caritas.cob.userservice.tenantservice.generated.web.TenantControllerApi;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnExpression("${multitenancy.enabled:true}")
@Component
public class TenantResolver {

  private static final String TENANT_ID = "tenantId";
  private static final long TECHNICAL_TENANT_ID = 0L;
  private @NonNull SubdomainExtractor subdomainExtractor;
  private @NonNull TenantControllerApi tenantControllerApi;

  public Long resolve(HttpServletRequest request) {
    if (userIsAuthenticated(request)) {
      return resolveForAuthenticatedUser(request);
    } else {
      return resolveForNonAuthenticatedUser();
    }
  }

  private Long resolveForNonAuthenticatedUser() {
    Optional<Long> tenantId = resolveTenantFromSubdomain();
    if (tenantId.isEmpty()) {
      throw new AccessDeniedException("Tenant id could not be resolved");
    }
    return tenantId.get();
  }

  private Long resolveForAuthenticatedUser(HttpServletRequest request) {
    return isTechnicalUserRole(request) ? TECHNICAL_TENANT_ID : resolveForAuthenticatedNonTechnicalUser(request);
  }

  private Long resolveForAuthenticatedNonTechnicalUser(HttpServletRequest request) {
    Optional<Long> tenantId = resolveTenantIdFromTokenClaims(request);
    Optional<Long> tenantIdFromSubdomain = resolveTenantFromSubdomain();
    if (tenantId.isPresent() && tenantIdFromSubdomain.isPresent()) {
      if (tenantId.get().equals(tenantIdFromSubdomain.get())) {
        return tenantId.get();
      }
      throw new AccessDeniedException("Tenant id from claim and subdomain not same.");
    }
    throw new AccessDeniedException("Tenant id could not be resolved");
  }

  private Optional<Long> resolveTenantFromSubdomain() {
    Optional<String> currentSubdomain = subdomainExtractor.getCurrentSubdomain();
    if (currentSubdomain.isPresent()) {
      return of(getTenantIdBySubdomain(currentSubdomain.get()));
    } else {
      return empty();
    }
  }

  private Long getTenantIdBySubdomain(String currentSubdomain) {
    return tenantControllerApi.getRestrictedTenantDataBySubdomain(currentSubdomain).getId();
  }

  private Optional<Long> getUserAttribute(Map<String, Object> claimMap, String claim) {
    if (claimMap.containsKey(claim)) {
      String userAttribute = (String) claimMap.get(claim);
      return of(Long.parseLong(userAttribute));
    } else {
      return Optional.empty();
    }
  }

  private Optional<Long> resolveTenantIdFromTokenClaims(HttpServletRequest request) {
    Map<String, Object> claimMap = getClaimMap(request);
    log.debug("Found tenantId in claim : " + claimMap.toString());
    return getUserAttribute(claimMap, TENANT_ID);
  }

  private boolean isTechnicalUserRole(HttpServletRequest request) {
    AccessToken token = ((KeycloakAuthenticationToken) request.getUserPrincipal()).getAccount()
        .getKeycloakSecurityContext().getToken();
    var accountResourceAccess = token.getResourceAccess("account");
    return hasRoles(accountResourceAccess) && accountResourceAccess.getRoles().contains("technical");
  }

  private boolean hasRoles(Access accountResourceAccess) {
    return accountResourceAccess != null && accountResourceAccess.getRoles() != null;
  }

  private boolean userIsAuthenticated(HttpServletRequest request) {
    return request.getUserPrincipal() != null;
  }

  private Map<String, Object> getClaimMap(HttpServletRequest request) {
    KeycloakSecurityContext keycloakSecContext =
        ((KeycloakAuthenticationToken) request.getUserPrincipal()).getAccount()
            .getKeycloakSecurityContext();
    return keycloakSecContext.getToken().getOtherClaims();
  }

}
