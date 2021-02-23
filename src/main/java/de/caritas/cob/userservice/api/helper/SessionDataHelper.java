package de.caritas.cob.userservice.api.helper;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.SessionDataInitializing;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Helper class for {@link SessionData}.
 */
@Component
@RequiredArgsConstructor
public class SessionDataHelper {

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
   * Get list of session data items for registration.
   *
   * @param session the {@link Session}
   * @param user    the {@link UserDTO}
   * @return the list of session data items for registration
   */
  public List<SessionData> createRegistrationSessionDataList(Session session, UserDTO user) {

    List<SessionData> sessionDataList = new ArrayList<>();
    if (getSessionDataInitializing(session.getConsultingType()).isAddictiveDrugs()) {
      sessionDataList.add(createSessionData(session, SessionDataType.REGISTRATION,
          SessionDataKeyRegistration.ADDICTIVE_DRUGS.getValue(),
          getAddictiveDrugsValueFromUser(user)));
    }
    if (getSessionDataInitializing(session.getConsultingType()).isAge()) {
      sessionDataList.add(createSessionData(session, SessionDataType.REGISTRATION,
          SessionDataKeyRegistration.AGE.getValue(), getAgeValueFromUser(user)));
    }
    if (getSessionDataInitializing(session.getConsultingType()).isGender()) {
      sessionDataList.add(createSessionData(session, SessionDataType.REGISTRATION,
          SessionDataKeyRegistration.GENDER.getValue(), getGenderValueFromUser(user)));
    }
    if (getSessionDataInitializing(session.getConsultingType()).isRelation()) {
      sessionDataList.add(createSessionData(session, SessionDataType.REGISTRATION,
          SessionDataKeyRegistration.RELATION.getValue(), getRelationValueFromUser(user)));
    }
    if (getSessionDataInitializing(session.getConsultingType()).isState()) {
      sessionDataList.add(createSessionData(session, SessionDataType.REGISTRATION,
          SessionDataKeyRegistration.STATE.getValue(), getStateValueFromUser(user)));
    }
    return sessionDataList;
  }

  private SessionDataInitializing getSessionDataInitializing(ConsultingType consultingType) {
    return consultingTypeManager.getConsultingTypeSettings(consultingType)
        .getSessionDataInitializing();

  }

  private SessionData createSessionData(Session session, SessionDataType sessionDataType,
      String key, String value) {
    return new SessionData(session, sessionDataType, key, value);
  }

  private String getAddictiveDrugsValueFromUser(UserDTO user) {
    return (user.getAddictiveDrugs() != null && !user.getAddictiveDrugs().equals(StringUtils.EMPTY))
        ? user.getAddictiveDrugs()
        : null;
  }

  private String getAgeValueFromUser(UserDTO user) {
    return (user.getAge() != null && !user.getAge().equals(StringUtils.EMPTY)) ? user.getAge()
        : null;
  }

  private String getGenderValueFromUser(UserDTO user) {
    return (user.getGender() != null && !user.getGender().equals(StringUtils.EMPTY))
        ? user.getGender()
        : null;
  }

  private String getRelationValueFromUser(UserDTO user) {
    return (user.getRelation() != null && !user.getRelation().equals(StringUtils.EMPTY))
        ? user.getRelation()
        : null;
  }

  private String getStateValueFromUser(UserDTO user) {
    return (user.getState() != null && !user.getState().equals(StringUtils.EMPTY)) ? user.getState()
        : null;
  }


}
