package de.caritas.cob.userservice.api.service.appointment;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.config.apiclient.AppointmentAgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.config.apiclient.AppointmentConsultantServiceApiControllerFactory;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.appointmentservice.generated.ApiClient;
import de.caritas.cob.userservice.appointmentservice.generated.web.AgencyApi;
import de.caritas.cob.userservice.appointmentservice.generated.web.ConsultantApi;
import de.caritas.cob.userservice.appointmentservice.generated.web.model.AgencyConsultantSyncRequestDTO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/** Service class to communicate with the AppointmentService. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

  private final @NonNull AppointmentConsultantServiceApiControllerFactory
      appointmentConsultantServiceApiControllerFactory;

  private final @NonNull AppointmentAgencyServiceApiControllerFactory
      appointmentAgencyServiceApiControllerFactory;

  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;
  private final @NonNull IdentityClient identityClient;

  @Value("${feature.appointment.enabled}")
  private boolean appointmentFeatureEnabled;

  @Value("${keycloakService.technical.username}")
  private String keycloakTechnicalUsername;

  @Value("${keycloakService.technical.password}")
  private String keycloakTechnicalPassword;

  public void createConsultant(ConsultantAdminResponseDTO consultantAdminResponseDTO) {
    if (!appointmentFeatureEnabled) {
      return;
    }

    if (consultantAdminResponseDTO != null) {
      ObjectMapper mapper = getObjectMapper(false);
      ConsultantApi appointmentConsultantApi =
          this.appointmentConsultantServiceApiControllerFactory.createControllerApi();
      addTechnicalUserHeaders(appointmentConsultantApi.getApiClient());
      try {
        de.caritas.cob.userservice.appointmentservice.generated.web.model.ConsultantDTO consultant =
            mapper.readValue(
                mapper.writeValueAsString(consultantAdminResponseDTO.getEmbedded()),
                de.caritas.cob.userservice.appointmentservice.generated.web.model.ConsultantDTO
                    .class);
        appointmentConsultantApi.createConsultant(consultant);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
  }

  public void updateConsultant(Consultant consultant) {
    ConsultantAdminResponseDTO ConsultantAdminResponseDTO = new ConsultantAdminResponseDTO();
    ConsultantDTO consultantEmbeded = new ConsultantDTO();
    consultantEmbeded.setId(consultant.getId());
    consultantEmbeded.setFirstname(consultant.getFirstName());
    consultantEmbeded.setLastname(consultant.getLastName());
    consultantEmbeded.setEmail(consultant.getEmail());
    consultantEmbeded.setAbsent(consultant.isAbsent());
    ConsultantAdminResponseDTO.setEmbedded(consultantEmbeded);
    updateConsultant(ConsultantAdminResponseDTO);
  }

  public void updateConsultant(ConsultantAdminResponseDTO consultantAdminResponseDTO) {
    if (!appointmentFeatureEnabled) {
      return;
    }
    ConsultantApi appointmentConsultantApi =
        this.appointmentConsultantServiceApiControllerFactory.createControllerApi();

    if (consultantAdminResponseDTO != null) {
      ObjectMapper mapper = getObjectMapper(false);
      addTechnicalUserHeaders(appointmentConsultantApi.getApiClient());
      try {
        de.caritas.cob.userservice.appointmentservice.generated.web.model.ConsultantDTO consultant =
            mapper.readValue(
                mapper.writeValueAsString(consultantAdminResponseDTO.getEmbedded()),
                de.caritas.cob.userservice.appointmentservice.generated.web.model.ConsultantDTO
                    .class);
        appointmentConsultantApi.updateConsultant(consultant.getId(), consultant);
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
  }

  /**
   * ObjectMapper provider method for injecting Mocks while testing
   *
   * @param failOnUnknownProperties DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
   * @return ObjectMapper
   */
  protected ObjectMapper getObjectMapper(boolean failOnUnknownProperties) {
    return new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);
  }

  public void deleteConsultant(String consultantId) {
    if (!appointmentFeatureEnabled) {
      return;
    }
    ConsultantApi appointmentConsultantApi =
        this.appointmentConsultantServiceApiControllerFactory.createControllerApi();

    if (consultantId != null && !consultantId.isEmpty()) {
      addTechnicalUserHeaders(appointmentConsultantApi.getApiClient());
      try {
        appointmentConsultantApi.deleteConsultant(consultantId);
      } catch (HttpClientErrorException ex) {
        acceptDeletionIfConsultantNotFoundInAppointmentService(ex, consultantId);
      }
    }
  }

  private void acceptDeletionIfConsultantNotFoundInAppointmentService(
      HttpClientErrorException ex, String consultantId) {
    if (!HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
      throw ex;
    } else {
      log.warn(
          "No consultant with id {} was found in appointmentService. Proceeding with deletion.",
          consultantId);
    }
  }

  private void addTechnicalUserHeaders(ApiClient apiClient) {
    var keycloakLoginResponseDTO =
        identityClient.loginUser(keycloakTechnicalUsername, keycloakTechnicalPassword);
    var headers =
        this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders(
            keycloakLoginResponseDTO.getAccessToken());
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  public void syncAgencies(String consultantId, List<CreateConsultantAgencyDTO> agencyList) {
    if (!appointmentFeatureEnabled) {
      return;
    }

    AgencyApi controllerApi =
        this.appointmentAgencyServiceApiControllerFactory.createControllerApi();

    addTechnicalUserHeaders(controllerApi.getApiClient());
    var agencies =
        agencyList.stream()
            .map(CreateConsultantAgencyDTO::getAgencyId)
            .collect(Collectors.toList());
    AgencyConsultantSyncRequestDTO request = new AgencyConsultantSyncRequestDTO();
    request.setAgencies(agencies);
    request.setConsultantId(consultantId);
    controllerApi.agencyConsultantsSync(request);
  }
}
