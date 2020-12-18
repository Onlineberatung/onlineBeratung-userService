package de.caritas.cob.userservice.api.admin.service.consultant.update;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_VALID;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultantUpdateServiceIT {

  private static final String VALID_CONSULTANT_ID = "5674839f-d0a3-47e2-8f9c-bb49fc2ddbbe";

  @Autowired
  private ConsultantUpdateService consultantUpdateService;

  @MockBean
  private KeycloakAdminClientService keycloakAdminClientService;

  @MockBean
  private RocketChatService rocketChatService;

  @Test
  public void updateConsultant_Should_returnUpdatedPersistedConsultant_When_inputDataIsValid() {
    UpdateConsultantDTO updateConsultantDTO = new UpdateConsultantDTO();
    updateConsultantDTO.setAbsent(true);
    updateConsultantDTO.setAbsenceMessage("I am absent!");
    updateConsultantDTO.setFirstname("new first name");
    updateConsultantDTO.setLastname("new last name");
    updateConsultantDTO.setEmail("newemail@address.de");
    updateConsultantDTO.formalLanguage(true);

    Consultant updatedConsultant =
        this.consultantUpdateService.updateConsultant(VALID_CONSULTANT_ID, updateConsultantDTO);

    assertThat(updatedConsultant, notNullValue());
    assertThat(updatedConsultant.isAbsent(), is(true));
    assertThat(updatedConsultant.getAbsenceMessage(), is("I am absent!"));
    assertThat(updatedConsultant.getFirstName(), is("new first name"));
    assertThat(updatedConsultant.getLastName(), is("new last name"));
    assertThat(updatedConsultant.getEmail(), is("newemail@address.de"));
    assertThat(updatedConsultant.isLanguageFormal(), is(true));
  }

  @Test
  public void updateConsultant_Should_throwCustomResponseException_When_absenceIsInvalid() {
    UpdateConsultantDTO updateConsultantDTO = new UpdateConsultantDTO();
    updateConsultantDTO.setAbsent(true);
    updateConsultantDTO.setAbsenceMessage(null);

    try {
      this.consultantUpdateService.updateConsultant(VALID_CONSULTANT_ID, updateConsultantDTO);
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0),
          is(MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER.name()));
    }
  }

  @Test
  public void updateConsultant_Should_throwCustomResponseException_When_newEmailIsInvalid() {
    UpdateConsultantDTO updateConsultantDTO = new UpdateConsultantDTO();
    updateConsultantDTO.setEmail("invalid");

    try {
      this.consultantUpdateService.updateConsultant(VALID_CONSULTANT_ID, updateConsultantDTO);
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0),
          is(EMAIL_NOT_VALID.name()));
    }
  }

}
