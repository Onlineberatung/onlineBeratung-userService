package de.caritas.cob.UserService.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.helper.SessionDataHelper;
import de.caritas.cob.UserService.api.model.UserDTO;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.sessionData.SessionData;
import de.caritas.cob.UserService.api.repository.sessionData.SessionDataRepository;

/**
 * Service for session data
 */
@Service
public class SessionDataService {

  private final SessionDataRepository sessionDataRepository;
  private final LogService logService;
  private final SessionDataHelper sessionDataHelper;

  @Autowired
  public SessionDataService(SessionDataRepository sessionDataRepository, LogService logService,
      SessionDataHelper sessionDataHelper) {
    this.sessionDataRepository = sessionDataRepository;
    this.logService = logService;
    this.sessionDataHelper = sessionDataHelper;
  }

  /**
   * Save additional registration information in session data
   * 
   * @param session the {@link Session}
   * @param user the {@Link UserDTO}
   */
  public Iterable<SessionData> saveSessionDataFromRegistration(Session session, UserDTO user) {

    List<SessionData> sessionDataList =
        sessionDataHelper.createRegistrationSessionDataList(session, user);
    try {
      return sessionDataRepository.saveAll(sessionDataList);
    } catch (DataAccessException ex) {
      logService.logDatabaseError(ex);
      throw new ServiceException("Database error while saving session data during registration");
    } catch (IllegalArgumentException illegalEx) {
      logService.logDatabaseError(illegalEx);
      throw new ServiceException("Database error while saving session data during registration");
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
      logService.logDatabaseError(ex);
      throw new ServiceException(String
          .format("Database error while retrieving session data for session id %s", sessionId));
    }

    return sessionDataList;
  }

}
