package de.caritas.cob.userservice.api.adapters.keycloak.config;

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
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
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
  public AuthenticatedUser authenticatedUser(HttpServletRequest request) {
    var userPrincipal = request.getUserPrincipal();
    var authenticatedUser = new AuthenticatedUser();

    if (nonNull(userPrincipal)) {
      var authToken = (KeycloakAuthenticationToken) userPrincipal;
      var securityContext = authToken.getAccount().getKeycloakSecurityContext();
      var claimMap = securityContext.getToken().getOtherClaims();

      try {
        if (claimMap.containsKey("username")) {
          authenticatedUser.setUsername(claimMap.get("username").toString());
        }
        authenticatedUser.setUserId(claimMap.get("userId").toString());
        authenticatedUser.setAccessToken(securityContext.getTokenString());
        authenticatedUser.setRoles(securityContext.getToken().getRealmAccess().getRoles());
      } catch (Exception exception) {
        throw new KeycloakException("Keycloak data missing.", exception);
      }

      var authorities =
          SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
              .map(Object::toString)
              .collect(Collectors.toSet());
      authenticatedUser.setGrantedAuthorities(authorities);
    }

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

  /**
   * Use the KeycloakSpringBootConfigResolver to be able to save the Keycloak settings in the spring
   * application properties.
   */
  @Bean
  public KeycloakConfigResolver keyCloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  @URL private String authServerUrl;

  @NotBlank private String realm;

  @NotBlank private String resource;

  @NotBlank private String principalAttribute;

  @NotNull private Boolean cors;

  private KeycloakCustomConfig config;
}
