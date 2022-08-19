package de.caritas.cob.userservice.api.service.appointment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.appointmentservice.generated.web.AgencyApi;
import de.caritas.cob.userservice.appointmentservice.generated.web.ConsultantApi;
import de.caritas.cob.userservice.appointmentservice.generated.web.model.ConsultantDTO;
import java.util.LinkedList;
import org.junit.Before;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(
    strictness = Strictness.LENIENT) // To allow "UnnecessaryStubbing" to keep tests clean
class AppointmentServiceTest {

  private static final String FIELD_NAME_APPOINTMENTS_ENABLED = "appointmentFeatureEnabled";

  @Spy @InjectMocks AppointmentService appointmentService;

  @Mock ConsultantApi appointmentConsultantApi;
  @Mock AgencyApi appointmentAgencyApi;
  @Mock SecurityHeaderSupplier securityHeaderSupplier;
  @Mock TenantHeaderSupplier tenantHeaderSupplier;
  @Mock IdentityClient identityClient;

  @Mock Logger log;

  @Mock ConsultantDTO consultantDTO;

  @Mock ConsultantAdminResponseDTO consultantAdminResponseDTO;

  @Mock KeycloakLoginResponseDTO keycloakLoginResponseDTO;

  @Mock org.springframework.http.HttpHeaders httpHeaders;

  @Mock ObjectMapper objectMapper;

  @Before
  public void setup() {}

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
  void syncAgencies_Should_NotCallAppointmentService_WhenAppointmentsIsDisabled() {
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, false);
    appointmentService.syncAgencies("testId", new LinkedList<>());
    verify(appointmentAgencyApi, never()).agencyConsultantsSync(any());
  }

  @Test
  void createConsultant_Should_CallAppointmentService_WhenAppointmentsIsDisabled() {
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, true);
    appointmentService.createConsultant(consultantAdminResponseDTO);
    verify(appointmentConsultantApi, times(1)).createConsultant(any());
  }

  @Test
  void updateConsultant_Should_CallAppointmentService_WhenAppointmentsIsDisabled() {
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, true);
    appointmentService.updateConsultant(consultantAdminResponseDTO);
    verify(appointmentConsultantApi, times(1)).updateConsultant(any(), any());
  }

  @Test
  void deleteConsultant_Should_CallAppointmentService_WhenAppointmentsIsDisabled() {
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, true);
    appointmentService.deleteConsultant("testId");
    verify(appointmentConsultantApi, times(1)).deleteConsultant(any());
  }

  @Test
  void syncAgencies_Should_CallAppointmentService_WhenAppointmentsIsDisabled() {
    setField(appointmentService, FIELD_NAME_APPOINTMENTS_ENABLED, true);
    appointmentService.syncAgencies("testId", new LinkedList<>());
    verify(appointmentAgencyApi, times(1)).agencyConsultantsSync(any());
  }
}
