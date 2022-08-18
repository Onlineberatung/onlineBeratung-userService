package de.caritas.cob.userservice.api.adapters.keycloak.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@Validated
@ConfigurationProperties(prefix = "keycloak.config")
public class KeycloakCustomConfig {

  @NotBlank private String adminUsername;

  @NotBlank private String adminPassword;

  @NotBlank private String adminClientId;

  @NotBlank private String appClientId;
}
