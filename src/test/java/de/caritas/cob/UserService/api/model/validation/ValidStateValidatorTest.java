package de.caritas.cob.UserService.api.model.validation;

import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITHOUT_MANDATORY_STATE;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITHOUT_STATE_FIELD;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_STATE;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTING_TYPE_U25;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_WITHOUT_CONSULTING_TYPE;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_WITHOUT_MANDATORY_STATE;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_WITH_INVALID_STATE;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_WITH_STATE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.UserService.api.service.LogService;

@RunWith(MockitoJUnitRunner.class)
public class ValidStateValidatorTest {

  @InjectMocks
  private ValidStateValidator validStateValidator;
  @Mock
  private ConsultingTypeManager consultingTypeManager;
  @Mock
  private LogService logService;

  @Test
  public void isValid_Should_ReturnFalse_WhenUserDTOIsNull() {

    boolean result = validStateValidator.isValid(null, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenUserDTOHasNoConsultingType() {

    boolean result = validStateValidator.isValid(USER_DTO_WITHOUT_CONSULTING_TYPE, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenConsultingTypeSettingsAreMissingMandatoryFields() {

    when(consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_STATE_FIELD);

    boolean result = validStateValidator.isValid(USER_DTO_WITHOUT_MANDATORY_STATE, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenStateIsMandatoryAndInvalid() {

    when(consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_U25))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_STATE);

    boolean result = validStateValidator.isValid(USER_DTO_WITH_INVALID_STATE, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnTrue_WhenStateIsMandatoryAndValid() {

    when(consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_U25))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_STATE);

    boolean result = validStateValidator.isValid(USER_DTO_WITH_STATE, null);

    assertTrue(result);
  }

  @Test
  public void isValid_Should_ReturnTrue_WhenStateIsNotMandatory() {

    when(consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MANDATORY_STATE);

    boolean result = validStateValidator.isValid(USER_DTO_WITHOUT_MANDATORY_STATE, null);

    assertTrue(result);
  }

}
