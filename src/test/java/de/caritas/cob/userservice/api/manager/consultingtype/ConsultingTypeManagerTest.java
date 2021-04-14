package de.caritas.cob.userservice.api.manager.consultingtype;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_AIDS;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_CHILDREN;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_CURE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_DEBT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_DISABILITY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_LAW;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_OFFENDER;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_PARENTING;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_PLANB;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_PREGNANCY;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultingTypeManagerTest {

  private static final String INIT_GROUP_NAME = "init";
  private static final String FIELD_NAME_CONSULTING_TYPE_SETTINGS_MAP = "consultingTypeSettingsMap";
  private static final String FIELD_NAME_CONSULTING_TYPES_SETTINGS_JSON_PATH =
      "consultingTypesSettingsJsonPath";
  private static final String FIELD_NAME_CONSULTING_TYPES_SETTINGS_JSON_PATH_VALUE =
      "consulting-type-settings";
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
        consultingTypeManager.getConsultingTypeSettings(0);
    assertEquals(CONSULTING_TYPE_SETTINGS_SUCHT, result);

    result = consultingTypeManager.getConsultingTypeSettings(1);
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
      consultingTypeManager.getConsultingTypeSettings(1);
      fail("Expected exception: MissingConsultingTypeException");
    } catch (MissingConsultingTypeException missingConsultingTypeException) {
      assertTrue("Excepted MissingConsultingTypeException thrown", true);
    }

  }

  protected static ConsultingTypeSettings loadConsultingTypeSettings(int consultingType) {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<ConsultingTypeSettings> typeReference =
        new TypeReference<ConsultingTypeSettings>() {
        };
    URL dirUrl = ConsultingType.class.getClassLoader()
        .getResource(FIELD_NAME_CONSULTING_TYPES_SETTINGS_JSON_PATH_VALUE);

    try {
      for (String jsonFileName : new File(dirUrl.toURI()).list()) {
        InputStream inputStream =
            TypeReference.class.getResourceAsStream(
                "/" + FIELD_NAME_CONSULTING_TYPES_SETTINGS_JSON_PATH_VALUE + "/" + jsonFileName);
        ConsultingTypeSettings consultingTypeSettings = mapper
            .readValue(inputStream, typeReference);
        if (consultingTypeSettings.getConsultingID() == consultingType) {
          return consultingTypeSettings;
        }
      }
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  protected static int countConsultingTypeSettings() {
    URL dirUrl = ConsultingType.class.getClassLoader()
        .getResource(FIELD_NAME_CONSULTING_TYPES_SETTINGS_JSON_PATH_VALUE);

    try {
      return new File(dirUrl.toURI()).list().length;
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return 0;
  }


}
