package de.caritas.cob.userservice.api.config.auth;

import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.*;

import de.caritas.cob.userservice.api.adapters.web.controller.interceptor.HttpTenantFilter;
import de.caritas.cob.userservice.api.adapters.web.controller.interceptor.StatelessCsrfFilter;
import de.caritas.cob.userservice.api.config.CsrfSecurityProperties;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;

/** Provides the Keycloak/Spring Security configuration. */
@KeycloakConfiguration
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

  private static final String UUID_PATTERN =
      "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";
  public static final String APPOINTMENTS_APPOINTMENT_ID = "/appointments/{appointmentId:";

  @SuppressWarnings({"unused", "FieldCanBeLocal"})
  private final KeycloakClientRequestFactory keycloakClientRequestFactory;

  private final CsrfSecurityProperties csrfSecurityProperties;

  @Value("${multitenancy.enabled}")
  private boolean multitenancy;

  private HttpTenantFilter tenantFilter;

  /**
   * Processes HTTP requests and checks for a valid spring security authentication for the
   * (Keycloak) principal (authorization header).
   */
  public SecurityConfig(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
          KeycloakClientRequestFactory keycloakClientRequestFactory,
      CsrfSecurityProperties csrfSecurityProperties,
      @Nullable HttpTenantFilter tenantFilter) {
    this.keycloakClientRequestFactory = keycloakClientRequestFactory;
    this.csrfSecurityProperties = csrfSecurityProperties;
    this.tenantFilter = tenantFilter;
  }

  /**
   * Configure spring security filter chain: disable default Spring Boot CSRF token behavior and add
   * custom {@link StatelessCsrfFilter}, set all sessions to be fully stateless, define necessary
   * Keycloak roles for specific REST API paths
   */
  @Override
  @SuppressWarnings("java:S4502") // Disabling CSRF protections is security-sensitive
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    var httpSecurity =
        http.csrf()
            .disable()
            .addFilterBefore(new StatelessCsrfFilter(csrfSecurityProperties), CsrfFilter.class);

    httpSecurity = enableTenantFilterIfMultitenancyEnabled(httpSecurity);

    httpSecurity
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
        .and()
        .authorizeRequests()
        .antMatchers(csrfSecurityProperties.getWhitelist().getConfigUris())
        .permitAll()
        .antMatchers(
            "/users/askers/new",
            "/conversations/askers/anonymous/new",
            "/users/consultants/{consultantId:" + UUID_PATTERN + "}",
            "/users/consultants/languages")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/conversations/anonymous/{sessionId:[0-9]+}")
        .hasAnyAuthority(ANONYMOUS_DEFAULT)
        .antMatchers("/users/notifications")
        .hasAnyAuthority(NOTIFICATIONS_TECHNICAL)
        .antMatchers("/users/data")
        .hasAnyAuthority(
            ANONYMOUS_DEFAULT,
            USER_DEFAULT,
            CONSULTANT_DEFAULT,
            SINGLE_TENANT_ADMIN,
            TENANT_ADMIN,
            RESTRICTED_AGENCY_ADMIN)
        .antMatchers(HttpMethod.GET, APPOINTMENTS_APPOINTMENT_ID + UUID_PATTERN + "}")
        .permitAll()
        .antMatchers("/users/sessions/askers")
        .hasAnyAuthority(ANONYMOUS_DEFAULT, USER_DEFAULT)
        .antMatchers(
            "/users/email",
            "/users/mails/messages/new",
            "/users/chat/{chatId:[0-9]+}",
            "/users/chat/e2e",
            "/users/chat/{chatId:[0-9]+}/join",
            "/users/chat/{chatId:[0-9]+}/members",
            "/users/chat/{chatId:[0-9]+}/leave",
            "/users/chat/{groupId:[\\dA-Za-z-,]+}/assign",
            "/users/consultants/toggleWalkThrough")
        .hasAnyAuthority(USER_DEFAULT, CONSULTANT_DEFAULT)
        .antMatchers("/users/chat/{chatId:[0-9]+}/verify")
        .hasAnyAuthority(CONSULTANT_DEFAULT)
        .antMatchers("/users/password/change")
        .hasAnyAuthority(
            USER_DEFAULT,
            CONSULTANT_DEFAULT,
            SINGLE_TENANT_ADMIN,
            TENANT_ADMIN,
            RESTRICTED_AGENCY_ADMIN)
        .antMatchers("/users/twoFactorAuth", "/users/2fa/**", "/users/mobile/app/token")
        .hasAnyAuthority(
            SINGLE_TENANT_ADMIN,
            TENANT_ADMIN,
            USER_DEFAULT,
            CONSULTANT_DEFAULT,
            RESTRICTED_AGENCY_ADMIN)
        .antMatchers("/users/statistics/registration")
        .hasAnyAuthority(SINGLE_TENANT_ADMIN, TENANT_ADMIN)
        .antMatchers(
            "/users/sessions/{sessionId:[0-9]+}/enquiry/new",
            "/appointments/sessions/{sessionId:[0-9]+}/enquiry/new",
            "/users/askers/consultingType/new",
            "/users/account",
            "/users/mobiletoken",
            "/users/sessions/{sessionId:[0-9]+}/data")
        .hasAuthority(USER_DEFAULT)
        .regexMatchers(HttpMethod.GET, "/users/sessions/room\\?rcGroupIds=[\\dA-Za-z-,]+")
        .hasAnyAuthority(ANONYMOUS_DEFAULT, USER_DEFAULT, CONSULTANT_DEFAULT)
        .antMatchers(HttpMethod.GET, "/users/sessions/room/{sessionId:[0-9]+}")
        .hasAnyAuthority(ANONYMOUS_DEFAULT, USER_DEFAULT, CONSULTANT_DEFAULT)
        .antMatchers(HttpMethod.GET, "/users/chat/room/{chatId:[0-9]+}")
        .hasAnyAuthority(USER_DEFAULT, CONSULTANT_DEFAULT)
        .antMatchers(
            "/users/sessions/open",
            "/users/sessions/consultants/new",
            "/users/sessions/new/{sessionId:[0-9]+}",
            "/users/consultants/absences",
            "/users/sessions/consultants",
            "/users/sessions/teams",
            "/conversations/askers/anonymous/{sessionId:[0-9]+}/accept",
            "/conversations/consultants/**")
        .hasAuthority(CONSULTANT_DEFAULT)
        .antMatchers("/conversations/anonymous/{sessionId:[0-9]+}/finish")
        .hasAnyAuthority(CONSULTANT_DEFAULT, ANONYMOUS_DEFAULT)
        .antMatchers("/users/sessions/{sessionId:[0-9]+}/consultant/{consultantId:[0-9A-Za-z-]+}")
        .hasAnyAuthority(ASSIGN_CONSULTANT_TO_ENQUIRY, ASSIGN_CONSULTANT_TO_SESSION)
        .antMatchers("/users/consultants")
        .hasAuthority(VIEW_AGENCY_CONSULTANTS)
        .antMatchers(
            "/users/consultants/import",
            "/users/askers/import",
            "/users/askersWithoutSession/import",
            "/users/sessions/rocketChatGroupId")
        .hasAuthority(TECHNICAL_DEFAULT)
        .antMatchers("/liveproxy/send")
        .hasAnyAuthority(USER_DEFAULT, CONSULTANT_DEFAULT, ANONYMOUS_DEFAULT)
        .antMatchers("/users/mails/messages/feedback/new")
        .hasAuthority(USE_FEEDBACK)
        .antMatchers("/users/messages/key")
        .hasAuthority(TECHNICAL_DEFAULT)
        .antMatchers("/users/chat/new", "/users/chat/v2/new")
        .hasAuthority(CREATE_NEW_CHAT)
        .antMatchers("/users/chat/{chatId:[0-9]+}/start")
        .hasAuthority(START_CHAT)
        .antMatchers("/users/chat/{chatId:[0-9]+}/stop")
        .hasAuthority(STOP_CHAT)
        .antMatchers(
            "/users/chat/{chatId:[0-9]+}/update",
            "/users/{chatUserId:[0-9A-Za-z]+}/chat/{chatId:[0-9]+}/ban")
        .hasAuthority(UPDATE_CHAT)
        .antMatchers("/useradmin/tenantadmins/", "/useradmin/tenantadmins/**")
        .hasAuthority(TENANT_ADMIN)
        .antMatchers("/useradmin/data/*")
        .hasAnyAuthority(SINGLE_TENANT_ADMIN, RESTRICTED_AGENCY_ADMIN)
        .antMatchers(HttpMethod.POST, "/useradmin/consultants/")
        .hasAnyAuthority(CONSULTANT_CREATE, TECHNICAL_DEFAULT)
        .antMatchers(HttpMethod.PUT, "/useradmin/consultants/{consultantId:" + UUID_PATTERN + "}")
        .hasAnyAuthority(CONSULTANT_UPDATE, TECHNICAL_DEFAULT)
        .antMatchers(
            HttpMethod.PUT, "/useradmin/consultants/{consultantId:" + UUID_PATTERN + "}/agencies")
        .hasAnyAuthority(CONSULTANT_UPDATE, TECHNICAL_DEFAULT)
        .antMatchers("/useradmin", "/useradmin/**")
        .hasAnyAuthority(USER_ADMIN, TECHNICAL_DEFAULT)
        .antMatchers("/users/consultants/search")
        .hasAnyAuthority(USER_ADMIN, TECHNICAL_DEFAULT)
        .antMatchers(
            "/users/consultants/sessions/{sessionId:[0-9]+}",
            "/users/sessions/{sessionId:[0-9]+}/archive",
            "/users/sessions/{sessionId:[0-9]+}")
        .hasAnyAuthority(CONSULTANT_DEFAULT)
        .antMatchers("/appointments")
        .hasAnyAuthority(CONSULTANT_DEFAULT, TECHNICAL_DEFAULT)
        .antMatchers("/appointments/booking/{id:[0-9]+}")
        .hasAnyAuthority(CONSULTANT_DEFAULT, TECHNICAL_DEFAULT)
        .antMatchers(HttpMethod.PUT, APPOINTMENTS_APPOINTMENT_ID + UUID_PATTERN + "}")
        .hasAuthority(CONSULTANT_DEFAULT)
        .antMatchers(HttpMethod.DELETE, APPOINTMENTS_APPOINTMENT_ID + UUID_PATTERN + "}")
        .hasAuthority(CONSULTANT_DEFAULT)
        .antMatchers("/users/sessions/{sessionId:[0-9]+}/dearchive", "/users/mails/reassignment")
        .hasAnyAuthority(USER_DEFAULT, CONSULTANT_DEFAULT)
        .antMatchers("/userstatistics", "/userstatistics/**")
        .permitAll()
        .antMatchers(HttpMethod.DELETE, "/useradmin/consultants/{consultantId:[0-9]+}/delete")
        .hasAnyAuthority(USER_ADMIN, RESTRICTED_AGENCY_ADMIN)
        .antMatchers(HttpMethod.GET, "/actuator/health")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/actuator/health/*")
        .permitAll()
        .mvcMatchers(HttpMethod.GET, "/users/{username}")
        .permitAll()
        .anyRequest()
        .denyAll();
  }

  /**
   * Adds additional filter for tenant feature if enabled that sets tenant_id into current thread.
   *
   * @param httpSecurity - httpSecurity
   * @return httpSecurity
   */
  private HttpSecurity enableTenantFilterIfMultitenancyEnabled(HttpSecurity httpSecurity) {
    if (multitenancy) {
      httpSecurity =
          httpSecurity.addFilterAfter(this.tenantFilter, KeycloakAuthenticatedActionsFilter.class);
    }
    return httpSecurity;
  }

  /**
   * Use the KeycloakSpringBootConfigResolver to be able to save the Keycloak settings in the spring
   * application properties.
   */
  @Bean
  public KeycloakConfigResolver keyCloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  /** Change springs authentication strategy to be stateless (no session is being created). */
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
  public void configureGlobal(
      final AuthenticationManagerBuilder auth, RoleAuthorizationAuthorityMapper authorityMapper) {
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
   * <p>https://github.com/keycloak/keycloak-documentation/blob/master/securing_apps/topics/oidc/java/spring-security-adapter.adoc
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
   * see above:
   * {@link
   * SecurityConfig#keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter)
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
   * see above:
   * {@link
   * SecurityConfig#keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter)
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
   * see above:
   * {@link
   * SecurityConfig#keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter)
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
