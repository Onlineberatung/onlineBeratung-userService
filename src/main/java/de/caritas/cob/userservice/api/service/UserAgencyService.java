package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAgencyService {

  private final @NonNull UserAgencyRepository userAgencyRepository;

  /**
   * Save a {@link UserAgency} to the database.
   *
   * @param userAgency {@link UserAgency}
   * @return the updated/created {@link UserAgency}
   */
  public UserAgency saveUserAgency(UserAgency userAgency) {
    try {
      return userAgencyRepository.save(userAgency);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          "Database error while saving user agency", LogService::logDatabaseError);
    }
  }

  /**
   * Get a list of the user agencies.
   *
   * @param user {@link User}
   * @return a list with the user agencies
   */
  public List<UserAgency> getUserAgenciesByUser(User user) {
    try {
      return userAgencyRepository.findByUser(user);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(
          "Database error while retrieving user agencies", LogService::logDatabaseError);
    }
  }

  /**
   * Deletes an {@link UserAgency} relation from database.
   *
   * @param userAgency {@link UserAgency}
   */
  public void deleteUserAgency(UserAgency userAgency) {
    try {
      userAgencyRepository.delete(userAgency);

    } catch (DataAccessException | IllegalArgumentException ex) {
      throw new InternalServerErrorException(
          "Database error while saving user agency", LogService::logDatabaseError);
    }
  }
}
