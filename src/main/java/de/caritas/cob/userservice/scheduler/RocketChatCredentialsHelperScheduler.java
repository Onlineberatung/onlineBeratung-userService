package de.caritas.cob.userservice.scheduler;

import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!testing")
public class RocketChatCredentialsHelperScheduler {

  @Autowired
  private RocketChatCredentialsProvider rcCredentialsHelper;

  @PostConstruct
  public void postConstructInitializer() {
    LogService.logDebug("RocketChatCredentialsHelperScheduler - initialize tokens");
    try {
      rcCredentialsHelper.updateCredentials();
    } catch (RocketChatLoginException e) {
      LogService.logUnauthorized(e.getMessage());
    }
  }

  @Scheduled(cron = "${rocket.credentialscheduler.cron}")
  public void scheduledRotateToken() {
    LogService.logDebug("RocketChatCredentialsHelperScheduler - rotating tokens");
    try {
      rcCredentialsHelper.updateCredentials();
    } catch (RocketChatLoginException e) {
      LogService.logUnauthorized(e.getMessage());
    }
  }

}
