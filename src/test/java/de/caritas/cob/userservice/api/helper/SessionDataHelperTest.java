package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.testHelper.TestConstants.ADDICTIVE_DRUGS;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.testHelper.TestConstants.GENDER;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_TEAM_SESSION;
import static de.caritas.cob.userservice.testHelper.TestConstants.RELATION;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.STATE;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.manager.consultingtype.SessionDataInitializing;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionData;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataKeyRegistration;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataType;
import de.caritas.cob.userservice.api.repository.user.User;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionDataHelperTest {

  private SessionDataHelper sessionDataHelper;
  private ConsultingTypeManager consultingTypeManager;

  private final User USER = new User(USER_ID, null, USERNAME, EMAIL, false);
  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, USERNAME, ROCKETCHAT_ID,
      "first name", "last name", "consultant@cob.de", false, false, null, false, null, null, null,
      null, null, null);
  private final Session INITALIZED_SESSION_SUCHT = new Session(1L, USER, CONSULTANT,
      ConsultingType.SUCHT, "99999", 0L, SessionStatus.INITIAL, null, null, null, null, false,
      false, null, null);
  private final Session INITALIZED_SESSION_U25 = new Session(1L, USER, CONSULTANT,
      ConsultingType.U25, "99999", 0L, SessionStatus.INITIAL, null, null, null, null, false,
      false, null, null);
  private final SessionData SESSION_DATA_ADDICTIVE_DRUGS = new SessionData(new Session(),
      SessionDataType.REGISTRATION, SessionDataKeyRegistration.ADDICTIVE_DRUGS.getValue(), "1");
  private final SessionData SESSION_DATA_AGE = new SessionData(new Session(),
      SessionDataType.REGISTRATION, SessionDataKeyRegistration.AGE.getValue(), "2");
  private final SessionData SESSION_DATA_GENDER = new SessionData(new Session(),
      SessionDataType.REGISTRATION, SessionDataKeyRegistration.GENDER.getValue(), "3");
  private final List<SessionData> SESSION_DATA =
      Arrays.asList(SESSION_DATA_ADDICTIVE_DRUGS, SESSION_DATA_AGE, SESSION_DATA_GENDER);
  private final Session INITIALIZED_SESSION_WITH_SESSION_DATA = new Session(1L, USER, CONSULTANT,
      ConsultingType.SUCHT, "99999", 1L, SessionStatus.IN_PROGRESS, new Date(), null,
      null, SESSION_DATA, IS_TEAM_SESSION, IS_MONITORING, null, null);
  private final UserDTO USER_DTO_WITHOUT_SESSION_DATA = new UserDTO(USERNAME, "99999", 99L, "xyz",
      "x@y.de", null, null, null, null, null, "true", "0", true);
  private final UserDTO USER_DTO_WITH_SESSION_DATA = new UserDTO(USERNAME, "99999", 99L, "xyz",
      "x@y.de", ADDICTIVE_DRUGS, RELATION, AGE, GENDER, STATE, "true", "0", true);
  private final UserDTO USER_DTO_WITH_EMPTY_SESSION_DATA =
      new UserDTO(USERNAME, "99999", 99L, "xyz", "x@y.de", "", "", "", "", "", "true", "0", true);
  private final SessionDataInitializing SESSION_DATA_INITIALIZING_WITH_ALL_SESSION_DATA_ITEMS =
      new SessionDataInitializing(true, true, true, true, true);
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null,
          SESSION_DATA_INITIALIZING_WITH_ALL_SESSION_DATA_ITEMS, true, null, false, null, false,
          null, null);
  private final SessionDataInitializing SESSION_DATA_INITIALIZING_WITH_NO_SESSION_DATA_ITEMS =
      new SessionDataInitializing(false, false, false, false, false);
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_NO_SESSION_DATA_ITEMS =
      new ConsultingTypeSettings(ConsultingType.U25, false, null,
          SESSION_DATA_INITIALIZING_WITH_NO_SESSION_DATA_ITEMS, true, null, false, null, false,
          null, null);

  @Before
  public void setup() {
    consultingTypeManager = Mockito.mock(ConsultingTypeManager.class);
    sessionDataHelper = new SessionDataHelper(consultingTypeManager);
  }

  @Test
  public void createSessionDataList_Should_ReturnCorrectListOfSessionDataItems() {

    when(consultingTypeManager.getConsultingTypeSettings(Mockito.eq(ConsultingType.SUCHT)))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> result = sessionDataHelper
        .createRegistrationSessionDataList(INITALIZED_SESSION_SUCHT, USER_DTO_WITH_SESSION_DATA);

    assertEquals(5, result.size());

    for (SessionData sessionData : result) {
      switch (sessionData.getKey()) {
        case "addictiveDrugs":
          assertEquals(ADDICTIVE_DRUGS, sessionData.getValue());
          break;
        case "age":
          assertEquals(AGE, sessionData.getValue());
          break;
        case "gender":
          assertEquals(GENDER, sessionData.getValue());
          break;
        case "relation":
          assertEquals(RELATION, sessionData.getValue());
          break;
        case "state":
          assertEquals(STATE, sessionData.getValue());
          break;
        default:
          fail("Unknown SessionData key");
          break;
      }
    }

  }

  @Test
  public void createSessionDataList_Should_ReturnEmptyListOfSessionDataItems() {

    when(consultingTypeManager.getConsultingTypeSettings(Mockito.eq(ConsultingType.U25)))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_NO_SESSION_DATA_ITEMS);

    List<SessionData> result = sessionDataHelper
        .createRegistrationSessionDataList(INITALIZED_SESSION_U25, USER_DTO_WITH_SESSION_DATA);

    assertEquals(0, result.size());

  }

  @Test
  public void createSessionDataList_Should_ReturnCorrectListOfSessionDataItems_WhenSessionDataValuesAreNull() {

    when(consultingTypeManager.getConsultingTypeSettings(Mockito.eq(ConsultingType.SUCHT)))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> result = sessionDataHelper
        .createRegistrationSessionDataList(INITALIZED_SESSION_SUCHT, USER_DTO_WITHOUT_SESSION_DATA);

    for (SessionData sessionData : result) {
      assertNull(sessionData.getValue());
    }

  }

  @Test
  public void createSessionDataList_Should_ReturnCorrectListOfSessionDataItems_WhenSessionDataValuesAreEmpty() {

    when(consultingTypeManager.getConsultingTypeSettings(Mockito.eq(ConsultingType.SUCHT)))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> result = sessionDataHelper.createRegistrationSessionDataList(
        INITALIZED_SESSION_SUCHT, USER_DTO_WITH_EMPTY_SESSION_DATA);

    for (SessionData sessionData : result) {
      assertNull(sessionData.getValue());
    }

  }

  @Test
  public void getValueOfKey_Should_ReturnCorrectValueToGivenKey() {

    when(consultingTypeManager.getConsultingTypeSettings(Mockito.eq(ConsultingType.SUCHT)))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> dataList = sessionDataHelper
        .createRegistrationSessionDataList(INITALIZED_SESSION_SUCHT, USER_DTO_WITH_SESSION_DATA);

    assertEquals(AGE, getValueOfKey(dataList, AGE));
  }

  @Test
  public void getValueOfKey_Should_ReturnNullWhenSessionDataValueIsNull() {

    when(consultingTypeManager.getConsultingTypeSettings(Mockito.eq(ConsultingType.SUCHT)))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> dataList = sessionDataHelper
        .createRegistrationSessionDataList(INITALIZED_SESSION_SUCHT, USER_DTO_WITHOUT_SESSION_DATA);

    assertNull(getValueOfKey(dataList, AGE));
  }

  @Test
  public void getValueOfKey_Should_ReturnNullWhenSessionDataListIsNull() {

    assertNull(getValueOfKey(null, AGE));
  }

  @Test
  public void getSessionDataMapFromSession_Should_ReturnCorrectMapOfSessionDataItems() {

    Map<String, Object> result =
        sessionDataHelper.getSessionDataMapFromSession(INITIALIZED_SESSION_WITH_SESSION_DATA);

    assertEquals(result.get(SESSION_DATA_ADDICTIVE_DRUGS.getKey()),
        SESSION_DATA_ADDICTIVE_DRUGS.getValue());
    assertEquals(result.get(SESSION_DATA_ADDICTIVE_DRUGS.getKey()),
        SESSION_DATA_ADDICTIVE_DRUGS.getValue());
    assertEquals(result.get(SESSION_DATA_GENDER.getKey()), SESSION_DATA_GENDER.getValue());
    assertEquals(result.get(SESSION_DATA_AGE.getKey()), SESSION_DATA_AGE.getValue());
    assertTrue(result.containsKey(SessionDataKeyRegistration.ADDICTIVE_DRUGS.getValue()));
    assertTrue(result.containsKey(SessionDataKeyRegistration.GENDER.getValue()));
    assertTrue(result.containsKey(SessionDataKeyRegistration.AGE.getValue()));
    assertFalse(result.containsKey(SessionDataKeyRegistration.RELATION.getValue()));
    assertFalse(result.containsKey(SessionDataKeyRegistration.STATE.getValue()));

  }

  private String getValueOfKey(List<SessionData> sessionDataList, String key) {

    if (nonNull(sessionDataList)) {
      SessionData sessionData = sessionDataList.stream()
          .filter(data -> key.equals(data.getKey()))
          .findAny()
          .orElse(null);

      if (nonNull(sessionData)) {
        return sessionData.getValue();
      }
    }

    return null;
  }

}
