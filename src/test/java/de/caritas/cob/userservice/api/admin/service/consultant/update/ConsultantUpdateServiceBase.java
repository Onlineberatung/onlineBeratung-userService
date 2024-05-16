package de.caritas.cob.userservice.api.admin.service.consultant.update;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_VALID;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class ConsultantUpdateServiceBase {

  private static final String VALID_CONSULTANT_ID = "5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe";

  @Autowired protected ConsultantUpdateService consultantUpdateService;

  @MockBean protected IdentityClient identityClient;

  @MockBean protected RocketChatService rocketChatService;

  public void updateConsultant_Should_returnUpdatedPersistedConsultant_When_inputDataIsValid() {
    UpdateAdminConsultantDTO updateConsultantDTO = new UpdateAdminConsultantDTO();
    updateConsultantDTO.setAbsent(true);
    updateConsultantDTO.setAbsenceMessage("I am absent!");
    updateConsultantDTO.setFirstname("new first name");
    updateConsultantDTO.setLastname("new last name");
    updateConsultantDTO.setEmail("newemail@address.de");
    updateConsultantDTO.formalLanguage(true);
    updateConsultantDTO.setDataPrivacyConfirmation(true);

    Consultant updatedConsultant =
        this.consultantUpdateService.updateConsultant(getValidConsultantId(), updateConsultantDTO);

    assertThat(updatedConsultant, notNullValue());
    assertThat(updatedConsultant.isAbsent(), is(true));
    assertThat(updatedConsultant.getAbsenceMessage(), is("I am absent!"));
    assertThat(updatedConsultant.getFirstName(), is("new first name"));
    assertThat(updatedConsultant.getLastName(), is("new last name"));
    assertThat(updatedConsultant.getEmail(), is("newemail@address.de"));
    assertThat(updatedConsultant.isLanguageFormal(), is(true));
    assertThat(
        updatedConsultant.getDataPrivacyConfirmation().getDayOfMonth(),
        is(LocalDateTime.now().getDayOfMonth()));
  }

  public void updateConsultant_Should_throwCustomResponseException_When_absenceIsInvalid() {
    UpdateAdminConsultantDTO updateConsultantDTO = new UpdateAdminConsultantDTO();
    updateConsultantDTO.setAbsent(true);
    updateConsultantDTO.setAbsenceMessage(null);

    try {
      this.consultantUpdateService.updateConsultant(getValidConsultantId(), updateConsultantDTO);
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(
          e.getCustomHttpHeaders().get("X-Reason").get(0),
          is(MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER.name()));
    }
  }

  public void updateConsultant_Should_throwCustomResponseException_When_newEmailIsInvalid() {
    UpdateAdminConsultantDTO updateConsultantDTO = new UpdateAdminConsultantDTO();
    updateConsultantDTO.setEmail("invalid");

    try {
      this.consultantUpdateService.updateConsultant(getValidConsultantId(), updateConsultantDTO);
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeaders().get("X-Reason").get(0), is(EMAIL_NOT_VALID.name()));
    }
  }

  protected String getValidConsultantId() {
    return VALID_CONSULTANT_ID;
  }
}
