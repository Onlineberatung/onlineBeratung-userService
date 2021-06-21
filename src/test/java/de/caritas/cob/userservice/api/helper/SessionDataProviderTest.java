package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.repository.session.RegistrationType.REGISTERED;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.testHelper.TestConstants.ADDICTIVE_DRUGS_VALUE;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.AGE_VALUE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_ID_U25;
import static de.caritas.cob.userservice.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.testHelper.TestConstants.GENDER_VALUE;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_TEAM_SESSION;
import static de.caritas.cob.userservice.testHelper.TestConstants.RELATION_VALUE;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.STATE_VALUE;
import static de.caritas.cob.userservice.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.SessionDataDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionData;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataKeyRegistration;
import de.caritas.cob.userservice.api.repository.sessiondata.SessionDataType;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.GroupChatDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.MonitoringDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.SessionDataInitializingDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.WelcomeMessageDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionDataProviderTest {

  private SessionDataProvider sessionDataProvider;
  private ConsultingTypeManager consultingTypeManager;

  private final EasyRandom easyRandom = new EasyRandom();
  private final User USER = new User(USER_ID, null, USERNAME, EMAIL, false);
  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, USERNAME, ROCKETCHAT_ID,
      "first name", "last name", "consultant@cob.de", false, false, null, false, null, null, null,
      null, null, null);
  private final Session INITIALIZED_SESSION_SUCHT = new Session(1L, USER, CONSULTANT,
      CONSULTING_TYPE_ID_SUCHT, REGISTERED, "99999", 0L, SessionStatus.INITIAL, null, null, null,
      null, false,
      false, null, null);
  private final Session INITIALIZED_SESSION_U25 = new Session(1L, USER, CONSULTANT,
      CONSULTING_TYPE_ID_U25, REGISTERED, "99999", 0L, SessionStatus.INITIAL, null, null, null,
      null, false,
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
      CONSULTING_TYPE_ID_SUCHT, REGISTERED, "99999", 1L, SessionStatus.IN_PROGRESS, nowInUtc(),
      null,
      null, SESSION_DATA, IS_TEAM_SESSION, IS_MONITORING, null, null);
  private final SessionDataDTO SESSION_DATA_DTO = (SessionDataDTO) new SessionDataDTO()
      .addictiveDrugs(ADDICTIVE_DRUGS_VALUE).relation(RELATION_VALUE).gender(GENDER_VALUE)
      .age(AGE_VALUE).state(STATE_VALUE);
  private final SessionDataDTO SESSION_DATA_DTO_WITH_NO_AGE_VALUE =
      (SessionDataDTO) new SessionDataDTO()
          .addictiveDrugs(ADDICTIVE_DRUGS_VALUE).relation(RELATION_VALUE).gender(GENDER_VALUE)
          .state(STATE_VALUE);
  private final SessionDataDTO EMPTY_SESSION_DATA_DTO =
      new SessionDataDTO();
  private final SessionDataInitializingDTO SESSION_DATA_INITIALIZING_WITH_ALL_SESSION_DATA_ITEMS =
      new SessionDataInitializingDTO().addictiveDrugs(true).age(true).gender(true).relation(true)
          .relation(true).state(true);
  private final ExtendedConsultingTypeResponseDTO CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS =
      new ExtendedConsultingTypeResponseDTO().id(CONSULTING_TYPE_ID_SUCHT).slug(null)
          .excludeNonMainConsultantsFromTeamSessions(false)
          .groupChat(new GroupChatDTO().isGroupChat(false)).consultantBoundedToConsultingType(false)
          .welcomeMessage(
              new WelcomeMessageDTO().sendWelcomeMessage(false).welcomeMessageText(null))
          .sendFurtherStepsMessage(false).sendSaveSessionDataMessage(false)
          .sessionDataInitializing(SESSION_DATA_INITIALIZING_WITH_ALL_SESSION_DATA_ITEMS)
          .monitoring(new MonitoringDTO().initializeMonitoring(true)
              .monitoringTemplateFile(null))
          .initializeFeedbackChat(false).notifications(null)
          .languageFormal(false).roles(null).registration(null);
  private final SessionDataInitializingDTO SESSION_DATA_INITIALIZING_WITH_NO_SESSION_DATA_ITEMS =
      new SessionDataInitializingDTO().addictiveDrugs(false).age(false).gender(false)
          .relation(false)
          .relation(false).state(false);
  private final ExtendedConsultingTypeResponseDTO CONSULTING_TYPE_SETTINGS_WITH_NO_SESSION_DATA_ITEMS =
      new ExtendedConsultingTypeResponseDTO().id(CONSULTING_TYPE_ID_U25).slug(null)
          .excludeNonMainConsultantsFromTeamSessions(false)
          .groupChat(new GroupChatDTO().isGroupChat(false)).consultantBoundedToConsultingType(false)
          .welcomeMessage(
              new WelcomeMessageDTO().sendWelcomeMessage(false).welcomeMessageText(null))
          .sendFurtherStepsMessage(false).sendSaveSessionDataMessage(false)
          .sessionDataInitializing(SESSION_DATA_INITIALIZING_WITH_NO_SESSION_DATA_ITEMS)
          .monitoring(new MonitoringDTO().initializeMonitoring(true)
              .monitoringTemplateFile(null))
          .initializeFeedbackChat(false).notifications(null)
          .languageFormal(false).roles(null).registration(null);

  @Before
  public void setup() {
    consultingTypeManager = Mockito.mock(ConsultingTypeManager.class);
    sessionDataProvider = new SessionDataProvider(consultingTypeManager);
  }

  @Test
  public void createSessionDataList_Should_ReturnCorrectListOfSessionDataItems() {
    Session sessionWithInitializedItem = easyRandom.nextObject(Session.class);
    sessionWithInitializedItem.setConsultingTypeId(CONSULTING_TYPE_ID_SUCHT);
    SessionData data = easyRandom.nextObject(SessionData.class);
    data.setKey("addictiveDrugs");
    data.setValue("updatedValue");
    SessionData dataAge = easyRandom.nextObject(SessionData.class);
    dataAge.setKey("age");
    dataAge.setValue(null);
    List<SessionData> sessionDataList = new ArrayList<>();
    sessionDataList.add(data);
    sessionDataList.add(dataAge);
    sessionWithInitializedItem.setSessionData(sessionDataList);
    when(consultingTypeManager.getConsultingTypeSettings(CONSULTING_TYPE_ID_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> result = sessionDataProvider
        .createSessionDataList(sessionWithInitializedItem, SESSION_DATA_DTO_WITH_NO_AGE_VALUE);

    assertEquals(5, result.size());

    for (SessionData sessionData : result) {
      switch (sessionData.getKey()) {
        case "addictiveDrugs":
          assertEquals(ADDICTIVE_DRUGS_VALUE, sessionData.getValue());
          assertThat(sessionData.getId(), is(notNullValue()));
          break;
        case "age":
          assertThat(sessionData.getValue(), is(nullValue()));
          assertThat(sessionData.getId(), is(notNullValue()));
          break;
        case "gender":
          assertEquals(GENDER_VALUE, sessionData.getValue());
          assertThat(sessionData.getId(), is(nullValue()));
          break;
        case "relation":
          assertEquals(RELATION_VALUE, sessionData.getValue());
          assertThat(sessionData.getId(), is(nullValue()));
          break;
        case "state":
          assertEquals(STATE_VALUE, sessionData.getValue());
          assertThat(sessionData.getId(), is(nullValue()));
          break;
        default:
          fail("Unknown SessionData key");
          break;
      }
    }

  }

  @Test
  public void createSessionDataList_Should_ReturnEmptyListOfSessionDataItems() {

    when(consultingTypeManager.getConsultingTypeSettings(CONSULTING_TYPE_ID_U25))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_NO_SESSION_DATA_ITEMS);

    List<SessionData> result = sessionDataProvider
        .createSessionDataList(INITIALIZED_SESSION_U25, SESSION_DATA_DTO);

    assertEquals(0, result.size());

  }

  @Test
  public void createSessionDataList_Should_ReturnCorrectListOfSessionDataItems_WhenSessionDataValuesAreNull() {
    when(consultingTypeManager.getConsultingTypeSettings(CONSULTING_TYPE_ID_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> result = sessionDataProvider
        .createSessionDataList(INITIALIZED_SESSION_SUCHT,
            new SessionDataDTO());

    for (SessionData sessionData : result) {
      assertThat(sessionData.getValue(), is(nullValue()));
    }
  }

  @Test
  public void createSessionDataList_Should_ReturnCorrectListOfSessionDataItems_WhenSessionDataValuesAreEmpty() {
    when(consultingTypeManager.getConsultingTypeSettings(CONSULTING_TYPE_ID_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> result = sessionDataProvider.createSessionDataList(
        INITIALIZED_SESSION_SUCHT, EMPTY_SESSION_DATA_DTO);

    for (SessionData sessionData : result) {
      assertThat(sessionData.getValue(), is(nullValue()));
    }

  }

  @Test
  public void getValueOfKey_Should_ReturnCorrectValueToGivenKey() {

    when(consultingTypeManager.getConsultingTypeSettings(CONSULTING_TYPE_ID_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> dataList = sessionDataProvider
        .createSessionDataList(INITIALIZED_SESSION_SUCHT, SESSION_DATA_DTO);

    assertEquals(AGE_VALUE, getValueOfKey(dataList, AGE));
  }

  @Test
  public void getValueOfKey_Should_ReturnNullWhenSessionDataValueIsNull() {

    when(consultingTypeManager.getConsultingTypeSettings(CONSULTING_TYPE_ID_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> dataList = sessionDataProvider
        .createSessionDataList(INITIALIZED_SESSION_SUCHT,
            new SessionDataDTO());

    assertNull(getValueOfKey(dataList, AGE));
  }

  @Test
  public void getValueOfKey_Should_ReturnNullWhenSessionDataListIsNull() {

    assertNull(getValueOfKey(null, AGE));
  }

  @Test
  public void getSessionDataMapFromSession_Should_ReturnCorrectMapOfSessionDataItems() {

    Map<String, Object> result =
        sessionDataProvider.getSessionDataMapFromSession(INITIALIZED_SESSION_WITH_SESSION_DATA);

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
