package de.caritas.cob.userservice.config;

import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.authorization.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.userservice.filter.StatelessCsrfFilter;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticatedActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;

/**
 * Provides the Keycloak/Spring Security configuration.
 */
@KeycloakConfiguration
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

  @SuppressWarnings("unused")
  private final KeycloakClientRequestFactory keycloakClientRequestFactory;
  private final CsrfSecurityProperties csrfSecurityProperties;

  /**
   * Processes HTTP requests and checks for a valid spring security authentication for the
   * (Keycloak) principal (authorization header).
   */
  public SecurityConfig(KeycloakClientRequestFactory keycloakClientRequestFactory,
      CsrfSecurityProperties csrfSecurityProperties) {
    this.keycloakClientRequestFactory = keycloakClientRequestFactory;
    this.csrfSecurityProperties = csrfSecurityProperties;
  }

  /**
   * Configure spring security filter chain: disable default Spring Boot CSRF token behavior and add
   * custom {@link StatelessCsrfFilter}, set all sessions to be fully stateless, define necessary
   * Keycloak roles for specific REST API paths
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http.csrf().disable()
        .addFilterBefore(new StatelessCsrfFilter(csrfSecurityProperties), CsrfFilter.class)
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .sessionAuthenticationStrategy(sessionAuthenticationStrategy()).and().authorizeRequests()
        .antMatchers(csrfSecurityProperties.getWhitelist().getConfigUris())
        .permitAll()
        .antMatchers("/users/askers/new", "/conversations/askers/anonymous/new")
        .permitAll()
        .antMatchers("/users/data", "/users/email", "/users/mails/messages/new",
            "/users/password/change", "/users/chat/{chatId:[0-9]+}",
            "/users/chat/{chatId:[0-9]+}/join", "/users/chat/{chatId:[0-9]+}/members",
            "/users/chat/{chatId:[0-9]+}/leave")
        .hasAnyAuthority(Authority.USER_DEFAULT, Authority.CONSULTANT_DEFAULT)
        .antMatchers("/users/sessions/{sessionId:[0-9]+}/enquiry/new", "/users/sessions/askers",
            "/users/askers/consultingType/new", "/users/account", "/users/mobiletoken",
            "/users/sessions/{sessionId:[0-9]+}/data")
        .hasAuthority(Authority.USER_DEFAULT)
        .antMatchers("/users/sessions/open", "/users/sessions/consultants/new",
            "/users/sessions/new/{sessionId:[0-9]+}", "/users/consultants/absences",
            "/users/sessions/consultants", "/users/sessions/teams",
            "/users/sessions/monitoring/{sessionId:[0-9]+}",
            "/users/sessions/{sessionId:[0-9]+}/monitoring",
            "/conversations/askers/anonymous/{sessionId:[0-9]+}/accept",
            "/conversations/consultants/**")
        .hasAuthority(Authority.CONSULTANT_DEFAULT)
        .antMatchers("/conversations/anonymous/{sessionId:[0-9]+}/finish")
        .hasAnyAuthority(Authority.CONSULTANT_DEFAULT, Authority.ANONYMOUS_DEFAULT)
        .antMatchers("/users/sessions/{sessionId:[0-9]+}/consultant/{consultantId:[0-9A-Za-z-]+}")
        .hasAnyAuthority(Authority.ASSIGN_CONSULTANT_TO_ENQUIRY,
            Authority.ASSIGN_CONSULTANT_TO_SESSION)
        .antMatchers("/users/consultants").hasAuthority(Authority.VIEW_AGENCY_CONSULTANTS)
        .antMatchers("/users/consultants/import", "/users/askers/import",
            "/users/askersWithoutSession/import")
        .hasAuthority(Authority.TECHNICAL_DEFAULT)
        .antMatchers("/liveproxy/send")
        .hasAnyAuthority(Authority.USER_DEFAULT, Authority.CONSULTANT_DEFAULT)
        .antMatchers("/users/mails/messages/feedback/new")
        .hasAuthority(Authority.USE_FEEDBACK).antMatchers("/users/messages/key")
        .hasAuthority(Authority.TECHNICAL_DEFAULT).antMatchers("/users/chat/new")
        .hasAuthority(Authority.CREATE_NEW_CHAT).antMatchers("/users/chat/{chatId:[0-9]+}/start")
        .hasAuthority(Authority.START_CHAT).antMatchers("/users/chat/{chatId:[0-9]+}/stop")
        .hasAuthority(Authority.STOP_CHAT).antMatchers("/users/chat/{chatId:[0-9]+}/update")
        .hasAuthority(Authority.UPDATE_CHAT).antMatchers("/useradmin", "/useradmin/**")
        .hasAuthority(Authority.USER_ADMIN)
        .antMatchers("/users/consultants/sessions/{sessionId:[0-9]+}")
        .hasAuthority(Authority.CONSULTANT_DEFAULT).anyRequest().denyAll();
  }

  /**
   * Use the KeycloakSpringBootConfigResolver to be able to save the Keycloak settings in the spring
   * application properties.
   */
  @Bean
  public KeycloakConfigResolver keyCloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  /**
   * Change springs authentication strategy to be stateless (no session is being created).
   */
  @Bean
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new NullAuthenticatedSessionStrategy();
  }

  /**
   * Change the default AuthenticationProvider to KeycloakAuthenticationProvider and register it in
   * the spring security context. Set the GrantedAuthoritiesMapper to map the Keycloak roles to the
   * granted authorities.
   */
  @Autowired
  public void configureGlobal(final AuthenticationManagerBuilder auth,
      RoleAuthorizationAuthorityMapper authorityMapper) {
    var keyCloakAuthProvider = keycloakAuthenticationProvider();
    keyCloakAuthProvider.setGrantedAuthoritiesMapper(authorityMapper);
    auth.authenticationProvider(keyCloakAuthProvider);
  }

  /**
   * From the Keycloak documentation: "Spring Boot attempts to eagerly register filter beans with
   * the web application context. Therefore, when running the Keycloak Spring Security adapter in a
   * Spring Boot environment, it may be necessary to add FilterRegistrationBeans to your security
   * configuration to prevent the Keycloak filters from being registered twice."
   *
   * https://github.com/keycloak/keycloak-documentation/blob/master/securing_apps/topics/oidc/java/spring-security-adapter.adoc
   *
   * @param filter {@link KeycloakAuthenticationProcessingFilter}
   * @return {@link FilterRegistrationBean}
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakAuthenticationProcessingFilterRegistrationBean(
      KeycloakAuthenticationProcessingFilter filter) {
    var registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  /**
   * see above: {@link SecurityConfig#keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter)
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakPreAuthActionsFilterRegistrationBean(
      KeycloakPreAuthActionsFilter filter) {
    var registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  /**
   * see above: {@link SecurityConfig#keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter)
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakAuthenticatedActionsFilterBean(
      KeycloakAuthenticatedActionsFilter filter) {
    var registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  /**
   * see above: {@link SecurityConfig#keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter)
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakSecurityContextRequestFilterBean(
      KeycloakSecurityContextRequestFilter filter) {
    var registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }
}
