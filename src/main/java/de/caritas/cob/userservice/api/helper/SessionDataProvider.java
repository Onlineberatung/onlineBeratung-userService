package de.caritas.cob.userservice.api.helper;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionDataDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.SessionDataInitializing;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.SessionData;
import de.caritas.cob.userservice.api.model.SessionData.SessionDataType;
import java.util.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Provider for {@link SessionData}. */
@Component
@RequiredArgsConstructor
public class SessionDataProvider {

  private final @NonNull ConsultingTypeManager consultingTypeManager;

  /**
   * Get map of session data items from registration.
   *
   * @param session {@link Session}
   * @return a map with registration session data items of the session
   */
  public Map<String, Object> getSessionDataMapFromSession(Session session) {
    Map<String, Object> sessionDataMap = new LinkedHashMap<>();

    session
        .getSessionData()
        .forEach(
            sessionData -> {
              if (SessionDataKeyRegistration.containsKey(sessionData.getKey())) {
                sessionDataMap.put(sessionData.getKey(), sessionData.getValue());
              }
            });

    return sessionDataMap;
  }

  /**
   * Get list of session data items for session.
   *
   * @param session the {@link Session}
   * @param sessionData the {@link SessionDataDTO}
   * @return the list of session data items
   */
  public List<SessionData> createSessionDataList(Session session, SessionDataDTO sessionData) {

    List<SessionData> sessionDataList = new ArrayList<>();
    if (getSessionDataInitializing(session.getConsultingTypeId()).isAge()) {
      sessionDataList.add(
          obtainSessionData(
              session, SessionDataKeyRegistration.AGE.getValue(), getAgeValue(sessionData)));
    }
    if (getSessionDataInitializing(session.getConsultingTypeId()).isState()) {
      sessionDataList.add(
          obtainSessionData(
              session, SessionDataKeyRegistration.STATE.getValue(), getStateValue(sessionData)));
    }
    return sessionDataList;
  }

  private SessionDataInitializing getSessionDataInitializing(int consultingTypeId) {
    return SessionDataInitializing.convertSessionDataInitializingDTOtoSessionDataInitializing(
        Objects.requireNonNull(
            consultingTypeManager
                .getConsultingTypeSettings(consultingTypeId)
                .getSessionDataInitializing()));
  }

  private SessionData obtainSessionData(Session session, String key, String value) {
    return nonNull(session.getSessionData())
        ? obtainUpdatedOrInitialSessionData(session, key, value)
        : obtainInitialSessionData(session, key, value);
  }

  private SessionData obtainUpdatedOrInitialSessionData(Session session, String key, String value) {
    var sessionData =
        session.getSessionData().stream()
            .filter(data -> data.getKey().equals(key))
            .findFirst()
            .orElse(obtainInitialSessionData(session, key, value));
    sessionData.setValue(nonNull(value) ? value : sessionData.getValue());

    return sessionData;
  }

  private SessionData obtainInitialSessionData(Session session, String key, String value) {
    return new SessionData(session, SessionDataType.REGISTRATION, key, value);
  }

  private String getAgeValue(SessionDataDTO sessionData) {
    return isEmpty(sessionData.getAge()) ? null : sessionData.getAge();
  }

  private String getStateValue(SessionDataDTO sessionData) {
    return isEmpty(sessionData.getState()) ? null : sessionData.getState();
  }

  /**
   * Returns the {@link SessionDataDTO} for the given {@link UserDTO}.
   *
   * @param userDTO {@link UserDTO}
   * @return {@link SessionDataDTO}
   */
  public static SessionDataDTO fromUserDTO(UserDTO userDTO) {
    return new SessionDataDTO().age(String.valueOf(userDTO.getUserAge())).state(userDTO.getState());
  }
}
