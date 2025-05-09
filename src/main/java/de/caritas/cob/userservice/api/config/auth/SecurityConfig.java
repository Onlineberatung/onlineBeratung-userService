package de.caritas.cob.userservice.api.config.auth;

import static de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue.*;

import de.caritas.cob.userservice.api.adapters.web.controller.interceptor.HttpTenantFilter;
import de.caritas.cob.userservice.api.adapters.web.controller.interceptor.StatelessCsrfFilter;
import de.caritas.cob.userservice.api.config.CsrfSecurityProperties;
import de.caritas.cob.userservice.api.service.security.AuthorisationService;
import de.caritas.cob.userservice.api.service.security.JwtAuthConverter;
import de.caritas.cob.userservice.api.service.security.JwtAuthConverterProperties;
import lombok.RequiredArgsConstructor;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Provides the Keycloak/Spring Security configuration. */
@Configuration
@KeycloakConfiguration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig implements WebMvcConfigurer {

  private static final String UUID_PATTERN =
      "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";
  public static final String APPOINTMENTS_APPOINTMENT_ID = "/appointments/{appointmentId:";

  @Autowired CsrfSecurityProperties csrfSecurityProperties;

  @Autowired AuthorisationService authorisationService;
  @Autowired JwtAuthConverterProperties jwtAuthConverterProperties;

  @Value("${csrf.cookie.property}")
  private String csrfCookieProperty;

  @Value("${csrf.header.property}")
  private String csrfHeaderProperty;

  @Autowired private Environment environment;

  @Value("${multitenancy.enabled}")
  private boolean multitenancy;

  @Autowired(required = false)
  private HttpTenantFilter tenantFilter;

  /**
   * Configure spring security filter chain: disable default Spring Boot CSRF token behavior and add
   * custom {@link StatelessCsrfFilter}, set all sessions to be fully stateless, define necessary
   * Keycloak roles for specific REST API paths
   */
  @Bean
  @SuppressWarnings("java:S4502")
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    var httpSecurity =
        http.csrf(csrf -> csrf.disable())
            .addFilterBefore(new StatelessCsrfFilter(csrfSecurityProperties), CsrfFilter.class);

    httpSecurity = enableTenantFilterIfMultitenancyEnabled(httpSecurity);

    httpSecurity
        .sessionManagement(
            management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers(csrfSecurityProperties.getWhitelist().getConfigUris())
                    .permitAll()
                    .requestMatchers(
                        "/users/askers/new",
                        "/conversations/askers/anonymous/new",
                        "/users/consultants/{consultantId:" + UUID_PATTERN + "}",
                        "/users/consultants/languages")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/conversations/anonymous/{sessionId:[0-9]+}")
                    .hasAnyAuthority(ANONYMOUS_DEFAULT)
                    .requestMatchers("/users/notifications")
                    .hasAnyAuthority(NOTIFICATIONS_TECHNICAL)
                    .requestMatchers("/users/data")
                    .hasAnyAuthority(
                        ANONYMOUS_DEFAULT,
                        USER_DEFAULT,
                        CONSULTANT_DEFAULT,
                        SINGLE_TENANT_ADMIN,
                        TENANT_ADMIN,
                        RESTRICTED_AGENCY_ADMIN)
                    .requestMatchers(
                        HttpMethod.GET, APPOINTMENTS_APPOINTMENT_ID + UUID_PATTERN + "}")
                    .permitAll()
                    .requestMatchers("/users/sessions/askers")
                    .hasAnyAuthority(ANONYMOUS_DEFAULT, USER_DEFAULT)
                    .requestMatchers(
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
                    .requestMatchers("/users/chat/{chatId:[0-9]+}/verify")
                    .hasAnyAuthority(CONSULTANT_DEFAULT)
                    .requestMatchers("/users/password/change")
                    .hasAnyAuthority(
                        USER_DEFAULT,
                        CONSULTANT_DEFAULT,
                        SINGLE_TENANT_ADMIN,
                        TENANT_ADMIN,
                        RESTRICTED_AGENCY_ADMIN)
                    .requestMatchers(
                        "/users/twoFactorAuth", "/users/2fa/**", "/users/mobile/app/token")
                    .hasAnyAuthority(
                        SINGLE_TENANT_ADMIN,
                        TENANT_ADMIN,
                        USER_DEFAULT,
                        CONSULTANT_DEFAULT,
                        RESTRICTED_AGENCY_ADMIN)
                    .requestMatchers("/users/statistics/registration")
                    .hasAnyAuthority(SINGLE_TENANT_ADMIN, TENANT_ADMIN)
                    .requestMatchers(
                        "/users/sessions/{sessionId:[0-9]+}/enquiry/new",
                        "/appointments/sessions/{sessionId:[0-9]+}/enquiry/new",
                        "/users/askers/consultingType/new",
                        "/users/account",
                        "/users/mobiletoken",
                        "/users/sessions/{sessionId:[0-9]+}/data")
                    .hasAuthority(USER_DEFAULT)
                    .requestMatchers(HttpMethod.GET, "/users/sessions/room")
                    .hasAnyAuthority(ANONYMOUS_DEFAULT, USER_DEFAULT, CONSULTANT_DEFAULT)
                    .requestMatchers(HttpMethod.GET, "/users/sessions/room/{sessionId:[0-9]+}")
                    .hasAnyAuthority(ANONYMOUS_DEFAULT, USER_DEFAULT, CONSULTANT_DEFAULT)
                    .requestMatchers(HttpMethod.GET, "/users/chat/room/{chatId:[0-9]+}")
                    .hasAnyAuthority(USER_DEFAULT, CONSULTANT_DEFAULT)
                    .requestMatchers(
                        "/users/sessions/open",
                        "/users/sessions/consultants/new",
                        "/users/sessions/new/{sessionId:[0-9]+}",
                        "/users/consultants/absences",
                        "/users/sessions/consultants",
                        "/users/sessions/teams",
                        "/conversations/askers/anonymous/{sessionId:[0-9]+}/accept",
                        "/conversations/consultants/**")
                    .hasAuthority(CONSULTANT_DEFAULT)
                    .requestMatchers("/conversations/anonymous/{sessionId:[0-9]+}/finish")
                    .hasAnyAuthority(CONSULTANT_DEFAULT, ANONYMOUS_DEFAULT)
                    .requestMatchers(
                        "/users/sessions/{sessionId:[0-9]+}/consultant/{consultantId:[0-9A-Za-z-]+}")
                    .hasAnyAuthority(ASSIGN_CONSULTANT_TO_ENQUIRY, ASSIGN_CONSULTANT_TO_SESSION)
                    .requestMatchers("/users/consultants")
                    .hasAuthority(VIEW_AGENCY_CONSULTANTS)
                    .requestMatchers(
                        "/users/consultants/import",
                        "/users/askers/import",
                        "/users/askersWithoutSession/import",
                        "/users/sessions/rocketChatGroupId")
                    .hasAuthority(TECHNICAL_DEFAULT)
                    .requestMatchers("/liveproxy/send")
                    .hasAnyAuthority(USER_DEFAULT, CONSULTANT_DEFAULT, ANONYMOUS_DEFAULT)
                    .requestMatchers("/users/mails/messages/feedback/new")
                    .hasAuthority(USE_FEEDBACK)
                    .requestMatchers("/users/messages/key")
                    .hasAuthority(TECHNICAL_DEFAULT)
                    .requestMatchers("/users/chat/new", "/users/chat/v2/new")
                    .hasAuthority(CREATE_NEW_CHAT)
                    .requestMatchers("/users/chat/{chatId:[0-9]+}/start")
                    .hasAuthority(START_CHAT)
                    .requestMatchers("/users/chat/{chatId:[0-9]+}/stop")
                    .hasAuthority(STOP_CHAT)
                    .requestMatchers(
                        "/users/chat/{chatId:[0-9]+}/update",
                        "/users/{chatUserId:[0-9A-Za-z]+}/chat/{chatId:[0-9]+}/ban")
                    .hasAuthority(UPDATE_CHAT)
                    .requestMatchers("/useradmin/tenantadmins/", "/useradmin/tenantadmins/**")
                    .hasAuthority(TENANT_ADMIN)
                    .requestMatchers("/useradmin/data/*")
                    .hasAnyAuthority(SINGLE_TENANT_ADMIN, RESTRICTED_AGENCY_ADMIN)
                    .requestMatchers(HttpMethod.POST, "/useradmin/consultants/")
                    .hasAnyAuthority(CONSULTANT_CREATE, TECHNICAL_DEFAULT)
                    .requestMatchers(
                        HttpMethod.PUT,
                        "/useradmin/consultants/{consultantId:" + UUID_PATTERN + "}")
                    .hasAnyAuthority(CONSULTANT_UPDATE, TECHNICAL_DEFAULT)
                    .requestMatchers(
                        HttpMethod.PUT,
                        "/useradmin/consultants/{consultantId:" + UUID_PATTERN + "}/agencies")
                    .hasAnyAuthority(CONSULTANT_UPDATE, TECHNICAL_DEFAULT)
                    .requestMatchers("/useradmin/2fa/{consultantId:" + UUID_PATTERN + "}")
                    .hasAnyAuthority(
                        USER_ADMIN,
                        SINGLE_TENANT_ADMIN,
                        TENANT_ADMIN,
                        RESTRICTED_AGENCY_ADMIN,
                        CONSULTANT_CREATE)
                    .requestMatchers("/useradmin", "/useradmin/**")
                    .hasAnyAuthority(USER_ADMIN, TECHNICAL_DEFAULT)
                    .requestMatchers("/users/consultants/search")
                    .hasAnyAuthority(USER_ADMIN, TECHNICAL_DEFAULT)
                    .requestMatchers(
                        "/users/consultants/sessions/{sessionId:[0-9]+}",
                        "/users/sessions/{sessionId:[0-9]+}/archive",
                        "/users/sessions/{sessionId:[0-9]+}")
                    .hasAnyAuthority(CONSULTANT_DEFAULT)
                    .requestMatchers("/appointments")
                    .hasAnyAuthority(CONSULTANT_DEFAULT, TECHNICAL_DEFAULT)
                    .requestMatchers("/appointments/booking/{id:[0-9]+}")
                    .hasAnyAuthority(CONSULTANT_DEFAULT, TECHNICAL_DEFAULT)
                    .requestMatchers(
                        HttpMethod.PUT, APPOINTMENTS_APPOINTMENT_ID + UUID_PATTERN + "}")
                    .hasAuthority(CONSULTANT_DEFAULT)
                    .requestMatchers(
                        HttpMethod.DELETE, APPOINTMENTS_APPOINTMENT_ID + UUID_PATTERN + "}")
                    .hasAuthority(CONSULTANT_DEFAULT)
                    .requestMatchers(
                        "/users/sessions/{sessionId:[0-9]+}/dearchive", "/users/mails/reassignment")
                    .hasAnyAuthority(USER_DEFAULT, CONSULTANT_DEFAULT)
                    .requestMatchers("/userstatistics", "/userstatistics/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.DELETE, "/useradmin/consultants/{consultantId:[0-9]+}/delete")
                    .hasAnyAuthority(USER_ADMIN, RESTRICTED_AGENCY_ADMIN)
                    .requestMatchers(HttpMethod.GET, "/actuator/health")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/actuator/health/*")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/users/{username}")
                    .permitAll()
                    .anyRequest()
                    .denyAll());

    httpSecurity.oauth2ResourceServer(
        server -> server.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));
    return httpSecurity.build();
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
          httpSecurity.addFilterAfter(this.tenantFilter, FilterSecurityInterceptor.class);
    }
    return httpSecurity;
  }

  /**
   * Configure trailing slash match for all endpoints (needed as Spring Boot 3.0.0 changed default
   * behaviour for trailing slash match) https://www.baeldung.com/spring-boot-3-migration (section
   * 3.1)
   */
  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.setUseTrailingSlashMatch(true);
  }

  @Bean
  JwtAuthConverter jwtAuthConverter() {
    return new JwtAuthConverter(jwtAuthConverterProperties, authorisationService);
  }
}
