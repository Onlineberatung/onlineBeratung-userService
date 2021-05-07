package de.caritas.cob.userservice.api.service.user.anonymous;

import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.concurrent.atomic.AtomicLong;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Registry to generate, hold and handle all current anonymous usernames.
 */
@Component
@RequiredArgsConstructor
public class AnonymousUsernameRegistry {

  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull UserHelper userHelper;

  @Value("${anonymous.username.prefix}")
  private String usernamePrefix;

  private static final AtomicLong usernameIdCounter = new AtomicLong();

  /**
   * Generates an unique anonymous username.
   *
   * @return encoded unique anonymous username
   */
  public synchronized String generateUniqueUsername() {

    long usernameId;
    do {
      usernameId = usernameIdCounter.incrementAndGet();
    } while (isUsernameIdOccupied(usernameId));

    return userHelper.encodeUsername(generateUsername(usernameId));
  }

  private boolean isUsernameIdOccupied(long usernameId) {
    return !keycloakAdminClientService.isUsernameAvailable(generateUsername(usernameId));
  }

  private String generateUsername(long usernameId) {
    return usernamePrefix + usernameId;
  }

  /**
   * Resets the anonymous username ID registry.
   */
  @Scheduled(cron = "${anonymous.username.reset.cron}")
  public synchronized void cleanUpAnonymousUsernameRegistry() {
    usernameIdCounter.set(1L);
    LogService.logInfo("Anonymous username Ids have been reset!");
  }
}
