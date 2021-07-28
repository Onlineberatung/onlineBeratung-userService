package de.caritas.cob.userservice.scheduler;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!testing")
@RequiredArgsConstructor
public class RocketChatCredentialsHelperScheduler {

  private final @NonNull RocketChatCredentialsProvider rocketChatCredentialsProvider;

  @PostConstruct
  public void postConstructInitializer() {
    LogService.logDebug("RocketChatCredentialsHelperScheduler - initialize tokens");
    try {
      rocketChatCredentialsProvider.updateCredentials();
    } catch (RocketChatLoginException e) {
      LogService.logUnauthorized(e.getMessage());
    }
  }

  @Scheduled(cron = "${rocket.credentialscheduler.cron}")
  public void scheduledRotateToken() {
    LogService.logDebug("RocketChatCredentialsHelperScheduler - rotating tokens");
    try {
      rocketChatCredentialsProvider.updateCredentials();
    } catch (RocketChatLoginException e) {
      LogService.logUnauthorized(e.getMessage());
    }
  }

}
