package de.caritas.cob.userservice.api.config.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.Mockito.mock;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
public class RoleAuthorizationAuthorityMapperTest {

  private final KeycloakAuthenticationProvider provider = new KeycloakAuthenticationProvider();
  private final Set<String> roles =
      Stream.of(UserRole.values()).map(UserRole::getValue).collect(Collectors.toSet());

  @Test
  public void roleAuthorizationAuthorityMapper_Should_GrantCorrectAuthorities() {

    Principal principal = mock(Principal.class);
    RefreshableKeycloakSecurityContext securityContext =
        mock(RefreshableKeycloakSecurityContext.class);
    KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, securityContext);

    KeycloakAuthenticationToken token = new KeycloakAuthenticationToken(account, false);

    RoleAuthorizationAuthorityMapper roleAuthorizationAuthorityMapper =
        new RoleAuthorizationAuthorityMapper();
    provider.setGrantedAuthoritiesMapper(roleAuthorizationAuthorityMapper);

    Authentication result = provider.authenticate(token);

    Set<SimpleGrantedAuthority> expectedGrantendAuthorities = new HashSet<>();
    roles.forEach(
        roleName -> {
          expectedGrantendAuthorities.addAll(
              Authority.getAuthoritiesByUserRole(UserRole.getRoleByValue(roleName).get()).stream()
                  .map(SimpleGrantedAuthority::new)
                  .collect(Collectors.toSet()));
        });

    assertThat(expectedGrantendAuthorities, containsInAnyOrder(result.getAuthorities().toArray()));
  }
}
