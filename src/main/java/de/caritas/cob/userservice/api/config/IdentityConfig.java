package de.caritas.cob.userservice.api.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "keycloak-api")
public class IdentityConfig {

  @NotBlank
  private String errorMessageDuplicatedEmail;

  @NotBlank
  private String errorMessageDuplicatedUsername;

  @URL
  private String loginUrl;

  @URL
  private String logoutUrl;

  @URL
  private String otpInfoUrl;

  @URL
  private String otpSetupUrl;

  @URL
  private String otpTeardownUrl;
}
