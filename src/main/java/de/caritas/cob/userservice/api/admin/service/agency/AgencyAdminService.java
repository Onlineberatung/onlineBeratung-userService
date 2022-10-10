package de.caritas.cob.userservice.api.admin.service.agency;

import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.agencyadminserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.AdminAgencyControllerApi;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminFullResponseDTO;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.config.apiclient.AgencyAdminServiceApiControllerFactory;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/** Service to wrap admin api call for agencies. */
@Service
@RequiredArgsConstructor
public class AgencyAdminService {

  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;

  private final @NonNull AgencyAdminServiceApiControllerFactory
      agencyAdminServiceApiControllerFactory;

  /**
   * Retrieves all agencies provided by agency service. Important hint: Depending on the amount of
   * existing agencies that call might need a few seconds to be performed.
   *
   * @return all existing agencies
   */
  public List<AgencyAdminResponseDTO> retrieveAllAgencies() {
    AdminAgencyControllerApi controllerApi =
        agencyAdminServiceApiControllerFactory.createControllerApi();
    addDefaultHeaders(controllerApi.getApiClient());
    return requireNonNull(
            controllerApi.searchAgencies(0, Integer.MAX_VALUE, null, null).getEmbedded())
        .stream()
        .map(AgencyAdminFullResponseDTO::getEmbedded)
        .collect(Collectors.toList());
  }

  public void addDefaultHeaders(ApiClient apiClient) {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }
}
