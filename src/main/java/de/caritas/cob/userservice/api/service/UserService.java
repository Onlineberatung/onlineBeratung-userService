package de.caritas.cob.userservice.api.service;

import java.util.Optional;
import javax.ws.rs.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.exception.SaveUserException;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;

@Service
public class UserService {

  private final LogService logService;
  private final UserRepository userRepository;

  @Autowired
  public UserService(LogService logService, UserRepository userRepository) {
    this.logService = logService;
    this.userRepository = userRepository;
  }

  public void deleteUser(User user) {
    try {
      userRepository.delete(user);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException("Deletion of user failed");
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
  public User createUser(String userId, String username, String email, boolean languageFormal)
      throws ServiceException {
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
   * @throws ServiceException
   */
  public User createUser(String userId, Long oldId, String username, String email,
      boolean languageFormal) throws ServiceException {
    try {
      return userRepository.save(new User(userId, oldId, username, email, languageFormal));
    } catch (DataAccessException ex) {
      throw new ServiceException("Creation of user failed", ex);
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
      throw new ServiceException("Database error while loading user", ex);
    }
  }

  /**
   * Saves an {@link User} to the database
   * 
   * @param user
   * @return
   */
  public User saveUser(User user) {
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
  public Optional<User> getUserViaAuthenticatedUser(AuthenticatedUser authenticatedUser)
      throws InternalServerErrorException {

    Optional<User> userOptional = getUser(authenticatedUser.getUserId());

    if (!userOptional.isPresent()) {
      throw new InternalServerErrorException(
          String.format("User with id %s not found.", authenticatedUser.getUserId()));
    }

    return userOptional;

  }

}
