package de.caritas.cob.userservice.api.scheduler;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!testing")
@RequiredArgsConstructor
public class RocketChatCredentialsHelperScheduler {

  private final @NonNull RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @PostConstruct
  public void postConstructInitializer() {
    log.debug("RocketChatCredentialsHelperScheduler - initialize tokens");
    try {
      rocketChatCredentialsProvider.updateCredentials();
    } catch (RocketChatLoginException e) {
      log.warn("Unauthorized: {}", e.getMessage());
    }
  }

  @Scheduled(cron = "${rocket.credentialscheduler.cron}")
  public void scheduledRotateToken() {
    log.debug("RocketChatCredentialsHelperScheduler - rotating tokens");
    try {
      rocketChatCredentialsProvider.updateCredentials();
    } catch (RocketChatLoginException e) {
      log.warn("Unauthorized: {}", e.getMessage());
    }
  }

}
