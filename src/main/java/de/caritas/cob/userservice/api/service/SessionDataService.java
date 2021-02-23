package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.helper.SessionDataHelper;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionData;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataRepository;

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
   */
  public Iterable<SessionData> saveSessionDataFromRegistration(Session session, UserDTO user) {

    List<SessionData> sessionDataList =
        sessionDataHelper.createRegistrationSessionDataList(session, user);
    try {
      return sessionDataRepository.saveAll(sessionDataList);
    } catch (DataAccessException | IllegalArgumentException ex) {
      String message = String.format(
          "Database error while saving session data during registration for session %s and user %s",
          session.toString(), user.toString());
      throw new InternalServerErrorException(message, LogService::logDatabaseError);
    }

  }

  /**
   * Returns the session for the provided sessionId
   * 
   * @param sessionId
   * @return {@link Session}
   */
  public List<SessionData> getSessionData(Long sessionId) {
    try {
      return sessionDataRepository.findBySessionId(sessionId);
    } catch (DataAccessException ex) {
      throw new InternalServerErrorException(String
          .format("Database error while retrieving session data for session id %s", sessionId),
          LogService::logDatabaseError);
    }
  }

}
