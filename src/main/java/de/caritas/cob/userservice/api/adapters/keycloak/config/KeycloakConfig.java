package de.caritas.cob.userservice.api.adapters.keycloak.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

  @Bean
  public KeycloakRestTemplate createKeycloakRestTemplate(
      RestTemplateBuilder restTemplateBuilder) {
    var restTemplate = restTemplateBuilder.build();

    return new KeycloakRestTemplate(restTemplate);
  }
}
