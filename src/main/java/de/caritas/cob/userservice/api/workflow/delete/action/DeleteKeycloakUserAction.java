package de.caritas.cob.userservice.api.workflow.delete.action;

import de.caritas.cob.userservice.api.port.out.IdentityClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/** Action to delete a user account in keycloak. */
@Component
@RequiredArgsConstructor
@Slf4j
public abstract class DeleteKeycloakUserAction {

  protected static final String ERROR_REASON = "Unable to delete keycloak user account";

  private final @NonNull IdentityClient identityClient;

  protected void deleteUserWithId(String userId) {

    try {
      identityClient.deleteUser(userId);
    } catch (HttpClientErrorException ex) {
      acceptDeletionIfUserNotFoundInKeycloak(userId, ex);
    }
  }

  private void acceptDeletionIfUserNotFoundInKeycloak(String userId, HttpClientErrorException ex) {
    if (!HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
      throw ex;
    } else {
      log.warn(
          "No user with id {} could be found in keycloak, but proceeding with further actions.",
          userId);
    }
  }
}
