package de.caritas.cob.userservice.api.deleteworkflow.action;

import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Action to delete a user account in keycloak.
 */
@Component
@RequiredArgsConstructor
public abstract class DeleteKeycloakUserAction {

  protected static final String ERROR_REASON = "Unable to delete keycloak user account";

  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;

  protected void deleteUserWithId(String userId) {
    this.keycloakAdminClientService.deleteUser(userId);
  }

}
