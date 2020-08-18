package de.caritas.cob.userservice.api.manager.consultingType;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_AIDS;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_CHILDREN;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_CURE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_DEBT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_DISABILITY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_EMIGRATION;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_HOSPICE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_KREUZBUND;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_LAW;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_MIGRATION;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_OFFENDER;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_PARENTING;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_PLANB;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_PREGNANCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_REGIONAL;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_REHABILITATION;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SENIORITY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_AIDS;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_CHILDREN;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_CURE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_DEBT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_DISABILITY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_LAW;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_OFFENDER;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_PARENTING;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_PLANB;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_PREGNANCY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_REHABILITATION;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SENIORITY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SOCIAL;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_U25;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SOCIAL;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_U25;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;

@RunWith(MockitoJUnitRunner.class)
public class ConsultingTypeManagerTest {

  private final String INIT_GROUP_NAME = "init";
  private final String FIELD_NAME_CONSULTING_TYPE_SETTINGS_MAP = "consultingTypeSettingsMap";
  private final String FIELD_NAME_CONSULTING_TYPES_SETTINGS_JSON_PATH =
      "CONSULTING_TYPES_SETTINGS_JSON_PATH";
  private final String FIELD_NAME_CONSULTING_TYPES_SETTINGS_JSON_PATH_VALUE =
      "/consulting-type-settings";
  private final Map<Integer, ConsultingTypeSettings> CONSULTING_TYPE_SETTINGS_MAP =
      new HashMap<Integer, ConsultingTypeSettings>() {
        private static final long serialVersionUID = 1L;
        {
          put(CONSULTING_TYPE_SUCHT.getValue(), CONSULTING_TYPE_SETTINGS_SUCHT);
          put(CONSULTING_TYPE_U25.getValue(), CONSULTING_TYPE_SETTINGS_U25);
          put(CONSULTING_TYPE_PREGNANCY.getValue(), CONSULTING_TYPE_SETTINGS_PREGNANCY);
          put(CONSULTING_TYPE_AIDS.getValue(), CONSULTING_TYPE_SETTINGS_AIDS);
          put(CONSULTING_TYPE_CHILDREN.getValue(), CONSULTING_TYPE_SETTINGS_CHILDREN);
          put(CONSULTING_TYPE_CURE.getValue(), CONSULTING_TYPE_SETTINGS_CURE);
          put(CONSULTING_TYPE_DEBT.getValue(), CONSULTING_TYPE_SETTINGS_DEBT);
          put(CONSULTING_TYPE_DISABILITY.getValue(), CONSULTING_TYPE_SETTINGS_DISABILITY);
          put(CONSULTING_TYPE_LAW.getValue(), CONSULTING_TYPE_SETTINGS_LAW);
          put(CONSULTING_TYPE_OFFENDER.getValue(), CONSULTING_TYPE_SETTINGS_OFFENDER);
          put(CONSULTING_TYPE_PARENTING.getValue(), CONSULTING_TYPE_SETTINGS_PARENTING);
          put(CONSULTING_TYPE_PLANB.getValue(), CONSULTING_TYPE_SETTINGS_PLANB);
          put(CONSULTING_TYPE_REHABILITATION.getValue(), CONSULTING_TYPE_SETTINGS_REHABILITATION);
          put(CONSULTING_TYPE_SENIORITY.getValue(), CONSULTING_TYPE_SETTINGS_SENIORITY);
          put(CONSULTING_TYPE_SOCIAL.getValue(), CONSULTING_TYPE_SETTINGS_SOCIAL);
        }
      };
  private final Map<Integer, ConsultingTypeSettings> CONSULTING_TYPE_SETTINGS_MAP_WITH_MISSING_CONSULTING_TYPE_SETTINGS_FOR_U25 =
      new HashMap<Integer, ConsultingTypeSettings>() {
        private static final long serialVersionUID = 1L;
        {
          put(CONSULTING_TYPE_SUCHT.getValue(), CONSULTING_TYPE_SETTINGS_SUCHT);
        }
      };

