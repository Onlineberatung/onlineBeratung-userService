package de.caritas.cob.userservice.api.facade.userdata;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ABSENCE_DTO;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ABSENCE_DTO_WITH_EMPTY_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ABSENCE_DTO_WITH_HTML_AND_JS;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ABSENCE_DTO_WITH_NULL_MESSAGE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConsultantDataFacadeTest {

  @InjectMocks
  private ConsultantDataFacade consultantDataFacade;

  @Mock
  private ConsultantService consultantService;

  @Test
  public void updateConsultantAbsent_Should_UpdateAbsenceMessageAndIsAbsence() {
    when(consultantService.saveConsultant(Mockito.any(Consultant.class))).thenReturn(CONSULTANT);

    Consultant consultant = consultantDataFacade.updateConsultantAbsent(CONSULTANT, ABSENCE_DTO);

    assertEquals(consultant.getAbsenceMessage(), ABSENCE_DTO.getMessage());
    assertEquals(consultant.isAbsent(), ABSENCE_DTO.getAbsent());
  }

  @Test
  public void saveEnquiryMessageAndRocketChatGroupId_Should_RemoveHtmlCodeAndJsFromMessageForXssProtection() {
    when(consultantService.saveConsultant(Mockito.any(Consultant.class))).thenReturn(CONSULTANT);

    Consultant consultant = consultantDataFacade.updateConsultantAbsent(CONSULTANT,
        ABSENCE_DTO_WITH_HTML_AND_JS);

    assertEquals(consultant.isAbsent(), ABSENCE_DTO_WITH_HTML_AND_JS.getAbsent());
    assertNotEquals(consultant.getAbsenceMessage(), ABSENCE_DTO_WITH_HTML_AND_JS.getMessage());
    assertEquals(MESSAGE, consultant.getAbsenceMessage());
  }

  @Test
  public void updateConsultantAbsent_Should_SetAbsenceMessageToNull_WhenAbsenceMessageFromDtoIsEmpty() {
    consultantDataFacade.updateConsultantAbsent(CONSULTANT, ABSENCE_DTO_WITH_EMPTY_MESSAGE);

    ArgumentCaptor<Consultant> captor = ArgumentCaptor.forClass(Consultant.class);
    verify(consultantService).saveConsultant(captor.capture());
    assertNull(captor.getValue().getAbsenceMessage());
  }

  @Test
  public void updateConsultantAbsent_Should_SetAbsenceMessageToNull_WhenAbsenceMessageFromDtoIsNull() {
    consultantDataFacade.updateConsultantAbsent(CONSULTANT, ABSENCE_DTO_WITH_NULL_MESSAGE);

    ArgumentCaptor<Consultant> captor = ArgumentCaptor.forClass(Consultant.class);
    verify(consultantService).saveConsultant(captor.capture());
    assertNull(captor.getValue().getAbsenceMessage());
  }
}
