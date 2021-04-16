package de.caritas.cob.userservice.api.facade.userdata;

import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO_WITH_EMPTY_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO_WITH_HTML_AND_JS;
import static de.caritas.cob.userservice.testHelper.TestConstants.ABSENCE_DTO_WITH_NULL_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import java.util.Optional;
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
  private AuthenticatedUser authenticatedUser;

  @Mock
  private ConsultantUpdateService consultantUpdateService;

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

  @Test(expected = NotFoundException.class)
  public void updateConsultantData_Should_throwNotFoundException_When_authenticatedConsultantDoesNotExist() {
    this.consultantDataFacade.updateConsultantData(new UpdateConsultantDTO());
  }

  @Test
  public void updateConsultantData_Should_updateExpectedConsultantData() {
    UpdateConsultantDTO updateConsultantDTO = new UpdateConsultantDTO()
        .email("email")
        .firstname("firstname")
        .lastname("lastname");
    Consultant consultant = mock(Consultant.class);
    when(consultant.isLanguageFormal()).thenReturn(true);
    when(consultant.isAbsent()).thenReturn(true);
    when(consultant.getAbsenceMessage()).thenReturn("absenceMessage");
    when(this.consultantService.getConsultant(any())).thenReturn(Optional.of(consultant));

    this.consultantDataFacade.updateConsultantData(updateConsultantDTO);

    ArgumentCaptor<UpdateAdminConsultantDTO> captor = ArgumentCaptor
        .forClass(UpdateAdminConsultantDTO.class);
    verify(this.consultantUpdateService).updateConsultant(any(), captor.capture());
    assertThat(captor.getValue().getEmail(), is("email"));
    assertThat(captor.getValue().getFirstname(), is("firstname"));
    assertThat(captor.getValue().getLastname(), is("lastname"));
    assertThat(captor.getValue().getAbsent(), is(true));
    assertThat(captor.getValue().getFormalLanguage(), is(true));
    assertThat(captor.getValue().getAbsenceMessage(), is("absenceMessage"));
  }

}
