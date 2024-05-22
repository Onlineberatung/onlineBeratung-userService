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
public class ConsultantSessionPageProviderTest {

  @InjectMocks private ConsultantSessionPageProvider consultantSessionPageProvider;

  @Mock private SessionRepository sessionRepository;

  @Mock private SessionFilter sessionFilter;

  @Test
  public void supports_Should_returnTrue_When_consultantFilterIsSet() {
    when(this.sessionFilter.getConsultant()).thenReturn("consultant");

    boolean supports = this.consultantSessionPageProvider.isSupported();

    assertThat(supports, is(true));
  }

  @Test
  public void supports_Should_returnFalse_When_consultantFilterIsNotSet() {
    when(this.sessionFilter.getConsultant()).thenReturn(null);

    boolean supports = this.consultantSessionPageProvider.isSupported();

    assertThat(supports, is(false));
  }

  @Test
  public void executeQuery_Should_executeQueryOnRepository_When_pagebleIsGiven() {
    when(this.sessionFilter.getConsultant()).thenReturn("consultant");
    PageRequest pageable = PageRequest.of(0, 1);

    this.consultantSessionPageProvider.executeQuery(pageable);

    verify(this.sessionRepository, atLeastOnce()).findByConsultantId("consultant", pageable);
  }
}
