package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionDataDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.SessionDataProvider;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.SessionData;
import de.caritas.cob.userservice.api.port.out.SessionDataRepository;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Service for session data. */
@Service
@RequiredArgsConstructor
public class SessionDataService {

  private final @NonNull SessionDataRepository sessionDataRepository;
  private final @NonNull SessionDataProvider sessionDataProvider;
  private final @NonNull SessionService sessionService;

  /**
   * Saves additional registration information in session data for the given session ID.
   *
   * @param sessionId the session ID
   * @param sessionData {@link SessionData}
   */
  public void saveSessionData(Long sessionId, SessionDataDTO sessionData) {
    Session session =
        sessionService
            .getSession(sessionId)
            .orElseThrow(() -> new NotFoundException("Session with id %s not found.", sessionId));
    this.saveSessionData(session, sessionData);
  }

  /**
   * Saves additional registration information in session data for the given session.
   *
   * @param session the {@link Session}
   * @param sessionData {@link SessionData}
   */
  public void saveSessionData(Session session, SessionDataDTO sessionData) {
    List<SessionData> sessionDataList =
        sessionDataProvider.createSessionDataList(session, sessionData);

    sessionDataRepository.saveAll(sessionDataList);
  }
}
