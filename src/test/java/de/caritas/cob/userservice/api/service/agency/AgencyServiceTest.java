package de.caritas.cob.userservice.api.service.agency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import de.caritas.cob.userservice.agencyserivce.generated.web.AgencyControllerApi;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgencyServiceTest {

  @InjectMocks
  AgencyService agencyService;

  @Mock
  AgencyControllerApi agencyControllerApi;

  @Mock
  SecurityHeaderSupplier securityHeaderSupplier;

  @ParameterizedTest
  @NullAndEmptySource
  void getAgenciesFromAgencyService_Should_returnEmptyList_When_nullPassed(List<Long> emptyIds) {
    List<AgencyDTO> result = this.agencyService.getAgencies(emptyIds);

    assertThat(result, hasSize(0));
  }

}
