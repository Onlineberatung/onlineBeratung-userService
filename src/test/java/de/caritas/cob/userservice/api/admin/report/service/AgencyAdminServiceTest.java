package de.caritas.cob.userservice.api.admin.report.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.agencyadminserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyadminserivce.generated.web.AdminAgencyControllerApi;
import de.caritas.cob.userservice.api.admin.service.agency.AgencyAdminService;
import de.caritas.cob.userservice.api.config.apiclient.AgencyAdminServiceApiControllerFactory;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class AgencyAdminServiceTest {

  @InjectMocks private AgencyAdminService agencyAdminService;
  @Mock private SecurityHeaderSupplier securityHeaderSupplier;

  @Mock private TenantHeaderSupplier tenantHeaderSupplier;

  @Mock private RestTemplate restTemplate;

  @Mock private AgencyAdminServiceApiControllerFactory agencyAdminServiceApiControllerFactory;

  @Test
  public void agencyAdminControllerShouldHaveCorrectHeaders() {
    var headers = new HttpHeaders();
    headers.add("header1", "header1");
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    AdminAgencyControllerApi api = new AdminAgencyControllerApi();
    api.setApiClient(new ApiClient());
    when(agencyAdminServiceApiControllerFactory.createControllerApi()).thenReturn(api);

    AdminAgencyControllerApi controllerApi =
        this.agencyAdminServiceApiControllerFactory.createControllerApi();
    ApiClient apiClient = controllerApi.getApiClient();
    agencyAdminService.addDefaultHeaders(apiClient);

    HttpHeaders defaultHeaders =
        (HttpHeaders) ReflectionTestUtils.getField(apiClient, "defaultHeaders");
    assertEquals("header1", defaultHeaders.get("header1").get(0));
  }
}
