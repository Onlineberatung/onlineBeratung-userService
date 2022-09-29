package de.caritas.cob.userservice.api.config.auth;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "identity.technical-user")
public class TechnicalUserConfig {

  @NotBlank private String username;

  @NotBlank private String password;
}
