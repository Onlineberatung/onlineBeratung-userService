package de.caritas.cob.userservice.api.admin.report.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.AdminAgencyControllerApi;
import de.caritas.cob.userservice.api.admin.service.agency.AgencyAdminService;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AgencyAdminServiceTest {

  @InjectMocks
  private AgencyAdminService agencyAdminService;

  @Mock
  private SecurityHeaderSupplier securityHeaderSupplier;

  @Mock
  private TenantHeaderSupplier tenantHeaderSupplier;

  @Test
  public void agencyAdminControllerShouldHaveCorrectHeaders() {
    ReflectionTestUtils
        .setField(agencyAdminService, "agencyAdminServiceApiUrl", "http://onlineberatung.net");
    var headers = new HttpHeaders();
    headers.add("header1", "header1");
    when(securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    AdminAgencyControllerApi controllerApi = this.agencyAdminService.createControllerApi();

    HttpHeaders defaultHeaders = (HttpHeaders) ReflectionTestUtils
        .getField(controllerApi.getApiClient(), "defaultHeaders");

    assertThat(defaultHeaders.get("header1").get(0), is("header1"));
  }
}
