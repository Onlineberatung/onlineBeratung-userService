package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static de.caritas.cob.userservice.api.config.auth.UserRole.CONSULTANT;
import static de.caritas.cob.userservice.api.config.auth.UserRole.GROUP_CHAT_CONSULTANT;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_VALID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDTO;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantAdminService;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.DistributedTransactionException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantImportService.ImportRecord;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.tenantadminservice.generated.web.model.Settings;
import de.caritas.cob.userservice.tenantadminservice.generated.web.model.TenantDTO;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class CreateConsultantSagaIT {

  private static final String DUMMY_RC_ID = "rcUserId";
  private static final String VALID_USERNAME = "validUsername";
  private static final String VALID_EMAILADDRESS = "valid@emailaddress.de";
  private static final long TENANT_ID = 1L;

  @Autowired private CreateConsultantSaga createConsultantSaga;

  @MockBean private RocketChatService rocketChatService;

  @MockBean private KeycloakService keycloakService;

  @MockBean private TenantAdminService tenantAdminService;

  @MockBean private RollbackFacade rollbackFacade;

  @MockBean private AppointmentService appointmentService;

  @MockBean private SessionService sessionService;

  private final EasyRandom easyRandom = new EasyRandom();

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(createConsultantSaga, "appointmentFeatureEnabled", false);
  }

  @Test
  public void createNewConsultant_Should_returnExpectedCreatedConsultant_When_inputDataIsCorrect()
      throws RocketChatLoginException, RocketChatAddUserToGroupException {
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenReturn(DUMMY_RC_ID);
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setUsername(VALID_USERNAME);
    createConsultantDTO.setEmail(VALID_EMAILADDRESS);
    createConsultantDTO.setIsGroupchatConsultant(false);

    when(sessionService.getRegisteredEnquiriesForConsultant(any()))
        .thenReturn(
            Lists.newArrayList(
                new ConsultantSessionResponseDTO().session(new SessionDTO().groupId("groupId"))));

    var consultantAdminResponseDTO =
        this.createConsultantSaga.createNewConsultant(createConsultantDTO);

    ConsultantDTO consultant = consultantAdminResponseDTO.getEmbedded();
    verify(keycloakService).updateRole(anyString(), eq(CONSULTANT.getValue()));

    assertThat(consultant, notNullValue());
    assertThat(consultant.getId(), notNullValue());
    assertThat(consultant.getAbsenceMessage(), notNullValue());
    assertThat(consultant.getCreateDate(), notNullValue());
    assertThat(consultant.getUpdateDate(), notNullValue());
    assertThat(consultant.getUsername(), notNullValue());
    assertThat(consultant.getFirstname(), notNullValue());
    assertThat(consultant.getLastname(), notNullValue());
    assertThat(consultant.getEmail(), notNullValue());

    verify(rocketChatService).getUserID(anyString(), anyString(), anyBoolean());
    verify(rocketChatService).addUserToGroup(Mockito.anyString(), Mockito.eq("groupId"));
  }

  @Test
  public void createNewConsultant_Should_callRollback_When_RocketchatThrowsException()
      throws RocketChatLoginException {
    doThrow(BadRequestException.class)
        .when(rocketChatService)
        .getUserID(anyString(), anyString(), anyBoolean());
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setUsername(VALID_USERNAME);
    createConsultantDTO.setEmail(VALID_EMAILADDRESS);
    createConsultantDTO.setIsGroupchatConsultant(false);

    try {
      this.createConsultantSaga.createNewConsultant(createConsultantDTO);
      fail("Exception should be thrown");
    } catch (DistributedTransactionException ex) {
      assertThat(
          ex.getCustomHttpHeaders().get("X-Reason").get(0),
          is("DISTRIBUTED_TRANSACTION_FAILED_ON_STEP_CREATE_ACCOUNT_IN_ROCKETCHAT"));
      verify(keycloakService).updateRole(anyString(), eq(CONSULTANT.getValue()));
      verify(keycloakService).updateRole(anyString(), eq(CONSULTANT.getValue()));
      verify(rollbackFacade).rollbackConsultantAccount(Mockito.any(Consultant.class));
    }
  }

  @Test
  public void createNewConsultant_Should_callRollback_When_AppointmentServiceThrowsException()
      throws RocketChatLoginException {
    ReflectionTestUtils.setField(createConsultantSaga, "appointmentFeatureEnabled", true);
    doThrow(BadRequestException.class).when(appointmentService).createConsultant(any());
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenReturn(DUMMY_RC_ID);
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setUsername(VALID_USERNAME);
    createConsultantDTO.setEmail(VALID_EMAILADDRESS);
    createConsultantDTO.setIsGroupchatConsultant(false);

    try {
      this.createConsultantSaga.createNewConsultant(createConsultantDTO);
      fail("Exception should be thrown");
    } catch (DistributedTransactionException ex) {
      assertThat(
          ex.getCustomHttpHeaders().get("X-Reason").get(0),
          is(
              "DISTRIBUTED_TRANSACTION_FAILED_ON_STEP_CREATE_ACCOUNT_IN_CALCOM_OR_APPOINTMENTSERVICE"));
      verify(keycloakService).updateRole(anyString(), eq(CONSULTANT.getValue()));
      verify(keycloakService).updateRole(anyString(), eq(CONSULTANT.getValue()));
      verify(rocketChatService).getUserID(anyString(), anyString(), anyBoolean());
      verify(rollbackFacade).rollbackConsultantAccount(Mockito.any(Consultant.class));
    }
  }

  @Test
  public void createNewConsultant_Should_callRollback_When_KeycloakUpdatePasswordThrowsException() {
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    doThrow(BadRequestException.class).when(keycloakService).updatePassword(any(), any());
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setUsername(VALID_USERNAME);
    createConsultantDTO.setEmail(VALID_EMAILADDRESS);
    createConsultantDTO.setIsGroupchatConsultant(false);

    try {
      this.createConsultantSaga.createNewConsultant(createConsultantDTO);
      fail("Exception should be thrown");
    } catch (DistributedTransactionException ex) {
      assertThat(
          ex.getCustomHttpHeaders().get("X-Reason").get(0),
          is("DISTRIBUTED_TRANSACTION_FAILED_ON_STEP_UPDATE_USER_PASSWORD_IN_KEYCLOAK"));
      verify(keycloakService, Mockito.never()).updateRole(anyString(), eq(CONSULTANT.getValue()));
      verify(rollbackFacade).rollbackConsultantAccount(Mockito.any(Consultant.class));
    }
  }

  @Test
  public void createNewConsultant_Should_callRollback_When_KeycloakUpdateRoleThrowsException()
      throws RocketChatLoginException {
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    doThrow(BadRequestException.class).when(keycloakService).updateRole(anyString(), anyString());
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setUsername(VALID_USERNAME);
    createConsultantDTO.setEmail(VALID_EMAILADDRESS);
    createConsultantDTO.setIsGroupchatConsultant(false);

    try {
      this.createConsultantSaga.createNewConsultant(createConsultantDTO);
      fail("Exception should be thrown");
    } catch (DistributedTransactionException ex) {
      assertThat(
          ex.getCustomHttpHeaders().get("X-Reason").get(0),
          is("DISTRIBUTED_TRANSACTION_FAILED_ON_STEP_UPDATE_USER_ROLES_IN_KEYCLOAK"));
      verify(rocketChatService, Mockito.never()).getUserID(anyString(), anyString(), anyBoolean());
      verify(rollbackFacade).rollbackConsultantAccount(Mockito.any(Consultant.class));
    }
  }

  @Test
  public void createNewConsultant_Should_callRollback_When_anyOfTheServicesThrowsException()
      throws RocketChatLoginException {
    doThrow(BadRequestException.class)
        .when(rocketChatService)
        .getUserID(anyString(), anyString(), anyBoolean());
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setUsername(VALID_USERNAME);
    createConsultantDTO.setEmail(VALID_EMAILADDRESS);
    createConsultantDTO.setIsGroupchatConsultant(false);

    try {
      this.createConsultantSaga.createNewConsultant(createConsultantDTO);
      fail("Exception should be thrown");
    } catch (DistributedTransactionException ex) {
      assertThat(
          ex.getCustomHttpHeaders().get("X-Reason").get(0),
          is("DISTRIBUTED_TRANSACTION_FAILED_ON_STEP_CREATE_ACCOUNT_IN_ROCKETCHAT"));
      verify(keycloakService).updateRole(anyString(), eq(CONSULTANT.getValue()));
      verify(keycloakService).updateRole(anyString(), eq(CONSULTANT.getValue()));
      verify(rollbackFacade).rollbackConsultantAccount(Mockito.any(Consultant.class));
    }
  }

  @Test
  public void
      createNewConsultant_Should_addConsultantAndGroupChatConsultantRole_When_isGroupChatConsultantFlagIsEnabled()
          throws RocketChatLoginException {
    // given
    when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
        .thenReturn(DUMMY_RC_ID);
    when(keycloakService.createKeycloakUser(any(), anyString(), any()))
        .thenReturn(easyRandom.nextObject(KeycloakCreateUserResponseDTO.class));
    var tenant = new TenantDTO().settings(new Settings().featureGroupChatV2Enabled(false));
    when(tenantAdminService.getTenantById((long) TENANT_ID)).thenReturn(tenant);

    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setTenantId(TENANT_ID);
    createConsultantDTO.setUsername(VALID_USERNAME);
    createConsultantDTO.setEmail(VALID_EMAILADDRESS);
    createConsultantDTO.setIsGroupchatConsultant(true);

    // when
    var consultantAdminResponseDTO = createConsultantSaga.createNewConsultant(createConsultantDTO);

    // then
    verify(keycloakService, times(2)).updateRole(anyString(), anyString());
    verify(keycloakService).updateRole(anyString(), eq(CONSULTANT.getValue()));
    verify(keycloakService).updateRole(anyString(), eq(GROUP_CHAT_CONSULTANT.getValue()));

    assertThat(consultantAdminResponseDTO.getEmbedded(), notNullValue());
    assertThat(consultantAdminResponseDTO.getEmbedded().getId(), notNullValue());
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
        this.createConsultantSaga.createNewConsultant(importRecord, asSet(CONSULTANT.getValue()));

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
      createNewConsultant_Should_throwCustomValidationHttpStatusException_When_userCanNotBeCreatedInRocketChat()
          throws RocketChatLoginException {
    assertThrows(
        DistributedTransactionException.class,
        () -> {
          when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
              .thenThrow(new RocketChatLoginException(""));
          KeycloakCreateUserResponseDTO validKeycloakResponse =
              easyRandom.nextObject(KeycloakCreateUserResponseDTO.class);
          when(keycloakService.createKeycloakUser(any(), anyString(), any()))
              .thenReturn(validKeycloakResponse);
          CreateConsultantDTO createConsultantDTO =
              this.easyRandom.nextObject(CreateConsultantDTO.class);
          createConsultantDTO.setUsername(VALID_USERNAME);
          createConsultantDTO.setEmail(VALID_EMAILADDRESS);

          this.createConsultantSaga.createNewConsultant(createConsultantDTO);
        });
  }

  @Test
  public void
      createNewConsultant_Should_throwCustomValidationHttpStatusException_When_keycloakIdIsMissing()
          throws RocketChatLoginException {
    assertThrows(
        CustomValidationHttpStatusException.class,
        () -> {
          when(rocketChatService.getUserID(anyString(), anyString(), anyBoolean()))
              .thenReturn(DUMMY_RC_ID);
          KeycloakCreateUserResponseDTO keycloakResponse =
              easyRandom.nextObject(KeycloakCreateUserResponseDTO.class);
          keycloakResponse.setUserId(null);
          when(keycloakService.createKeycloakUser(any(), anyString(), any()))
              .thenReturn(keycloakResponse);
          CreateConsultantDTO createConsultantDTO =
              this.easyRandom.nextObject(CreateConsultantDTO.class);

          this.createConsultantSaga.createNewConsultant(createConsultantDTO);
        });
  }

  @Test
  public void createNewConsultant_Should_throwExpectedException_When_emailIsInvalid() {
    CreateConsultantDTO createConsultantDTO = this.easyRandom.nextObject(CreateConsultantDTO.class);
    createConsultantDTO.setEmail("invalid");

    try {
      this.createConsultantSaga.createNewConsultant(createConsultantDTO);
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeaders(), notNullValue());
      assertThat(e.getCustomHttpHeaders().get("X-Reason").get(0), is(EMAIL_NOT_VALID.name()));
    }
  }
}
