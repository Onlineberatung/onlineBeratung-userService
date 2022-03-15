package de.caritas.cob.userservice.api.adapters.keycloak.config;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

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
