package de.caritas.cob.userservice.api.adapters.keycloak.config;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

@Data
@Configuration
@Validated
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakConfig {

  @Bean("keycloakRestTemplate")
  public RestTemplate keycloakRestTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.build();
  }

  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public KeycloakAuthenticationToken keycloakAuthenticationToken(HttpServletRequest request) {
    return (KeycloakAuthenticationToken) request.getUserPrincipal();
  }

  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public KeycloakSecurityContext keycloakSecurityContext(KeycloakAuthenticationToken token) {
    return token.getAccount().getKeycloakSecurityContext();
  }

  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public AuthenticatedUser authenticatedUser(KeycloakSecurityContext securityContext,
      KeycloakAuthenticationToken authenticationToken) {
    var authenticatedUser = new AuthenticatedUser();
    if (isNull(authenticationToken)) {
      return authenticatedUser;
    }

    if (isNull(securityContext.getTokenString())) {
      throw new KeycloakException("No Keycloak access token string found.");
    }
    authenticatedUser.setAccessToken(securityContext.getTokenString());

    var claimMap = securityContext.getToken().getOtherClaims();
    var userId = claimMap.get("userId");
    if (isNull(userId)) {
      throw new KeycloakException("Keycloak user attribute 'userId' not found.");
    }
    authenticatedUser.setUserId(userId.toString());

    var roles = securityContext.getToken().getRealmAccess().getRoles();
    if (isNull(roles) || roles.size() == 0) {
      var message = "Keycloak roles null or not set for user " + authenticatedUser.getUserId();
      throw new KeycloakException(message);
    }
    authenticatedUser.setRoles(roles);

    var username = claimMap.get("username");
    if (nonNull(username)) {
      authenticatedUser.setUsername(username.toString());
    }

    var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
        .stream()
        .map(Object::toString)
        .collect(Collectors.toSet());
    authenticatedUser.setGrantedAuthorities(authorities);

    return authenticatedUser;
  }

  @Bean
  public Keycloak keycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(authServerUrl)
        .realm(realm)
        .username(config.getAdminUsername())
        .password(config.getAdminPassword())
        .clientId(config.getAdminClientId())
        .build();
  }

  @URL
  private String authServerUrl;

  @NotBlank
  private String realm;

  @NotBlank
  private String resource;

  @NotBlank
  private String principalAttribute;

  @NotNull
  private Boolean cors;

  private KeycloakCustomConfig config;
}
