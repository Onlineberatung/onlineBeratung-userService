package de.caritas.cob.userservice.api.service.agency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.config.apiclient.AgencyServiceApiControllerFactory;
import de.caritas.cob.userservice.api.service.httpheader.HttpHeadersResolver;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AgencyServiceTest {

  private static final String ORIGIN_URL = "subdomain.onlineberatung.net";

  @InjectMocks AgencyService agencyService;

  @Mock AgencyControllerApi agencyControllerApi;

  @Mock AgencyServiceApiControllerFactory agencyServiceApiControllerFactory;

  @Mock TenantHeaderSupplier tenantHeaderSupplier;

  @Mock SecurityHeaderSupplier securityHeaderSupplier;

  @Mock ApiClient apiClient;

  @ParameterizedTest
  @NullAndEmptySource
  void getAgenciesFromAgencyService_Should_returnEmptyList_When_nullPassed(List<Long> emptyIds) {
    List<AgencyDTO> result = this.agencyService.getAgencies(emptyIds);
    assertThat(result).isEmpty();
  }

  @Test
  void getAgenciesFromAgencyService_Should_passTenantId() {
    TenantContext.setCurrentTenant(1L);
    TenantHeaderSupplier tenantHeaderSupplier = new TenantHeaderSupplier(new HttpHeadersResolver());
    ReflectionTestUtils.setField(tenantHeaderSupplier, "multitenancy", true);
    ReflectionTestUtils.setField(agencyService, "tenantHeaderSupplier", tenantHeaderSupplier);
    HttpHeaders headers = new HttpHeaders();
    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(headers);
    when(this.agencyControllerApi.getApiClient()).thenReturn(apiClient);
    var agencyDTOS =
        Lists.newArrayList(
            new de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO());
    when(agencyServiceApiControllerFactory.createControllerApi()).thenReturn(agencyControllerApi);
    when(this.agencyControllerApi.getAgenciesByIds(Lists.newArrayList(1L))).thenReturn(agencyDTOS);

    this.agencyService.getAgency(1L);

    assertThat(headers.get("tenantId").get(0)).isEqualTo("1");
    TenantContext.clear();
  }
}
