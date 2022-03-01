package de.caritas.cob.userservice.api.actions.user;

import de.caritas.cob.userservice.api.actions.ActionCommand;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deactivates a user in Keycloak.
 */
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
    this.identityClient.deactivateUser(user.getUserId());
  }

}
