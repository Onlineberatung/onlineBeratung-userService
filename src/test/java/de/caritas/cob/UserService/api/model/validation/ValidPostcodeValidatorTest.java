package de.caritas.cob.UserService.api.model.validation;

import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_SUCHT;
import static de.caritas.cob.UserService.testHelper.TestConstants.CONSULTING_TYPE_SUCHT;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_SUCHT;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_SUCHT_WITH_INVALID_POSTCODE;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_WITHOUT_CONSULTING_TYPE;
import static de.caritas.cob.UserService.testHelper.TestConstants.USER_DTO_WITHOUT_MANDATORY_STATE;
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
public class ValidPostcodeValidatorTest {

  @InjectMocks
  private ValidPostcodeValidator validPostcodeValidator;
  @Mock
  private ConsultingTypeManager consultingTypeManager;
  @Mock
  private LogService logService;

  @Test
  public void isValid_Should_ReturnFalse_WhenUserDTOIsNull() {

    boolean result = validPostcodeValidator.isValid(null, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenUserDTOHasNoConsultingType() {

    boolean result = validPostcodeValidator.isValid(USER_DTO_WITHOUT_CONSULTING_TYPE, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenConsultingTypeSettingsAreMissingRegistration() {

    boolean result = validPostcodeValidator.isValid(USER_DTO_WITHOUT_MANDATORY_STATE, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenPostcodeSizeIsInvalid() {

    when(consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);

    boolean result = validPostcodeValidator.isValid(USER_DTO_SUCHT_WITH_INVALID_POSTCODE, null);

    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnTrue_WhenPostcodeSizeIsValid() {

    when(consultingTypeManager.getConsultantTypeSettings(CONSULTING_TYPE_SUCHT))
        .thenReturn(CONSULTING_TYPE_SETTINGS_SUCHT);

    boolean result = validPostcodeValidator.isValid(USER_DTO_SUCHT, null);

    assertTrue(result);
  }

}
