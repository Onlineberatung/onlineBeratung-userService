package de.caritas.cob.userservice.api.model.validation;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITHOUT_AGE_MANDATORY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_AGE_MANDATORY;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_U25;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_WITHOUT_CONSULTING_TYPE;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_WITHOUT_MANDATORY_AGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_WITH_AGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_WITH_INVALID_AGE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.service.LogService;

@RunWith(MockitoJUnitRunner.class)
public class ValidAgeValidatorTest {

  @InjectMocks
  private ValidAgeValidator validAgeValidator;
  @Mock
  private ConsultingTypeManager consultingTypeManager;
  @Mock
  private LogService logService;

  @Test
  public void isValid_Should_ReturnFalse_WhenUserDTOIsNull() {

    boolean result = validAgeValidator.isValid(null, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenUserDTOHasNoConsultingType() {

    boolean result = validAgeValidator.isValid(USER_DTO_WITHOUT_CONSULTING_TYPE, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenConsultingTypeSettingsAreMissingMandatoryFields() {

    when(consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);

    boolean result = validAgeValidator.isValid(USER_DTO_WITHOUT_MANDATORY_AGE, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenAgeIsMandatoryAndInvalid() {

    when(consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_U25))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_AGE_MANDATORY);

    boolean result = validAgeValidator.isValid(USER_DTO_WITH_INVALID_AGE, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnTrue_WhenAgeIsMandatoryAndValid() {

    when(consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_U25))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_AGE_MANDATORY);

    boolean result = validAgeValidator.isValid(USER_DTO_WITH_AGE, null);

    assertTrue(result);
  }

  @Test
  public void isValid_Should_ReturnTrue_WhenAgeIsNotMandatory() {

    when(consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_AGE_MANDATORY);

    boolean result = validAgeValidator.isValid(USER_DTO_WITHOUT_MANDATORY_AGE, null);

    assertTrue(result);
  }
}
