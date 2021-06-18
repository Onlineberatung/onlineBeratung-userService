package de.caritas.cob.userservice.api.helper;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.SessionDataInitializing;
import de.caritas.cob.userservice.api.model.SessionDataDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionData;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataKeyRegistration;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Provider for {@link SessionData}.
 */
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

    session.getSessionData().forEach(sessionData -> {
      if (SessionDataKeyRegistration.containsKey(sessionData.getKey())) {
        sessionDataMap.put(sessionData.getKey(), sessionData.getValue());
      }
    });

    return sessionDataMap;
  }

  /**
   * Get list of session data items for session.
   *
   * @param session     the {@link Session}
   * @param sessionData the {@link SessionDataDTO}
   * @return the list of session data items
   */
  public List<SessionData> createSessionDataList(Session session,
      SessionDataDTO sessionData) {

    List<SessionData> sessionDataList = new ArrayList<>();
    if (getSessionDataInitializing(session.getConsultingTypeId()).isAddictiveDrugs()) {
      sessionDataList.add(obtainSessionData(session,
          SessionDataKeyRegistration.ADDICTIVE_DRUGS.getValue(),
          getAddictiveDrugsValue(sessionData)));
    }
    if (getSessionDataInitializing(session.getConsultingTypeId()).isAge()) {
      sessionDataList.add(obtainSessionData(session,
          SessionDataKeyRegistration.AGE.getValue(), getAgeValue(sessionData)));
    }
    if (getSessionDataInitializing(session.getConsultingTypeId()).isGender()) {
      sessionDataList.add(obtainSessionData(session,
          SessionDataKeyRegistration.GENDER.getValue(), getGenderValue(sessionData)));
    }
    if (getSessionDataInitializing(session.getConsultingTypeId()).isRelation()) {
      sessionDataList.add(obtainSessionData(session,
          SessionDataKeyRegistration.RELATION.getValue(), getRelationValue(sessionData)));
    }
    if (getSessionDataInitializing(session.getConsultingTypeId()).isState()) {
      sessionDataList.add(obtainSessionData(session,
          SessionDataKeyRegistration.STATE.getValue(), getStateValue(sessionData)));
    }
    return sessionDataList;
  }

  private SessionDataInitializing getSessionDataInitializing(int consultingTypeId) {
    return SessionDataInitializing.convertSessionDataInitializingDTOtoSessionDataInitializing(
        Objects.requireNonNull(consultingTypeManager.getConsultingTypeSettings(consultingTypeId)
            .getSessionDataInitializing()));
  }

  private SessionData obtainSessionData(Session session, String key, String value) {
    return nonNull(session.getSessionData())
        ? obtainUpdatedOrInitialSessionData(session, key, value)
        : obtainInitialSessionData(session, key, value);
  }

  private SessionData obtainUpdatedOrInitialSessionData(Session session, String key, String value) {
    var sessionData = session.getSessionData()
        .stream()
        .filter(data -> data.getKey().equals(key))
        .findFirst()
        .orElse(obtainInitialSessionData(session, key, value));
    sessionData.setValue(nonNull(value) ? value : sessionData.getValue());

    return sessionData;
  }

  private SessionData obtainInitialSessionData(Session session, String key, String value) {
    return new SessionData(session, SessionDataType.REGISTRATION, key, value);
  }

  private String getAddictiveDrugsValue(SessionDataDTO sessionData) {
    return isEmpty(sessionData.getAddictiveDrugs()) ? null : sessionData.getAddictiveDrugs();
  }

  private String getAgeValue(SessionDataDTO sessionData) {
    return isEmpty(sessionData.getAge()) ? null : sessionData.getAge();
  }

  private String getGenderValue(SessionDataDTO sessionData) {
    return isEmpty(sessionData.getGender()) ? null : sessionData.getGender();
  }

  private String getRelationValue(SessionDataDTO sessionData) {
    return isEmpty(sessionData.getRelation()) ? null : sessionData.getRelation();
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
    return (SessionDataDTO) new SessionDataDTO()
        .age(userDTO.getAge())
        .state(userDTO.getState());
  }
}
