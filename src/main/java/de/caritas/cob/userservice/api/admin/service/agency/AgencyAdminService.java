package de.caritas.cob.userservice.api.admin.service.agency;

import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.agencyadminserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.AdminAgencyControllerApi;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminFullResponseDTO;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.service.httpheader.OriginHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service to wrap admin api call for agencies.
 */
@Service
@RequiredArgsConstructor
public class AgencyAdminService {

  private final @NonNull AdminAgencyControllerApi adminAgencyControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull OriginHeaderSupplier originHeaderSupplier;
  private final @NonNull RestTemplate restTemplate;
  @Value("${agency.admin.service.api.url}")
  private String agencyAdminServiceApiUrl;

  /**
   * Retrieves all agencies provided by agency service. Important hint: Depending on the amount of
   * existing agencies that call might need a few seconds to be performed.
   *
   * @return all existing agencies
   */
  public List<AgencyAdminResponseDTO> retrieveAllAgencies() {
    var apiClient = new ApiClient(restTemplate).setBasePath(this.agencyAdminServiceApiUrl);
    this.adminAgencyControllerApi.setApiClient(apiClient);
    addDefaultHeaders(this.adminAgencyControllerApi.getApiClient());
    return requireNonNull(this.adminAgencyControllerApi.searchAgencies(0, Integer.MAX_VALUE, null)
        .getEmbedded())
        .stream()
        .map(AgencyAdminFullResponseDTO::getEmbedded)
        .collect(Collectors.toList());
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    addOriginHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  private void addOriginHeader(HttpHeaders headers) {
    String originHeaderValue = originHeaderSupplier.getOriginHeaderValue();
    if (originHeaderValue != null) {
      headers.add("origin", originHeaderValue);
    }
  }



}
