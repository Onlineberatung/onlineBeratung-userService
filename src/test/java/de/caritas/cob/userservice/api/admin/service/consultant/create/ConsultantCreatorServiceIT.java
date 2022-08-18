package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static de.caritas.cob.userservice.api.config.auth.UserRole.CONSULTANT;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_VALID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantImportService.ImportRecord;
import org.jeasy.random.EasyRandom;
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
public class ConsultantCreatorServiceIT {

  private static final String DUMMY_RC_ID = "rcUserId";
  private static final String VALID_USERNAME = "validUsername";
  private static final String VALID_EMAILADDRESS = "valid@emailaddress.de";

  @Autowired private ConsultantCreatorService consultantCreatorService;

  @MockBean private RocketChatService rocketChatService;

  @MockBean private KeycloakService keycloakService;

  private final EasyRandom easyRandom = new EasyRandom();

  @Test
  public void createNewConsultant_Should_returnExpectedCreatedConsultant_When_inputDataIsCorrect()
      throws RocketChatLoginException {
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenReturn(DUMMY_RC_ID);
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setUsername(VALID_USERNAME);
    createConsultantDTO.setEmail(VALID_EMAILADDRESS);

    Consultant consultant = this.consultantCreatorService.createNewConsultant(createConsultantDTO);

    assertThat(consultant, notNullValue());
    assertThat(consultant.getId(), notNullValue());
    assertThat(consultant.getRocketChatId(), is(DUMMY_RC_ID));
    assertThat(consultant.getAbsenceMessage(), notNullValue());
    assertThat(consultant.getCreateDate(), notNullValue());
    assertThat(consultant.getUpdateDate(), notNullValue());
    assertThat(consultant.getUsername(), notNullValue());
    assertThat(consultant.getFirstName(), notNullValue());
    assertThat(consultant.getLastName(), notNullValue());
    assertThat(consultant.getEmail(), notNullValue());
    assertThat(consultant.getFullName(), notNullValue());
  }

  @Test
  public void
      createNewConsultant_Should_returnExpectedCreatedConsultant_When_inputDataIsCorrectImportRecord()
          throws RocketChatLoginException {
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenReturn(DUMMY_RC_ID);
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    ImportRecord importRecord = this.easyRandom.nextObject(ImportRecord.class);
    importRecord.setUsername(VALID_USERNAME);
    importRecord.setEmail(VALID_EMAILADDRESS);

    Consultant consultant =
        this.consultantCreatorService.createNewConsultant(
            importRecord, asSet(CONSULTANT.getValue()));

    assertThat(consultant, notNullValue());
    assertThat(consultant.getId(), notNullValue());
    assertThat(consultant.getRocketChatId(), is(DUMMY_RC_ID));
    assertThat(consultant.getAbsenceMessage(), notNullValue());
    assertThat(consultant.getCreateDate(), notNullValue());
    assertThat(consultant.getUpdateDate(), notNullValue());
    assertThat(consultant.getUsername(), notNullValue());
    assertThat(consultant.getFirstName(), notNullValue());
    assertThat(consultant.getLastName(), notNullValue());
    assertThat(consultant.getEmail(), notNullValue());
    assertThat(consultant.getFullName(), notNullValue());
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      createNewConsultant_Should_throwCustomValidationHttpStatusException_When_userCanNotBeCreatedInRocketChat()
          throws RocketChatLoginException {
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenThrow(new RocketChatLoginException(""));
    KeycloakCreateUserResponseDTO validKeycloakResponse =
        easyRandom.nextObject(KeycloakCreateUserResponseDTO.class);
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(validKeycloakResponse);
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setUsername(VALID_USERNAME);
    createConsultantDTO.setEmail(VALID_EMAILADDRESS);

    this.consultantCreatorService.createNewConsultant(createConsultantDTO);
  }

  @Test(expected = CustomValidationHttpStatusException.class)
  public void
      createNewConsultant_Should_throwCustomValidationHttpStatusException_When_keycloakIdIsMissing()
          throws RocketChatLoginException {
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenReturn(DUMMY_RC_ID);
    KeycloakCreateUserResponseDTO keycloakResponse =
        easyRandom.nextObject(KeycloakCreateUserResponseDTO.class);
    keycloakResponse.setUserId(null);
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(keycloakResponse);
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);

    this.consultantCreatorService.createNewConsultant(createConsultantDTO);
  }

  @Test
  public void createNewConsultant_Should_throwExpectedException_When_emailIsInvalid() {
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setEmail("invalid");

    try {
      this.consultantCreatorService.createNewConsultant(createConsultantDTO);
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeader(), notNullValue());
      assertThat(e.getCustomHttpHeader().get("X-Reason").get(0), is(EMAIL_NOT_VALID.name()));
    }
  }
}
