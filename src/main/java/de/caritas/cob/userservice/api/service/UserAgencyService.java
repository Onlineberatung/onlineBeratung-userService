package de.caritas.cob.userservice.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.userAgency.UserAgency;
import de.caritas.cob.userservice.api.repository.userAgency.UserAgencyRepository;

@Service
public class UserAgencyService {

  @Autowired
  UserAgencyRepository userAgencyRepository;

  /**
   * Save a {@link UserAgency} to the database
   * 
   * @param userAgency
   * @return the {@link UserAgency}
   * 
   * @throws {@link ServiceException}
   */
  public UserAgency saveUserAgency(UserAgency userAgency) {
    try {
      return userAgencyRepository.save(userAgency);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new ServiceException("Database error while saving user agency");
    }
  }

  /**
   * Get a list of the user agencies
   * 
   * @param user
   * @return a list with the user agencies
   */
  public List<UserAgency> getUserAgenciesByUser(User user) {
    try {
      return userAgencyRepository.findByUser(user);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new ServiceException("Database error while retrieving user agencies");
    }
  }

  /**
   * Deletes an {@link UserAgency} relation from database
   * 
   * @param userAgency
   * 
   * @throws {@link ServiceException}
   */
  public void deleteUserAgency(UserAgency userAgency) {
    try {
      userAgencyRepository.delete(userAgency);

    } catch (DataAccessException dataAccessException) {
      throw new ServiceException("Database error while saving user agency", dataAccessException);

    } catch (IllegalArgumentException illArgException) {
      throw new ServiceException("Database error while saving user agency", illArgException);
    }
  }

}
