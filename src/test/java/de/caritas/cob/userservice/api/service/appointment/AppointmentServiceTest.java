package de.caritas.cob.userservice.api.service.appointment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.config.apiclient.AppointmentAgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.AppointmentAskerServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.AppointmentConsultantServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.auth.IdentityConfig;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.appointmentservice.generated.web.AgencyApi;
import de.caritas.cob.userservice.appointmentservice.generated.web.ConsultantApi;
import de.caritas.cob.userservice.appointmentservice.generated.web.model.ConsultantDTO;
import java.util.LinkedList;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(
    strictness = Strictness.LENIENT) // To allow "UnnecessaryStubbing" to keep tests clean
class AppointmentServiceTest {

  private static final String FIELD_NAME_APPOINTMENTS_ENABLED = "appointmentFeatureEnabled";
  private static final EasyRandom easyRandom = new EasyRandom();

  @Spy @InjectMocks AppointmentService appointmentService;

  @InjectMocks AppointmentService nonSpiedAppointmentService;

  @Mock ConsultantApi appointmentConsultantApi;
  @Mock AgencyApi appointmentAgencyApi;
  @Mock SecurityHeaderSupplier securityHeaderSupplier;

  @Mock TenantHeaderSupplier tenantHeaderSupplier;
  @Mock IdentityClient identityClient;

  @SuppressWarnings("unused")
  @Mock
  IdentityClientConfig identityClientConfig;

  @Mock Logger log;

  @Mock ConsultantDTO consultantDTO;

  @Mock ConsultantAdminResponseDTO consultantAdminResponseDTO;

  @Mock KeycloakLoginResponseDTO keycloakLoginResponseDTO;

  @Mock org.springframework.http.HttpHeaders httpHeaders;

  @Mock ObjectMapper objectMapper;

  @Mock HttpClientErrorException httpClientErrorException;

  @Mock AppointmentAgencyServiceApiControllerFactory appointmentAgencyServiceApiControllerFactory;

  @Mock
  AppointmentConsultantServiceApiControllerFactory appointmentConsultantServiceApiControllerFactory;

  @Mock AppointmentAskerServiceApiControllerFactory appointmentAskerServiceApiControllerFactory;

  @BeforeEach
  public void beforeEach() throws JsonProcessingException {
    when(identityClient.loginUser(any(), any())).thenReturn(keycloakLoginResponseDTO);
    when(identityClient.loginUser(any(), any())).thenReturn(keycloakLoginResponseDTO);
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders(any())).thenReturn(httpHeaders);
    when(consultantDTO.getId()).thenReturn("testId");
    when(objectMapper.readValue(
            nullable(String.class), ArgumentMatchers.<Class<ConsultantDTO>>any()))
        .thenReturn(consultantDTO);
    when(appointmentService.getObjectMapper(anyBoolean())).thenReturn(objectMapper);
    when(appointmentAgencyServiceApiControllerFactory.createControllerApi())
        .thenReturn(appointmentAgencyApi);
    when(appointmentConsultantServiceApiControllerFactory.createControllerApi())
        .thenReturn(appointmentConsultantApi);
  }

  @Test
  void createConsultant_Should_NotCallAppointmentService_WhenAppointmentsIsDisabled() {
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, false);
    appointmentService.createConsultant(consultantAdminResponseDTO);
    verify(appointmentConsultantApi, never()).createConsultant(any());
    verify(appointmentConsultantApi, never()).createConsultantWithHttpInfo(any());
  }

  @Test
  void updateConsultant_Should_NotCallAppointmentService_WhenAppointmentsIsDisabled() {
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, false);
    appointmentService.updateConsultant(consultantAdminResponseDTO);
    verify(appointmentConsultantApi, never()).updateConsultant(any(), any());
    verify(appointmentConsultantApi, never()).updateConsultantWithHttpInfo(any(), any());
  }

  @Test
  void deleteConsultant_Should_NotCallAppointmentService_WhenAppointmentsIsDisabled() {
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, false);
    appointmentService.deleteConsultant("testId");
    verify(appointmentConsultantApi, never()).deleteConsultant(any());
    verify(appointmentConsultantApi, never()).deleteConsultantWithHttpInfo(any());
  }

  @Test
  void
      deleteConsultant_Should_ProceedWithDeletion_WhenAppointmentsIsEnabledAndConsultantNotFoundInAppointmentService() {
    givenAnIdentityClientConfig();
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, true);
    when(httpClientErrorException.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
    doThrow(httpClientErrorException).when(appointmentConsultantApi).deleteConsultant("testId");
    appointmentService.deleteConsultant("testId");
    verify(appointmentConsultantApi).deleteConsultant("testId");
  }

  @Test
  void
      deleteConsultant_Should_ProceedWithDeletion_WhenAppointmentsIsEnabledAndAppointmentServiceThrowsExceptionOtherThan404() {
    var identityClientConfig = easyRandom.nextObject(IdentityConfig.class);
    setField(nonSpiedAppointmentService, "identityClientConfig", identityClientConfig);
    setField(nonSpiedAppointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, true);
    when(httpClientErrorException.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
    doThrow(httpClientErrorException).when(appointmentConsultantApi).deleteConsultant("testId");
    assertThrows(
        HttpClientErrorException.class,
        () -> nonSpiedAppointmentService.deleteConsultant("testId"));
  }

  @Test
  void syncAgencies_Should_NotCallAppointmentService_WhenAppointmentsIsDisabled() {
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, false);
    appointmentService.syncAgencies("testId", new LinkedList<>());
    verify(appointmentAgencyApi, never()).agencyConsultantsSync(any());
  }

  @Test
  void createConsultant_Should_CallAppointmentService_WhenAppointmentsIsDisabled() {
    givenAnIdentityClientConfig();
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, true);
    appointmentService.createConsultant(consultantAdminResponseDTO);
    verify(appointmentConsultantApi, times(1)).createConsultant(any());
  }

  @Test
  void updateConsultant_Should_CallAppointmentService_WhenAppointmentsIsDisabled() {
    givenAnIdentityClientConfig();
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, true);
    appointmentService.updateConsultant(consultantAdminResponseDTO);
    verify(appointmentConsultantApi, times(1)).updateConsultant(any(), any());
  }

  @Test
  void deleteConsultant_Should_CallAppointmentService_WhenAppointmentsIsDisabled() {
    givenAnIdentityClientConfig();
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, true);
    appointmentService.deleteConsultant("testId");
    verify(appointmentConsultantApi, times(1)).deleteConsultant(any());
  }

  @Test
  void syncAgencies_Should_CallAppointmentService_WhenAppointmentsIsDisabled() {
    givenAnIdentityClientConfig();
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, true);
    appointmentService.syncAgencies("testId", new LinkedList<>());
    verify(appointmentAgencyApi, times(1)).agencyConsultantsSync(any());
  }

  private void givenAnIdentityClientConfig() {
    var identityClientConfig = easyRandom.nextObject(IdentityConfig.class);
    setField(appointmentService, "identityClientConfig", identityClientConfig);
  }
}
