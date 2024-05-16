package de.caritas.cob.userservice.api.admin.service.session.pageprovider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AgencySessionPageProviderTest {

  @InjectMocks private AgencySessionPageProvider agencySessionPageProvider;

  @Mock private SessionRepository sessionRepository;

  @Mock private SessionFilter sessionFilter;

  @Test
  void supports_Should_returnTrue_When_agencyFilterIsSet() {
    when(this.sessionFilter.getAgency()).thenReturn(1);

    boolean supports = this.agencySessionPageProvider.isSupported();

    assertThat(supports, is(true));
  }

  @Test
  void supports_Should_returnFalse_When_agencyFilterIsNotSet() {
    when(this.sessionFilter.getAgency()).thenReturn(null);

    boolean supports = this.agencySessionPageProvider.isSupported();

    assertThat(supports, is(false));
  }

  @Test
  void executeQuery_Should_executeQueryOnRepository_When_pagebleIsGiven() {
    when(this.sessionFilter.getAgency()).thenReturn(1);
    PageRequest pageable = PageRequest.of(0, 1);

    this.agencySessionPageProvider.executeQuery(pageable);

    verify(this.sessionRepository, atLeastOnce()).findByAgencyId(1L, pageable);
  }
}
