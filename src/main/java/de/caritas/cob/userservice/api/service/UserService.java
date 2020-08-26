package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.exception.SaveUserException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;

@Service
public class UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void deleteUser(User user) {
    try {
      userRepository.delete(user);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new InternalServerErrorException("Deletion of user failed");
    }
  }

  /**
   * Create a new {@link User}
   * 
   * @param userId
   * @param username
   * @param email
   * @return The created {@link User}
   */
  public User createUser(String userId, String username, String email, boolean languageFormal) {
    return createUser(userId, null, username, email, languageFormal);
  }

  /**
   * Creates a new {@link User}
   * 
   * @param userId
   * @param oldId
   * @param username
   * @param email
   * @return The created {@link User}
   */
  public User createUser(String userId, Long oldId, String username, String email,
      boolean languageFormal) {
    try {
      return userRepository.save(new User(userId, oldId, username, email, languageFormal));
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(ex.getMessage(), LogService::logInternalServerError);
    }
  }

  /**
   * Load a {@link User}
   * 
   * @param userId
   * @return An {@link Optional} with the {@link User}, if found
   */
  public Optional<User> getUser(String userId) {
    try {
      return userRepository.findByUserId(userId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(ex.getMessage(), LogService::logInternalServerError);
    }
  }

  /**
   * Saves an {@link User} to the database
   * 
   * @param user
   * @return
   */
  public User saveUser(User user) throws SaveUserException {
    try {
      return userRepository.save(user);
    } catch (DataAccessException ex) {
      throw new SaveUserException("Database error while saving user.", ex);
    }
  }

  /**
   * Find a consultant via the {@link AuthenticatedUser}
   * 
   * @param authenticatedUser
   * @return Optional of user
   * @throws {@link InternalServerErrorException}
   */
  public Optional<User> getUserViaAuthenticatedUser(AuthenticatedUser authenticatedUser) {

    Optional<User> userOptional = getUser(authenticatedUser.getUserId());

    if (!userOptional.isPresent()) {
      throw new InternalServerErrorException(
          String.format("User with id %s not found.", authenticatedUser.getUserId()));
    }

    return userOptional;

  }

}
