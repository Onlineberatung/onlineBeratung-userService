package de.caritas.cob.userservice.api.model.validation;

import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTING_TYPE_PREGNANCY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.SessionDataDTO;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MandatoryFieldsValidatorTest {

  @InjectMocks private MandatoryFieldsValidator mandatoryFieldsValidator;
  @Mock private ConsultingTypeManager consultingTypeManager;

  EasyRandom easyRandom = new EasyRandom();

  @Test(expected = InternalServerErrorException.class)
  public void
      validateFields_Should_ThrowInternalServerError_When_MandatoryFieldsMissingInConsultingTypeSettings() {
    ConsultingTypeSettings settings = easyRandom.nextObject(ConsultingTypeSettings.class);
    settings.getRegistration().setMandatoryFields(null);
    when(consultingTypeManager.getConsultingTypeSettings(any())).thenReturn(settings);

    mandatoryFieldsValidator.validateFields(CONSULTING_TYPE_PREGNANCY, new SessionDataDTO());
  }

  @Test(expected = BadRequestException.class)
  public void validateFields_Should_ThrowBadRequestException_When_MandatoryFieldAgeIsMissing() {
    ConsultingTypeSettings settings = easyRandom.nextObject(ConsultingTypeSettings.class);
    settings.getRegistration().getMandatoryFields().setAge(true);
    SessionDataDTO sessionData = easyRandom.nextObject(SessionDataDTO.class);
    sessionData.setAge("");
    when(consultingTypeManager.getConsultingTypeSettings(any())).thenReturn(settings);

    mandatoryFieldsValidator.validateFields(CONSULTING_TYPE_PREGNANCY, sessionData);
  }

  @Test(expected = BadRequestException.class)
  public void validateFields_Should_ThrowBadRequestException_When_MandatoryFieldStateIsMissing() {
    ConsultingTypeSettings settings = easyRandom.nextObject(ConsultingTypeSettings.class);
    settings.getRegistration().getMandatoryFields().setState(true);
    SessionDataDTO sessionData = easyRandom.nextObject(SessionDataDTO.class);
    sessionData.setAge("4");
    sessionData.setState("");
    when(consultingTypeManager.getConsultingTypeSettings(any())).thenReturn(settings);

    mandatoryFieldsValidator.validateFields(CONSULTING_TYPE_PREGNANCY, sessionData);
  }

  @Test
  public void validateFields_ShouldNot_ThrowBadRequestException_When_AllMandatoryFieldsAreGiven() {
    ConsultingTypeSettings settings = easyRandom.nextObject(ConsultingTypeSettings.class);
    settings.getRegistration().getMandatoryFields().setState(true);
    settings.getRegistration().getMandatoryFields().setAge(true);
    SessionDataDTO sessionData = easyRandom.nextObject(SessionDataDTO.class);
    sessionData.setAge("4");
    sessionData.setState("6");
    when(consultingTypeManager.getConsultingTypeSettings(any())).thenReturn(settings);

    mandatoryFieldsValidator.validateFields(CONSULTING_TYPE_PREGNANCY, sessionData);

    verify(consultingTypeManager, times(1)).getConsultingTypeSettings(any());
  }
}
