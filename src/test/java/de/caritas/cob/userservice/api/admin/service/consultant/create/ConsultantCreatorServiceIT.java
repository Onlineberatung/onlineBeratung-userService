package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static de.caritas.cob.userservice.api.authorization.UserRole.CONSULTANT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantImportService.ImportRecord;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
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

  @Autowired
  private ConsultantCreatorService consultantCreatorService;

  @MockBean
  private RocketChatService rocketChatService;

  @MockBean
  private KeycloakAdminClientHelper keycloakAdminClientHelper;

  private final EasyRandom easyRandom = new EasyRandom();

  @Test
  public void createNewConsultant_Should_returnExpectedCreatedConsultant_When_inputDataIsCorrect()
      throws RocketChatLoginException {
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenReturn(DUMMY_RC_ID);
    when(keycloakAdminClientHelper.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);

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
  public void createNewConsultant_Should_returnExpectedCreatedConsultant_When_inputDataIsCorrectImportRecord()
      throws RocketChatLoginException {
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenReturn(DUMMY_RC_ID);
    when(keycloakAdminClientHelper.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    ImportRecord importRecord = this.easyRandom.nextObject(ImportRecord.class);

    Consultant consultant = this.consultantCreatorService.createNewConsultant(importRecord,
        asSet(CONSULTANT.getValue()));

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
  public void createNewConsultant_Should_throwInternalServerErrorException_When_userCanNotBeCreatedInRocketChat()
      throws RocketChatLoginException {
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenThrow(new RocketChatLoginException(""));
    when(keycloakAdminClientHelper.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(
            KeycloakCreateUserResponseDTO.class));
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);

    this.consultantCreatorService.createNewConsultant(createConsultantDTO);
  }

  @Test(expected = KeycloakException.class)
  public void createNewConsultant_Should_throwKeycloakException_When_keycloakIdIsMissing()
      throws RocketChatLoginException {
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenReturn(DUMMY_RC_ID);
    KeycloakCreateUserResponseDTO keycloakResponse = easyRandom.nextObject(
        KeycloakCreateUserResponseDTO.class);
    keycloakResponse.setUserId(null);
    when(keycloakAdminClientHelper.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(keycloakResponse);
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);

    this.consultantCreatorService.createNewConsultant(createConsultantDTO);
  }

}
