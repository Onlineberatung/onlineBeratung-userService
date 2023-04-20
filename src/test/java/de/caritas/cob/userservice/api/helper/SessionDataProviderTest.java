package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.REGISTERED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGE_VALUE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_U25;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.IS_TEAM_SESSION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.STATE_VALUE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_ID;
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

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDataDTO;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.SessionData;
import de.caritas.cob.userservice.api.model.SessionData.SessionDataType;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.GroupChatDTO;
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
  private final Consultant CONSULTANT =
      new Consultant(
          CONSULTANT_ID,
          USERNAME,
          ROCKETCHAT_ID,
          "first name",
          "last name",
          "consultant@cob.de",
          false,
          false,
          null,
          false,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          true,
          true,
          true,
          true,
          null,
          null,
          ConsultantStatus.CREATED,
          false,
          LanguageCode.de,
          null,
          null,
          false,
          null);
  private final Session INITIALIZED_SESSION_SUCHT =
      Session.builder()
          .id(1L)
          .user(USER)
          .consultant(CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_SUCHT)
          .registrationType(REGISTERED)
          .postcode("99999")
          .agencyId(0L)
          .status(SessionStatus.INITIAL)
          .enquiryMessageDate(nowInUtc())
          .createDate(nowInUtc())
          .build();

  private final Session INITIALIZED_SESSION_U25 =
      Session.builder()
          .id(1L)
          .user(USER)
          .consultant(CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_U25)
          .registrationType(REGISTERED)
          .postcode("99999")
          .agencyId(0L)
          .status(SessionStatus.INITIAL)
          .enquiryMessageDate(nowInUtc())
          .createDate(nowInUtc())
          .build();

  private final SessionData SESSION_DATA_AGE =
      new SessionData(
          new Session(),
          SessionDataType.REGISTRATION,
          SessionDataKeyRegistration.AGE.getValue(),
          "2");
  private final List<SessionData> SESSION_DATA = Arrays.asList(SESSION_DATA_AGE);
  private final Session INITIALIZED_SESSION_WITH_SESSION_DATA =
      Session.builder()
          .id(1L)
          .user(USER)
          .consultant(CONSULTANT)
          .consultingTypeId(CONSULTING_TYPE_ID_U25)
          .registrationType(REGISTERED)
          .postcode("99999")
          .agencyId(1L)
          .status(SessionStatus.IN_PROGRESS)
          .enquiryMessageDate(nowInUtc())
          .sessionData(SESSION_DATA)
          .teamSession(IS_TEAM_SESSION)
          .createDate(nowInUtc())
          .build();

  private final SessionDataDTO SESSION_DATA_DTO =
      new SessionDataDTO().age(AGE_VALUE).state(STATE_VALUE);
  private final SessionDataDTO SESSION_DATA_DTO_WITH_NO_AGE_VALUE =
      new SessionDataDTO().state(STATE_VALUE);
  private final SessionDataDTO EMPTY_SESSION_DATA_DTO = new SessionDataDTO();
  private final SessionDataInitializingDTO SESSION_DATA_INITIALIZING_WITH_ALL_SESSION_DATA_ITEMS =
      new SessionDataInitializingDTO().age(true).state(true);
  private final ExtendedConsultingTypeResponseDTO
      CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS =
          new ExtendedConsultingTypeResponseDTO()
              .id(CONSULTING_TYPE_ID_SUCHT)
              .slug(null)
              .excludeNonMainConsultantsFromTeamSessions(false)
              .groupChat(new GroupChatDTO().isGroupChat(false))
              .consultantBoundedToConsultingType(false)
              .welcomeMessage(
                  new WelcomeMessageDTO().sendWelcomeMessage(false).welcomeMessageText(null))
              .sendFurtherStepsMessage(false)
              .sessionDataInitializing(SESSION_DATA_INITIALIZING_WITH_ALL_SESSION_DATA_ITEMS)
              .initializeFeedbackChat(false)
              .notifications(null)
              .languageFormal(false)
              .roles(null)
              .registration(null);
  private final SessionDataInitializingDTO SESSION_DATA_INITIALIZING_WITH_NO_SESSION_DATA_ITEMS =
      new SessionDataInitializingDTO().age(false).state(false);
  private final ExtendedConsultingTypeResponseDTO
      CONSULTING_TYPE_SETTINGS_WITH_NO_SESSION_DATA_ITEMS =
          new ExtendedConsultingTypeResponseDTO()
              .id(CONSULTING_TYPE_ID_U25)
              .slug(null)
              .excludeNonMainConsultantsFromTeamSessions(false)
              .groupChat(new GroupChatDTO().isGroupChat(false))
              .consultantBoundedToConsultingType(false)
              .welcomeMessage(
                  new WelcomeMessageDTO().sendWelcomeMessage(false).welcomeMessageText(null))
              .sendFurtherStepsMessage(false)
              .sessionDataInitializing(SESSION_DATA_INITIALIZING_WITH_NO_SESSION_DATA_ITEMS)
              .initializeFeedbackChat(false)
              .notifications(null)
              .languageFormal(false)
              .roles(null)
              .registration(null);

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

    List<SessionData> result =
        sessionDataProvider.createSessionDataList(
            sessionWithInitializedItem, SESSION_DATA_DTO_WITH_NO_AGE_VALUE);

    assertEquals(2, result.size());

    for (SessionData sessionData : result) {
      switch (sessionData.getKey()) {
        case "age":
          assertThat(sessionData.getValue(), is(nullValue()));
          assertThat(sessionData.getId(), is(notNullValue()));
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

    List<SessionData> result =
        sessionDataProvider.createSessionDataList(INITIALIZED_SESSION_U25, SESSION_DATA_DTO);

    assertEquals(0, result.size());
  }

  @Test
  public void
      createSessionDataList_Should_ReturnCorrectListOfSessionDataItems_WhenSessionDataValuesAreNull() {
    when(consultingTypeManager.getConsultingTypeSettings(CONSULTING_TYPE_ID_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> result =
        sessionDataProvider.createSessionDataList(INITIALIZED_SESSION_SUCHT, new SessionDataDTO());

    for (SessionData sessionData : result) {
      assertThat(sessionData.getValue(), is(nullValue()));
    }
  }

  @Test
  public void
      createSessionDataList_Should_ReturnCorrectListOfSessionDataItems_WhenSessionDataValuesAreEmpty() {
    when(consultingTypeManager.getConsultingTypeSettings(CONSULTING_TYPE_ID_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> result =
        sessionDataProvider.createSessionDataList(
            INITIALIZED_SESSION_SUCHT, EMPTY_SESSION_DATA_DTO);

    for (SessionData sessionData : result) {
      assertThat(sessionData.getValue(), is(nullValue()));
    }
  }

  @Test
  public void getValueOfKey_Should_ReturnCorrectValueToGivenKey() {

    when(consultingTypeManager.getConsultingTypeSettings(CONSULTING_TYPE_ID_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> dataList =
        sessionDataProvider.createSessionDataList(INITIALIZED_SESSION_SUCHT, SESSION_DATA_DTO);

    assertEquals(AGE_VALUE, getValueOfKey(dataList, AGE));
  }

  @Test
  public void getValueOfKey_Should_ReturnNullWhenSessionDataValueIsNull() {

    when(consultingTypeManager.getConsultingTypeSettings(CONSULTING_TYPE_ID_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_ALL_SESSION_DATA_ITEMS);

    List<SessionData> dataList =
        sessionDataProvider.createSessionDataList(INITIALIZED_SESSION_SUCHT, new SessionDataDTO());

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

    assertEquals(result.get(SESSION_DATA_AGE.getKey()), SESSION_DATA_AGE.getValue());
    assertTrue(result.containsKey(SessionDataKeyRegistration.AGE.getValue()));
    assertFalse(result.containsKey(SessionDataKeyRegistration.STATE.getValue()));
  }

  private String getValueOfKey(List<SessionData> sessionDataList, String key) {

    if (nonNull(sessionDataList)) {
      SessionData sessionData =
          sessionDataList.stream().filter(data -> key.equals(data.getKey())).findAny().orElse(null);

      if (nonNull(sessionData)) {
        return sessionData.getValue();
      }
    }

    return null;
  }
}
