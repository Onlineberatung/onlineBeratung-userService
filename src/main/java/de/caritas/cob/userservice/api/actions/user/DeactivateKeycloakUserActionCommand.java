package de.caritas.cob.userservice.api.actions.user;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Deactivates a user in Keycloak. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeactivateKeycloakUserActionCommand implements ActionCommand<User> {

  private final @NonNull IdentityClient identityClient;

  /**
   * Deactivates a user in Keycloak.
   *
   * @param user the user to deactivate in Keycloak.
   */
  @Override
  public void execute(User user) {
    try {
      this.identityClient.deactivateUser(user.getUserId());
    } catch (Exception e) {
      log.error("Unable to deactivate User with id {}", user.getUserId());
      log.error(getStackTrace(e));
    }
  }
}
