package de.caritas.cob.userservice.api.service.user;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetUserIdException;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class to enrich user data.
 */
@Service
@RequiredArgsConstructor
public class UserEnricher {

  private final @NonNull RocketChatService rocketChatService;

  /**
   *  Eriches an {@link User} instance with Rocket.Chat id.
   *
   * @param user an {@link Optional} of a user instance
   * @return an {@link Optional} of a user instance enriched with Rocket.Chat id.
   */
  public Optional<User> enrichUserWithRocketChatId(Optional<User> user) {
    return user.map(this::getUser);
  }

  private User getUser(User user) {
    try {
      String rocketChatId = rocketChatService.getRocketChatUserIdByUsername(user.getUsername());
      user.setRcUserId(rocketChatId);
      return user;
    } catch (RocketChatGetUserIdException ex) {
      throw new InternalServerErrorException(
          String.format("Could not obtain Rocket.Chat id via Rocket.Chat api for username %s",
              user.getUsername()), ex);
    }
  }

}
