package de.caritas.cob.userservice.api.admin.facade;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Wrapper facade to provide admin operations on asker accounts.
 */
@Service
@RequiredArgsConstructor
public class UserAdminFacade {

  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull UserService userService;

  /**
   * Marks the asker with the given id for deletion.
   *
   * @param userId the id of the asker
   */
  public void markAskerForDeletion(String userId) {
    User user = userService.getUser(userId)
        .orElseThrow(() -> new NotFoundException(
            String.format("Asker with id %s does not exist", userId)));

    if (nonNull(user.getDeleteDate())) {
      throw new ConflictException(
          String.format("Asker with id %s is already marked for deletion", userId));
    }

    this.keycloakAdminClientService.deactivateUser(userId);
    user.setDeleteDate(nowInUtc());
    this.userService.saveUser(user);
  }

}