  @Test
  public void test_Should_Fail_WhenMethodInitDoesNotHavePostConstructAnnotation()
      throws NoSuchMethodException, SecurityException {

    ConsultingTypeManager consultingTypeManager = new ConsultingTypeManager();
    Class<? extends ConsultingTypeManager> classToTest = consultingTypeManager.getClass();

    Method methodToTest = classToTest.getDeclaredMethod(INIT_GROUP_NAME);
    methodToTest.setAccessible(true);
    PostConstruct annotation = methodToTest.getAnnotation(PostConstruct.class);

    assertNotNull(annotation);
  }

  @Test
  public void getConsultantTypeSettings_Should_ReturnConsultantTypeSettingsForConsultingType()
      throws NoSuchFieldException, SecurityException {

    ConsultingTypeManager consultingTypeManager = new ConsultingTypeManager();
    FieldSetter.setField(consultingTypeManager,
        consultingTypeManager.getClass().getDeclaredField(FIELD_NAME_CONSULTING_TYPE_SETTINGS_MAP),
        CONSULTING_TYPE_SETTINGS_MAP);

    ConsultingTypeSettings result =
        consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_SUCHT);
    assertEquals(CONSULTING_TYPE_SETTINGS_SUCHT, result);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_U25);
    assertEquals(CONSULTING_TYPE_SETTINGS_U25, result);
  }

  @Test
  public void getConsultantTypeSettings_Should_ThrowMissingConsultingTypeException_WhenSettingsForGivenConsultingTypeAreMissing()
      throws NoSuchFieldException, SecurityException {

    ConsultingTypeManager consultingTypeManager = new ConsultingTypeManager();
    FieldSetter.setField(consultingTypeManager,
        consultingTypeManager.getClass().getDeclaredField(FIELD_NAME_CONSULTING_TYPE_SETTINGS_MAP),
        CONSULTING_TYPE_SETTINGS_MAP_WITH_MISSING_CONSULTING_TYPE_SETTINGS_FOR_U25);

    try {
      consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_U25);
      fail("Expected exception: MissingConsultingTypeException");
    } catch (MissingConsultingTypeException missingConsultingTypeException) {
      assertTrue("Excepted MissingConsultingTypeException thrown", true);
    }

  }

  @Test
  public void init_Should_InitializeConsultingTypeSettingFromJsonFile()
      throws NoSuchMethodException, SecurityException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, NoSuchFieldException, JsonParseException,
      JsonMappingException, IOException {

    ConsultingTypeManager consultingTypeManager = new ConsultingTypeManager();

    FieldSetter.setField(consultingTypeManager,
        consultingTypeManager.getClass()
            .getDeclaredField(FIELD_NAME_CONSULTING_TYPES_SETTINGS_JSON_PATH),
        FIELD_NAME_CONSULTING_TYPES_SETTINGS_JSON_PATH_VALUE);

    Class<? extends ConsultingTypeManager> classToTest = consultingTypeManager.getClass();
    Method methodToTest = classToTest.getDeclaredMethod(INIT_GROUP_NAME);
    methodToTest.setAccessible(true);
    methodToTest.invoke(consultingTypeManager);

    /**
     * SUCHT
     */

    ConsultingTypeSettings consultantTypeSettingsSucht =
        loadConsultingTypeSettings(CONSULTING_TYPE_SUCHT);
    consultantTypeSettingsSucht.setConsultingType(CONSULTING_TYPE_SUCHT);

    ConsultingTypeSettings result =
        consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_SUCHT);
    assertEquals(consultantTypeSettingsSucht.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsSucht.isSendWelcomeMessage(), result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsSucht.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsSucht.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsSucht.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsSucht.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsSucht.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsSucht.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertTrue(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsSucht.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * U25
     */

    ConsultingTypeSettings consultantTypeSettingsU25 =
        loadConsultingTypeSettings(CONSULTING_TYPE_U25);
    consultantTypeSettingsU25.setConsultingType(CONSULTING_TYPE_U25);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_U25);
    assertEquals(consultantTypeSettingsU25.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsU25.isSendWelcomeMessage(), result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsU25.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsU25.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsU25.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsU25.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsU25.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsU25.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertEquals(
        consultantTypeSettingsU25.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isMonitoring());
    assertTrue(result.isFeedbackChat());
    assertFalse(result.isLanguageFormal());
    assertTrue(result.getRegistration().getMandatoryFields().isAge());
    assertTrue(result.getRegistration().getMandatoryFields().isState());

    /**
     * PREGNANCY
     */

    ConsultingTypeSettings consultantTypeSettingsPregnancy =
        loadConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY);
    consultantTypeSettingsPregnancy.setConsultingType(CONSULTING_TYPE_PREGNANCY);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_PREGNANCY);
    assertEquals(consultantTypeSettingsPregnancy.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsPregnancy.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsPregnancy.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsPregnancy.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsPregnancy.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsPregnancy.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsPregnancy.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsPregnancy.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsPregnancy.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertFalse(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * AIDS
     */

    ConsultingTypeSettings consultantTypeSettingsAids =
        loadConsultingTypeSettings(CONSULTING_TYPE_AIDS);
    consultantTypeSettingsAids.setConsultingType(CONSULTING_TYPE_AIDS);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_AIDS);
    assertEquals(consultantTypeSettingsAids.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsAids.isSendWelcomeMessage(), result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsAids.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsAids.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsAids.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsAids.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsAids.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsAids.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsAids.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * CHILDREN
     */

    ConsultingTypeSettings consultantTypeSettingsChildren =
        loadConsultingTypeSettings(CONSULTING_TYPE_CHILDREN);
    consultantTypeSettingsChildren.setConsultingType(CONSULTING_TYPE_CHILDREN);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_CHILDREN);
    assertEquals(consultantTypeSettingsChildren.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsChildren.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsChildren.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsChildren.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsChildren.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsChildren.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsChildren.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsChildren.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsChildren.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * CURE
     */

    ConsultingTypeSettings consultantTypeSettingsCure =
        loadConsultingTypeSettings(CONSULTING_TYPE_CURE);
    consultantTypeSettingsCure.setConsultingType(CONSULTING_TYPE_CURE);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_CURE);
    assertEquals(consultantTypeSettingsCure.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsCure.isSendWelcomeMessage(), result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsCure.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsCure.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsCure.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsCure.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsCure.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsCure.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsCure.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * DEBT
     */

    ConsultingTypeSettings consultantTypeSettingsDebt =
        loadConsultingTypeSettings(CONSULTING_TYPE_DEBT);
    consultantTypeSettingsDebt.setConsultingType(CONSULTING_TYPE_DEBT);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_DEBT);
    assertEquals(consultantTypeSettingsDebt.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsDebt.isSendWelcomeMessage(), result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsDebt.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsDebt.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsDebt.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsDebt.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsDebt.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsDebt.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsDebt.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * DISABILITY
     */

    ConsultingTypeSettings consultantTypeSettingsDisability =
        loadConsultingTypeSettings(CONSULTING_TYPE_DISABILITY);
    consultantTypeSettingsDisability.setConsultingType(CONSULTING_TYPE_DISABILITY);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_DISABILITY);
    assertEquals(consultantTypeSettingsDisability.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsDisability.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsDisability.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsDisability.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsDisability.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsDisability.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsDisability.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsDisability.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsDisability.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * LAW
     */

    ConsultingTypeSettings consultantTypeSettingsLaw =
        loadConsultingTypeSettings(CONSULTING_TYPE_LAW);
    consultantTypeSettingsLaw.setConsultingType(CONSULTING_TYPE_LAW);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_LAW);
    assertEquals(consultantTypeSettingsLaw.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsLaw.isSendWelcomeMessage(), result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsLaw.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsLaw.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsLaw.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsLaw.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsLaw.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsLaw.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsLaw.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * OFFENDER
     */

    ConsultingTypeSettings consultantTypeSettingsOffender =
        loadConsultingTypeSettings(CONSULTING_TYPE_OFFENDER);
    consultantTypeSettingsOffender.setConsultingType(CONSULTING_TYPE_OFFENDER);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_OFFENDER);
    assertEquals(consultantTypeSettingsOffender.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsOffender.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsOffender.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsOffender.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsOffender.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsOffender.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsOffender.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsOffender.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsOffender.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * PARENTING
     */

    ConsultingTypeSettings consultantTypeSettingsParenting =
        loadConsultingTypeSettings(CONSULTING_TYPE_PARENTING);
    consultantTypeSettingsParenting.setConsultingType(CONSULTING_TYPE_PARENTING);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_PARENTING);
    assertEquals(consultantTypeSettingsParenting.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsParenting.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsParenting.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsParenting.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsParenting.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsParenting.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsParenting.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsParenting.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsParenting.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * PLANB
     */

    ConsultingTypeSettings consultantTypeSettingsPlanB =
        loadConsultingTypeSettings(CONSULTING_TYPE_PLANB);
    consultantTypeSettingsPlanB.setConsultingType(CONSULTING_TYPE_PLANB);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_PLANB);
    assertEquals(consultantTypeSettingsPlanB.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsPlanB.isSendWelcomeMessage(), result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsPlanB.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsPlanB.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsPlanB.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsPlanB.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsPlanB.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsPlanB.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsPlanB.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertFalse(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * REHABILITATION
     */

    ConsultingTypeSettings consultantTypeSettingsRehabilitation =
        loadConsultingTypeSettings(CONSULTING_TYPE_REHABILITATION);
    consultantTypeSettingsRehabilitation.setConsultingType(CONSULTING_TYPE_REHABILITATION);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_REHABILITATION);
    assertEquals(consultantTypeSettingsRehabilitation.getConsultingType(),
        result.getConsultingType());
    assertEquals(consultantTypeSettingsRehabilitation.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsRehabilitation.getWelcomeMessage(),
        result.getWelcomeMessage());
    assertEquals(
        consultantTypeSettingsRehabilitation.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsRehabilitation.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsRehabilitation.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsRehabilitation.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsRehabilitation.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsRehabilitation.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * SENIORITY
     */

    ConsultingTypeSettings consultantTypeSettingsSeniority =
        loadConsultingTypeSettings(CONSULTING_TYPE_SENIORITY);
    consultantTypeSettingsSeniority.setConsultingType(CONSULTING_TYPE_SENIORITY);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_SENIORITY);
    assertEquals(consultantTypeSettingsSeniority.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsSeniority.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsSeniority.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsSeniority.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsSeniority.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsSeniority.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsSeniority.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsSeniority.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsSeniority.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * SOCIAL
     */

    ConsultingTypeSettings consultantTypeSettingsSocial =
        loadConsultingTypeSettings(CONSULTING_TYPE_SOCIAL);
    consultantTypeSettingsSocial.setConsultingType(CONSULTING_TYPE_SOCIAL);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_SOCIAL);
    assertEquals(consultantTypeSettingsSocial.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsSocial.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsSocial.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsSocial.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsSocial.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsSocial.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsSocial.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsSocial.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsSocial.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * KREUZBUND
     */

    ConsultingTypeSettings consultantTypeSettingsKreuzbund =
        loadConsultingTypeSettings(CONSULTING_TYPE_KREUZBUND);
    consultantTypeSettingsKreuzbund.setConsultingType(CONSULTING_TYPE_KREUZBUND);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_KREUZBUND);
    assertEquals(consultantTypeSettingsKreuzbund.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsKreuzbund.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsKreuzbund.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsKreuzbund.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsKreuzbund.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsKreuzbund.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsKreuzbund.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsKreuzbund.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsKreuzbund.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * MIGRATION
     */

    ConsultingTypeSettings consultantTypeSettingsMigration =
        loadConsultingTypeSettings(CONSULTING_TYPE_MIGRATION);
    consultantTypeSettingsMigration.setConsultingType(CONSULTING_TYPE_MIGRATION);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_MIGRATION);
    assertEquals(consultantTypeSettingsMigration.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsMigration.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsMigration.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsMigration.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsMigration.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsMigration.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsMigration.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsMigration.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsMigration.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * EMIGRATION
     */

    ConsultingTypeSettings consultantTypeSettingsEmigration =
        loadConsultingTypeSettings(CONSULTING_TYPE_EMIGRATION);
    consultantTypeSettingsEmigration.setConsultingType(CONSULTING_TYPE_EMIGRATION);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_EMIGRATION);
    assertEquals(consultantTypeSettingsEmigration.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsEmigration.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsEmigration.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsEmigration.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsEmigration.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsEmigration.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsEmigration.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsEmigration.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsEmigration.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * HOSPICE
     */

    ConsultingTypeSettings consultantTypeSettingsHospice =
        loadConsultingTypeSettings(CONSULTING_TYPE_HOSPICE);
    consultantTypeSettingsHospice.setConsultingType(CONSULTING_TYPE_HOSPICE);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_HOSPICE);
    assertEquals(consultantTypeSettingsHospice.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsHospice.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsHospice.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsHospice.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsHospice.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsHospice.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsHospice.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsHospice.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsHospice.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

    /**
     * REGIONAL
     */

    ConsultingTypeSettings consultantTypeSettingsRegional =
        loadConsultingTypeSettings(CONSULTING_TYPE_REGIONAL);
    consultantTypeSettingsRegional.setConsultingType(CONSULTING_TYPE_REGIONAL);

    result = consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_REGIONAL);
    assertEquals(consultantTypeSettingsRegional.getConsultingType(), result.getConsultingType());
    assertEquals(consultantTypeSettingsRegional.isSendWelcomeMessage(),
        result.isSendWelcomeMessage());
    assertEquals(consultantTypeSettingsRegional.getWelcomeMessage(), result.getWelcomeMessage());
    assertEquals(consultantTypeSettingsRegional.getSessionDataInitializing().isAddictiveDrugs(),
        result.getSessionDataInitializing().isAddictiveDrugs());
    assertEquals(consultantTypeSettingsRegional.getSessionDataInitializing().isAge(),
        result.getSessionDataInitializing().isAge());
    assertEquals(consultantTypeSettingsRegional.getSessionDataInitializing().isGender(),
        result.getSessionDataInitializing().isGender());
    assertEquals(consultantTypeSettingsRegional.getSessionDataInitializing().isRelation(),
        result.getSessionDataInitializing().isRelation());
    assertEquals(consultantTypeSettingsRegional.getSessionDataInitializing().isState(),
        result.getSessionDataInitializing().isState());
    assertFalse(result.isMonitoring());
    assertFalse(result.isFeedbackChat());
    assertEquals(
        consultantTypeSettingsRegional.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant().isAllTeamConsultants(),
        result.getNotifications().getNewMessage().getTeamSession().getToConsultant()
            .isAllTeamConsultants());
    assertTrue(result.isLanguageFormal());
    assertFalse(result.getRegistration().getMandatoryFields().isAge());
    assertFalse(result.getRegistration().getMandatoryFields().isState());

  }

  private ConsultingTypeSettings loadConsultingTypeSettings(ConsultingType consultingType)
      throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<ConsultingTypeSettings> typeReference =
        new TypeReference<ConsultingTypeSettings>() {};
    InputStream inputStream = null;
    inputStream =
        TypeReference.class.getResourceAsStream(FIELD_NAME_CONSULTING_TYPES_SETTINGS_JSON_PATH_VALUE
            + "/" + consultingType.name().toLowerCase() + ".json");
    return mapper.readValue(inputStream, typeReference);
  }

}
