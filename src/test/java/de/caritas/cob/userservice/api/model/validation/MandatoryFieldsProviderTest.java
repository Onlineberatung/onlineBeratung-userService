package de.caritas.cob.userservice.api.model.validation;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_U25;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITHOUT_REGISTRATION;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_FIELDS_NULL;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_U25;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.registration.mandatoryFields.MandatoryFields;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MandatoryFieldsProviderTest {

  @InjectMocks
  private MandatoryFieldsProvider mandatoryFieldsProvider;
  @Mock
  private ConsultingTypeManager consultingTypeManager;

  @Test
  public void fetchMandatoryFieldsForConsultingType_Should_ReturnMandatoryFieldsForConsultingType() {

    when(consultingTypeManager.getConsultingTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_U25);
    MandatoryFields result =
        mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(Integer.toString(CONSULTING_TYPE_U25.getValue()));
    assertEquals(CONSULTING_TYPE_SETTINGS_U25.getRegistration().getMandatoryFields(), result);
  }

  @Test(expected = InternalServerErrorException.class)
  public void fetchMandatoryFieldsForConsultingType_Should_ThrowInternalServerError_WhenRegistrationIsNull() {
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_REGISTRATION);
    MandatoryFields result =
        mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(Integer.toString(CONSULTING_TYPE_U25.getValue()));
  }

  @Test(expected = InternalServerErrorException.class)
  public void fetchMandatoryFieldsForConsultingType_Should_ThrowInternalServerError_WhenMandatoryFieldsIsNull() {
    when(consultingTypeManager.getConsultingTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_FIELDS_NULL);
    MandatoryFields result =
        mandatoryFieldsProvider.fetchMandatoryFieldsForConsultingType(Integer.toString(CONSULTING_TYPE_U25.getValue()));
  }

}
