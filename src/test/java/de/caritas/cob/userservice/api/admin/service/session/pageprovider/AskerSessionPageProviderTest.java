package de.caritas.cob.userservice.api.admin.service.session.pageprovider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;

@RunWith(MockitoJUnitRunner.class)
public class AskerSessionPageProviderTest {

  @InjectMocks private AskerSessionPageProvider askerSessionPageProvider;

  @Mock private SessionRepository sessionRepository;

  @Mock private SessionFilter sessionFilter;

  @Test
  public void supports_Should_returnTrue_When_askerFilterIsSet() {
    when(this.sessionFilter.getAsker()).thenReturn("asker");

    boolean supports = this.askerSessionPageProvider.isSupported();

    assertThat(supports, is(true));
  }

  @Test
  public void supports_Should_returnFalse_When_askerFilterIsNotSet() {
    when(this.sessionFilter.getAsker()).thenReturn(null);

    boolean supports = this.askerSessionPageProvider.isSupported();

    assertThat(supports, is(false));
  }

  @Test
  public void executeQuery_Should_executeQueryOnRepository_When_pagebleIsGiven() {
    when(this.sessionFilter.getAsker()).thenReturn("asker");
    PageRequest pageable = PageRequest.of(0, 1);

    this.askerSessionPageProvider.executeQuery(pageable);

    verify(this.sessionRepository, atLeastOnce()).findByUserUserId("asker", pageable);
  }
}
