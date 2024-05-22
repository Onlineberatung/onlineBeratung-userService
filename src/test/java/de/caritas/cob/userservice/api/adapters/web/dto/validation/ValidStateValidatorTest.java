package de.caritas.cob.userservice.api.adapters.web.dto.validation;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_U25;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITHOUT_MANDATORY_STATE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_STATE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_DTO_WITHOUT_CONSULTING_TYPE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_DTO_WITHOUT_MANDATORY_STATE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_DTO_WITH_INVALID_STATE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_DTO_WITH_STATE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.manager.consultingtype.registration.mandatoryfields.MandatoryFields;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ValidStateValidatorTest {

  @InjectMocks private ValidStateValidator validStateValidator;
  @Mock private MandatoryFieldsProvider mandatoryFieldsProvider;

  @Test
  public void isValid_Should_ReturnFalse_WhenConsultingTypeIsMissing() {

    boolean result = validStateValidator.isValid(USER_DTO_WITHOUT_CONSULTING_TYPE, null);
    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenStateIsMandatoryAndInvalid() {

    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(
            String.valueOf(CONSULTING_TYPE_ID_U25)))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_STATE
                    .getRegistration()
                    .getMandatoryFields()));

    boolean result = validStateValidator.isValid(USER_DTO_WITH_INVALID_STATE, null);
    assertFalse(result);
  }

  @Test
  public void isValid_Should_ReturnTrue_WhenStateIsMandatoryAndValid() {

    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(
            String.valueOf(CONSULTING_TYPE_ID_U25)))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_STATE
                    .getRegistration()
                    .getMandatoryFields()));

    boolean result = validStateValidator.isValid(USER_DTO_WITH_STATE, null);
    assertTrue(result);
  }

  @Test
  public void isValid_Should_ReturnTrue_WhenStateIsNotMandatory() {

    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(
            String.valueOf(CONSULTING_TYPE_ID_SUCHT)))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_WITHOUT_MANDATORY_STATE
                    .getRegistration()
                    .getMandatoryFields()));

    boolean result = validStateValidator.isValid(USER_DTO_WITHOUT_MANDATORY_STATE, null);
    assertTrue(result);
  }

  @Test
  public void isValid_Should_ReturnFalse_WhenStateIsMandatoryButNull() {

    when(mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(
            String.valueOf(CONSULTING_TYPE_ID_SUCHT)))
        .thenReturn(
            MandatoryFields.convertMandatoryFieldsDTOtoMandatoryFields(
                CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_STATE
                    .getRegistration()
                    .getMandatoryFields()));

    boolean result = validStateValidator.isValid(USER_DTO_WITHOUT_MANDATORY_STATE, null);
    assertFalse(result);
  }
}
