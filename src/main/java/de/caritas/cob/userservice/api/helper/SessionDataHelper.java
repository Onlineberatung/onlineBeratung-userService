package de.caritas.cob.userservice.api.helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.SessionDataInitializing;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.sessionData.SessionData;
import de.caritas.cob.userservice.api.repository.sessionData.SessionDataKeyRegistration;
import de.caritas.cob.userservice.api.repository.sessionData.SessionDataType;

/**
 * 
 * Helper class for {@link SessionData}
 *
 */
@Component
public class SessionDataHelper {

  private static ConsultingTypeManager consultingTypeManager;

  @Autowired
  public SessionDataHelper(ConsultingTypeManager consultingTypeManager) {
    SessionDataHelper.consultingTypeManager = consultingTypeManager;
  }

  /**
   * Get map of session data items from registration
   * 
   * @param session {@link Session}
   * @return a map with registration session data items of the session
   */
  public LinkedHashMap<String, Object> getSessionDataMapFromSession(Session session) {

    LinkedHashMap<String, Object> sessionDataMap = new LinkedHashMap<String, Object>();

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
   * Get list of session data items for registration
   * 
   * @param session the {@link Session}
   * @param user the {@link UserDTO}
   * @return the list of session data items for registration
   */
  public List<SessionData> createRegistrationSessionDataList(Session session, UserDTO user) {

    List<SessionData> sessionDataList = new ArrayList<SessionData>();
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

  public String getValueOfKey(List<SessionData> sessionDataList, String key) {

    if (sessionDataList != null) {
      SessionData sessionData =
          sessionDataList.stream().filter(data -> key.equals(data.getKey())).findAny().orElse(null);

      if (sessionData != null) {
        return sessionData.getValue();
      }
    }

    return null;
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
