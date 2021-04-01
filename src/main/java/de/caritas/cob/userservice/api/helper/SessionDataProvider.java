package de.caritas.cob.userservice.api.helper;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.SessionDataInitializing;
import de.caritas.cob.userservice.api.model.SessionDataDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionData;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataKeyRegistration;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper class for {@link SessionData}.
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

    if (session.getSessionData() != null) {
      for (SessionData sessionData : session.getSessionData()) {
        if (SessionDataKeyRegistration.containsKey(sessionData.getKey())) {
          sessionDataMap.put(sessionData.getKey(), sessionData.getValue());
        }
      }
    }

    return sessionDataMap;

  }

  /**
   * Get list of session data items for session.
   *
   * @param session       the {@link Session}
   * @param sessionData   the {@link SessionDataDTO}
   * @return the list of session data items
   */
  public List<SessionData> createInitialSessionDataList(Session session,
      SessionDataDTO sessionData) {

    List<SessionData> sessionDataList = new ArrayList<>();
    if (getSessionDataInitializing(session.getConsultingType()).isAddictiveDrugs()) {
      sessionDataList.add(createRegistrationSessionData(session,
          SessionDataKeyRegistration.ADDICTIVE_DRUGS.getValue(),
          getAddictiveDrugsValue(sessionData)));
    }
    if (getSessionDataInitializing(session.getConsultingType()).isAge()) {
      sessionDataList.add(createRegistrationSessionData(session,
          SessionDataKeyRegistration.AGE.getValue(), getAgeValue(sessionData)));
    }
    if (getSessionDataInitializing(session.getConsultingType()).isGender()) {
      sessionDataList.add(createRegistrationSessionData(session,
          SessionDataKeyRegistration.GENDER.getValue(), getGenderValue(sessionData)));
    }
    if (getSessionDataInitializing(session.getConsultingType()).isRelation()) {
      sessionDataList.add(createRegistrationSessionData(session,
          SessionDataKeyRegistration.RELATION.getValue(), getRelationValue(sessionData)));
    }
    if (getSessionDataInitializing(session.getConsultingType()).isState()) {
      sessionDataList.add(createRegistrationSessionData(session,
          SessionDataKeyRegistration.STATE.getValue(), getStateValue(sessionData)));
    }
    return sessionDataList;
  }

  private SessionDataInitializing getSessionDataInitializing(ConsultingType consultingType) {
    return consultingTypeManager.getConsultingTypeSettings(consultingType)
        .getSessionDataInitializing();

  }

  private SessionData createRegistrationSessionData(Session session, String key, String value) {
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


}
