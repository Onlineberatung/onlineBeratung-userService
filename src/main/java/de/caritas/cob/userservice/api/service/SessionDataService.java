package de.caritas.cob.userservice.api.service;

import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.model.SessionDataDTO;
import de.caritas.cob.userservice.api.model.validation.MandatoryFieldsValidator;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import de.caritas.cob.userservice.api.helper.SessionDataProvider;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionData;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataRepository;

/**
 * Service for session data.
 */
@Service
@RequiredArgsConstructor
public class SessionDataService {

  private final @NonNull SessionDataRepository sessionDataRepository;
  private final @NonNull SessionDataProvider sessionDataProvider;
  private final @NonNull SessionService sessionService;
  private final @NonNull MandatoryFieldsValidator mandatoryFieldsValidator;

  /**
   * Saves additional registration information in session data.
   *
   * @param sessionId   the session ID
   * @param sessionData {@link SessionData}
   */
  public void saveSessionData(Long sessionId, SessionDataDTO sessionData) {
    Session session = sessionService.getSession(sessionId).orElseThrow(() -> new NotFoundException(
        String.format("Session with id %s not found for mandatory field check.", sessionId)));
    mandatoryFieldsValidator.validateFields(session.getConsultingType(), sessionData);
    List<SessionData> sessionDataList = this.buildSessionDataList(session, sessionData);

    sessionDataRepository.saveAll(sessionDataList);
  }

  private List<SessionData> buildSessionDataList(Session session, SessionDataDTO sessionData) {
    return sessionDataProvider.createInitialSessionDataList(session, sessionData);
  }

  private List<SessionData> buildUpdatedSessionDataList(Session session, SessionDataDTO sessionData) {
    session.getSessionData()
        .forEach(data -> data.setValue(obtainNewValue(data.getKey(), sessionData)));

    return session.getSessionData();
  }

  private String obtainNewValue(String key, SessionDataDTO sessionData) {
    try {
      Field f = sessionData.getClass().getDeclaredField(key);
      f.setAccessible(true);
      return f.get(sessionData).toString();
    } catch (Exception exception) {
      return "";
    }
  }
}
