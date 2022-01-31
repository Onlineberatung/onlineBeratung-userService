package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.config.CsrfSecurityProperties;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({CsrfSecurityProperties.class})
public class UserServiceApplication {

  @Value("${thread.executor.corePoolSize}")
  private int THREAD_CORE_POOL_SIZE;
  @Value("${thread.executor.maxPoolSize}")
  private int THREAD_MAX_POOL_SIZE;
  @Value("${thread.executor.queueCapacity}")
  private int THREAD_QUEUE_CAPACITY;
  @Value("${thread.executor.threadNamePrefix}")
  private String THREAD_NAME_PREFIX;

  private final String claimNameUserId = "userId";
  private final String claimNameUsername = "username";

  public static void main(String[] args) {
    SpringApplication.run(UserServiceApplication.class, args);
  }

  /**
   * Returns the @KeycloakAuthenticationToken which represents the token for a Keycloak
   * authentication.
   *
   * @return KeycloakAuthenticationToken
   */
  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public KeycloakAuthenticationToken getAccessToken() {
    return (KeycloakAuthenticationToken) getRequest().getUserPrincipal();
  }

  /**
   * Returns the @KeycloakSecurityContext
   *
   * @return KeycloakSecurityContext
   */
  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public KeycloakSecurityContext getKeycloakSecurityContext() {
    return (KeycloakSecurityContext) ((KeycloakAuthenticationToken) getRequest().getUserPrincipal())
        .getAccount().getKeycloakSecurityContext();
  }

  /**
   * Returns the Keycloak user id of the authenticated user
   *
   * @return {@link AuthenticatedUser}
   */
  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public AuthenticatedUser getAuthenticatedUser() {

    // Get current KeycloakSecurityContext
    KeycloakSecurityContext keycloakSecContext =
        ((KeycloakAuthenticationToken) getRequest().getUserPrincipal()).getAccount()
            .getKeycloakSecurityContext();

    AuthenticatedUser authenticatedUser = new AuthenticatedUser();

    // Set Keycloak token to authenticated user object
    if (keycloakSecContext.getTokenString() != null) {
      authenticatedUser.setAccessToken(keycloakSecContext.getTokenString());
    } else {
      throw new KeycloakException("No valid Keycloak access token string found.");
    }

    // Set userId and username to authenticated user object
    Map<String, Object> claimMap = keycloakSecContext.getToken().getOtherClaims();
    if (claimMap.containsKey(claimNameUserId)) {
      authenticatedUser.setUserId(claimMap.get(claimNameUserId).toString());
    } else {
      throw new KeycloakException("Keycloak user attribute '" + claimNameUserId + "' not found.");
    }

    if (claimMap.containsKey(claimNameUsername)) {
      authenticatedUser.setUsername(claimMap.get(claimNameUsername).toString());
    }

    // Set user roles
    AccessToken.Access realmAccess = keycloakSecContext.getToken().getRealmAccess();
    Set<String> roles = realmAccess.getRoles();
    if (roles != null && roles.size() > 0) {
      authenticatedUser.setRoles(roles);
    } else {
      throw new KeycloakException(
          "Keycloak roles null or not set for user: " + authenticatedUser.getUserId() != null
              ? authenticatedUser.getUserId()
              : "unknown");
    }

    // Set granted authorities
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    authenticatedUser.setGrantedAuthorities(authentication.getAuthorities().stream()
        .map(authority -> authority.toString()).collect(Collectors.toSet()));

    return authenticatedUser;
  }

  private HttpServletRequest getRequest() {
    return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
        .getRequest();
  }

  @Bean
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    /*
     * This will create 10 threads at the time of initialization. If all 10 threads are busy and new
     * task comes up, then It will keep tasks in queue. If queue is full it will create 11th thread
     * and will go till 15. Then will throw TaskRejected Exception.
     */
    executor.setCorePoolSize(THREAD_CORE_POOL_SIZE);
    executor.setMaxPoolSize(THREAD_MAX_POOL_SIZE);
    executor.setQueueCapacity(THREAD_QUEUE_CAPACITY);
    executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
    executor.initialize();
    return executor;
  }

}
