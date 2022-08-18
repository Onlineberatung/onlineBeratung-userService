package de.caritas.cob.userservice.api.config;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "videochat")
public class VideoChatConfig {

  @NotNull private Boolean e2eEncryptionEnabled;
}
