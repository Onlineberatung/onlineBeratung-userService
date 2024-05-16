package de.caritas.cob.userservice.api.admin.service.session.pageprovider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PageProviderFactoryTest {

  @Mock private SessionRepository sessionRepository;

  @Test
  public void
      retrieveFirstSupportedSessionPageProvider_Should_returnAllSessionPageProvider_When_noFilterIsSet() {
    SessionFilter sessionFilter = new SessionFilter();

    SessionPageProvider resultProvider =
        PageProviderFactory.getInstance(sessionRepository, sessionFilter)
            .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(AllSessionPageProvider.class));
  }

  @Test
  public void
      retrieveFirstSupportedSessionPageProvider_Should_returnAgencySessionPageProvider_When_agencyFilterIsSet() {
    SessionFilter sessionFilter = new SessionFilter().agency(5);

    SessionPageProvider resultProvider =
        PageProviderFactory.getInstance(sessionRepository, sessionFilter)
            .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(AgencySessionPageProvider.class));
  }

  @Test
  public void
      retrieveFirstSupportedSessionPageProvider_Should_returnAskerSessionPageProvider_When_askerFilterIsSet() {
    SessionFilter sessionFilter = new SessionFilter().asker("asker");

    SessionPageProvider resultProvider =
        PageProviderFactory.getInstance(sessionRepository, sessionFilter)
            .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(AskerSessionPageProvider.class));
  }

  @Test
  public void
      retrieveFirstSupportedSessionPageProvider_Should_returnConsultantSessionPageProvider_When_consultantFilterIsSet() {
    SessionFilter sessionFilter = new SessionFilter().consultant("consultant");

    SessionPageProvider resultProvider =
        PageProviderFactory.getInstance(sessionRepository, sessionFilter)
            .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(ConsultantSessionPageProvider.class));
  }

  @Test
  public void
      retrieveFirstSupportedSessionPageProvider_Should_returnConsultingTypeSessionPageProvider_When_consultingTypeFilterIsSet() {
    SessionFilter sessionFilter = new SessionFilter().consultingType(5);

    SessionPageProvider resultProvider =
        PageProviderFactory.getInstance(sessionRepository, sessionFilter)
            .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(ConsultingTypeSessionPageProvider.class));
  }

  @Test
  public void
      retrieveFirstSupportedSessionPageProvider_Should_returnAgencySessionPageProvider_When_allFiltersAreSet() {
    SessionFilter sessionFilter =
        new SessionFilter().agency(10).asker("asker").consultant("consultant").consultingType(5);

    SessionPageProvider resultProvider =
        PageProviderFactory.getInstance(sessionRepository, sessionFilter)
            .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(AgencySessionPageProvider.class));
  }
}
