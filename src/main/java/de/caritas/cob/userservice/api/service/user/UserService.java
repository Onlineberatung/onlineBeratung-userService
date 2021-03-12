package de.caritas.cob.userservice.api.service.user;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final @NonNull UserRepository userRepository;

  /**
   * Deletes an user.
   *
   * @param user the user to be deleted
   */
  public void deleteUser(User user) {
    userRepository.delete(user);
  }

  /**
   * Creates a new {@link User}.
   *
   * @param userId         the new user id
   * @param username       the name for the user
   * @param email          the email of the user
   * @param languageFormal flag for language formal
   * @return The created {@link User}
   */
  public User createUser(String userId, String username, String email, boolean languageFormal) {
    return createUser(userId, null, username, email, languageFormal);
  }

  /**
   * Creates a new {@link User}.
   *
   * @param userId         the new user id
   * @param oldId          an optional old user id
   * @param username       the name for the user
   * @param email          the email of the user
   * @param languageFormal flag for language formal
   * @return The created {@link User}
   */
  public User createUser(String userId, Long oldId, String username, String email,
      boolean languageFormal) {
    return userRepository.save(new User(userId, oldId, username, email, languageFormal));
  }

  /**
   * Loads an {@link User}.
   *
   * @param userId the id of the user to search for
   * @return An {@link Optional} with the {@link User}, if found
   */
  public Optional<User> getUser(String userId) {
    return userRepository.findByUserIdAndDeleteDateIsNull(userId);
  }

  /**
   * Saves an {@link User} to the database.
   *
   * @param user the {@link User} to save
   * @return the saved {@link User}
   */
  public User saveUser(User user) {
    return userRepository.save(user);
  }

  /**
   * Finds an user via the {@link AuthenticatedUser}.
   *
   * @return Optional of user
   */
  public Optional<User> getUserViaAuthenticatedUser(AuthenticatedUser authenticatedUser) {

    Optional<User> userOptional = getUser(authenticatedUser.getUserId());

    if (!userOptional.isPresent()) {
      throw new InternalServerErrorException(
          String.format("User with id %s not found.", authenticatedUser.getUserId()));
    }

    return userOptional;

  }

  /**
   * Finds an user by the given rocket chat user id.
   *
   * @param rcUserId the rocket chat user id to search for
   * @return the user as an {@link Optional}
   */
  public Optional<User> findUserByRcUserId(String rcUserId) {
    return userRepository.findByRcUserIdAndDeleteDateIsNull(rcUserId);
  }

  /**
   * Updates/sets the user's Rocket.Chat ID in MariaDB if not already set.
   *
   * @param user {@link User}
   * @param rcUserId Rocket.Chat user ID
   */
  public void updateRocketChatIdInDatabase(User user, String rcUserId) {
    if (nonNull(user) && isEmpty(user.getRcUserId())) {
      user.setRcUserId(rcUserId);
      saveUser(user);
    }
  }
}
