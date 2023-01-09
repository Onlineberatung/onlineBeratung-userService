package de.caritas.cob.userservice.api.admin.facade;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.AskerResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Wrapper facade to provide admin operations on asker accounts. */
@Service
@RequiredArgsConstructor
public class AskerUserAdminFacade {

  private final @NonNull IdentityClient identityClient;
  private final @NonNull UserService userService;
  private final @NonNull UsernameTranscoder usernameTranscoder;

  /**
   * Marks the asker with the given id for deletion.
   *
   * @param userId the id of the asker
   */
  public void markAskerForDeletion(String userId) {
    User user =
        userService
            .getUser(userId)
            .orElseThrow(() -> new NotFoundException("Asker with id %s does not exist", userId));

    if (nonNull(user.getDeleteDate())) {
      throw new ConflictException(
          String.format("Asker with id %s is already marked for deletion", userId));
    }

    this.identityClient.deactivateUser(userId);
    user.setDeleteDate(nowInUtc());
    this.userService.saveUser(user);
  }

  public AskerResponseDTO getAsker(String userId) {
    User user =
        userService
            .getUser(userId)
            .orElseThrow(() -> new NotFoundException("Asker with id %s does not exist", userId));
    AskerResponseDTO asker = new AskerResponseDTO();
    asker.setId(user.getUserId());
    asker.setUsername(this.usernameTranscoder.decodeUsername(user.getUsername()));
    asker.setEmail(user.getEmail());
    return asker;
  }
}
