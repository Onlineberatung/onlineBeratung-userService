package de.caritas.cob.userservice.api.service.appointment;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.appointmentservice.generated.ApiClient;
import de.caritas.cob.userservice.appointmentservice.generated.web.ConsultantApi;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Service class to communicate with the AppointmentService.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

  private final @NonNull ConsultantApi appointmentConsultantApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;
  private final @NonNull IdentityClient identityClient;


  @Value("${keycloakService.technical.username}")
  private String keycloakTechnicalUsername;

  @Value("${keycloakService.technical.password}")
  private String keycloakTechnicalPassword;


  public void createConsultant(ConsultantAdminResponseDTO consultantAdminResponseDTO) {
    if (consultantAdminResponseDTO != null) {
      ObjectMapper mapper = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      addTechnicalUserHeaders(this.appointmentConsultantApi.getApiClient());
      try {
        de.caritas.cob.userservice.appointmentservice.generated.web.model.ConsultantDTO consultant =
            mapper.readValue(mapper.writeValueAsString(consultantAdminResponseDTO.getEmbedded()),
                de.caritas.cob.userservice.appointmentservice.generated.web.model.ConsultantDTO.class);
        this.appointmentConsultantApi.createConsultant(consultant);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
  }

  public void updateConsultant(ConsultantAdminResponseDTO consultantAdminResponseDTO) {
    if (consultantAdminResponseDTO != null) {
      ObjectMapper mapper = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      addTechnicalUserHeaders(this.appointmentConsultantApi.getApiClient());
      try {
        de.caritas.cob.userservice.appointmentservice.generated.web.model.ConsultantDTO consultant =
            mapper.readValue(mapper.writeValueAsString(consultantAdminResponseDTO.getEmbedded()),
                de.caritas.cob.userservice.appointmentservice.generated.web.model.ConsultantDTO.class);
        this.appointmentConsultantApi.updateConsultant(consultant.getId(), consultant);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
  }

  public void deleteConsultant(String consultantId) {
    if (consultantId != null && !consultantId.isEmpty()) {
      addTechnicalUserHeaders(this.appointmentConsultantApi.getApiClient());
      this.appointmentConsultantApi.deleteConsultant(consultantId);
    }
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    var headers = this.securityHeaderSupplier.getCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  private void addTechnicalUserHeaders(ApiClient apiClient) {
    var keycloakLoginResponseDTO = identityClient.loginUser(
        keycloakTechnicalUsername, keycloakTechnicalPassword
    );
    var headers = this.securityHeaderSupplier
        .getKeycloakAndCsrfHttpHeaders(keycloakLoginResponseDTO.getAccessToken());
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }
}