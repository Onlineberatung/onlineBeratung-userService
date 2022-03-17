package de.caritas.cob.userservice.api.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class VideoChatConfig {

  @Value("video.chat.e2e-encryption.enabled")
  private boolean isE2EEncryptionEnabled;

}
