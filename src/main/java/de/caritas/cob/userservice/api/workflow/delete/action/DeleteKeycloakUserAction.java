package de.caritas.cob.userservice.api.workflow.delete.action;

import de.caritas.cob.userservice.api.port.out.IdentityClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Action to delete a user account in keycloak. */
@Component
@RequiredArgsConstructor
public abstract class DeleteKeycloakUserAction {

  protected static final String ERROR_REASON = "Unable to delete keycloak user account";

  private final @NonNull IdentityClient identityClient;

  protected void deleteUserWithId(String userId) {
    identityClient.deleteUser(userId);
  }
}
