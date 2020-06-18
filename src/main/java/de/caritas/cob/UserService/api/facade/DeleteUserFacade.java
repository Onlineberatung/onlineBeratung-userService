package de.caritas.cob.UserService.api.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.model.DeleteUserDTO;
import de.caritas.cob.UserService.api.service.KeycloakService;
import de.caritas.cob.UserService.api.service.LogService;

/**
 * Facade to encapsulate the steps for deleting an user.
 *
 */
@Service
public class DeleteUserFacade {
  private final KeycloakService keycloakService;
  private final LogService logService;

  @Autowired
  public DeleteUserFacade(KeycloakService keycloakService, LogService logService) {
    this.keycloakService = keycloakService;
    this.logService = logService;
  }

  /**
   * Delete an user.
   */
  public void deleteUser(DeleteUserDTO deleteUserDTO) {
    // TODO: check password
    // TODO: if correct password is entered, deactivate user in keycloak
  }
}
