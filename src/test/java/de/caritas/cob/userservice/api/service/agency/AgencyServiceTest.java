package de.caritas.cob.userservice.api.service.agency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


import com.google.common.collect.Lists;
import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.api.model.AgencyDTO;
import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import de.caritas.cob.userservice.agencyserivce.generated.ApiClient;

@ExtendWith(MockitoExtension.class)
class AgencyServiceTest {

  @InjectMocks
  AgencyService agencyService;

  @Mock
  AgencyControllerApi agencyControllerApi;

  @Mock
  SecurityHeaderSupplier securityHeaderSupplier;

  @Mock
  ApiClient apiClient;

  @ParameterizedTest
  @NullAndEmptySource
  void getAgenciesFromAgencyService_Should_returnEmptyList_When_nullPassed(List<Long> emptyIds) {
    // given, when
    List<AgencyDTO> result = this.agencyService.getAgencies(emptyIds);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void getAgenciesFromAgencyService_Should_passOriginalRequestHeaderIfCalledWithRequestServerNameParameter() {
    // given
    HttpHeaders headers = new HttpHeaders();
    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(headers);
    when(this.agencyControllerApi.getApiClient()).thenReturn(apiClient);
    var agencyDTOS = Lists.newArrayList(new de.caritas.cob.userservice.agencyserivce.generated.web.model.AgencyResponseDTO());
    when(this.agencyControllerApi.getAgenciesByIds(Lists.newArrayList(1L))).thenReturn(agencyDTOS);
    // when
    this.agencyService.getAgency(1L, "subdomain.onlineberatung.net");

    // then
    assertThat(headers.get("origin").get(0)).isEqualTo("subdomain.onlineberatung.net");
  }

}
