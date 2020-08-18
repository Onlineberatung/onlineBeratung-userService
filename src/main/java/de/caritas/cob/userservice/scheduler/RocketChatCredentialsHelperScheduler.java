package de.caritas.cob.userservice.scheduler;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import de.caritas.cob.userservice.api.service.helper.RocketChatCredentialsHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!testing")
public class RocketChatCredentialsHelperScheduler {

  @Autowired
  private RocketChatCredentialsHelper rcCredentialsHelper;

  @PostConstruct
  public void postConstructInitializer() {
    log.debug("RocketChatCredentialsHelperScheduler - initialize tokens");
    rcCredentialsHelper.updateCredentials();
  }

  @Scheduled(cron = "${rocket.credentialscheduler.cron}")
  public void scheduledRotateToken() {
    log.debug("RocketChatCredentialsHelperScheduler - rotating tokens");
    rcCredentialsHelper.updateCredentials();
  }

}
