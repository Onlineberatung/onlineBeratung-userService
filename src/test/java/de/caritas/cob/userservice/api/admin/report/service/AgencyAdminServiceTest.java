package de.caritas.cob.userservice.api.admin.report.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.agencyadminserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.AdminAgencyControllerApi;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminFullResponseDTO;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminSearchResultDTO;
import de.caritas.cob.userservice.api.admin.service.agency.AgencyAdminService;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

@RunWith(MockitoJUnitRunner.class)
public class AgencyAdminServiceTest {

  @InjectMocks
  private AgencyAdminService agencyAdminService;

  @Mock
  private AdminAgencyControllerApi adminAgencyControllerApi;

  @Mock
  private SecurityHeaderSupplier securityHeaderSupplier;

  @Mock
  private TenantHeaderSupplier tenantHeaderSupplier;

  @Mock
  private ApiClient apiClient;

  @Test
  public void retrieveAllAgencies_Should_useSerivcesCorrectly() {
    when(adminAgencyControllerApi.getApiClient()).thenReturn(apiClient);
    when(adminAgencyControllerApi.searchAgencies(any(), any(), any()))
        .thenReturn(new AgencyAdminSearchResultDTO()
            .addEmbeddedItem(new AgencyAdminFullResponseDTO()));
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(new HttpHeaders());

    this.agencyAdminService.retrieveAllAgencies();

    verify(this.adminAgencyControllerApi, times(1))
        .searchAgencies(0, Integer.MAX_VALUE, null);
  }

}
