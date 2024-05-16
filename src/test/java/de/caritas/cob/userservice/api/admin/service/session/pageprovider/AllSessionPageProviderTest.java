package de.caritas.cob.userservice.api.admin.service.session.pageprovider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.port.out.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AllSessionPageProviderTest {

  @InjectMocks private AllSessionPageProvider allSessionPageProvider;

  @Mock private SessionRepository sessionRepository;

  @Test
  void supports_Should_returnTrue() {
    boolean supports = this.allSessionPageProvider.isSupported();

    assertThat(supports, is(true));
  }

  @Test
  void executeQuery_Should_executeQueryOnRepository_When_pagebleIsGiven() {
    PageRequest pageable = PageRequest.of(0, 1);

    this.allSessionPageProvider.executeQuery(pageable);

    verify(this.sessionRepository, atLeastOnce()).findAll(pageable);
  }
}
