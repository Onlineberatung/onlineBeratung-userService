package de.caritas.cob.userservice.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.model.UserDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.sessionData.SessionData;
import de.caritas.cob.userservice.api.repository.sessionData.SessionDataRepository;

/**
 * Service for session data
 */
@Service
public class SessionDataService {

  private final SessionDataRepository sessionDataRepository;
  private final SessionDataHelper sessionDataHelper;

  @Autowired
  public SessionDataService(SessionDataRepository sessionDataRepository,
      SessionDataHelper sessionDataHelper) {
    this.sessionDataRepository = sessionDataRepository;
    this.sessionDataHelper = sessionDataHelper;
  }

  /**
   * Save additional registration information in session data.
   * 
   * @param session {@link Session}
   * @param user {@link UserDTO}
   * @return list of {@link SessionData}
   * @throws ServiceException when saving session data fails
   */
  public Iterable<SessionData> saveSessionDataFromRegistration(Session session, UserDTO user)
      throws ServiceException {

    List<SessionData> sessionDataList =
        sessionDataHelper.createRegistrationSessionDataList(session, user);
    try {
      return sessionDataRepository.saveAll(sessionDataList);
    } catch (DataAccessException | IllegalArgumentException ex) {
      LogService.logDatabaseError(ex);
      throw new ServiceException(String.format(
          "Database error while saving session data during registration for session %s and user %s",
          session.toString(), user.toString()), ex);
    }

  }

  /**
   * Returns the session for the provided sessionId
   * 
   * @param sessionId
   * @return {@link Session}
   */
  public List<SessionData> getSessionData(Long sessionId) {
    List<SessionData> sessionDataList;

    try {
      sessionDataList = sessionDataRepository.findBySessionId(sessionId);
    } catch (DataAccessException ex) {
      LogService.logDatabaseError(ex);
      throw new ServiceException(String
          .format("Database error while retrieving session data for session id %s", sessionId));
    }

    return sessionDataList;
  }

}
