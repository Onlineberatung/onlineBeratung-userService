package de.caritas.cob.userservice.api.admin.pageprovider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import de.caritas.cob.userservice.api.model.Filter;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageProviderFactoryTest {

  @Mock
  private SessionRepository sessionRepository;

  @Test
  public void retrieveFirstSupportedSessionPageProvider_Should_returnAllSessionPageProvider_When_noFilterIsSet() {
    Filter filter = new Filter();

    SessionPageProvider resultProvider = PageProviderFactory.getInstance(sessionRepository, filter)
        .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(AllSessionPageProvider.class));
  }

  @Test
  public void retrieveFirstSupportedSessionPageProvider_Should_returnAgencySessionPageProvider_When_agencyFilterIsSet() {
    Filter filter = new Filter().agency(5);

    SessionPageProvider resultProvider = PageProviderFactory.getInstance(sessionRepository, filter)
        .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(AgencySessionPageProvider.class));
  }

  @Test
  public void retrieveFirstSupportedSessionPageProvider_Should_returnAskerSessionPageProvider_When_askerFilterIsSet() {
    Filter filter = new Filter().asker("asker");

    SessionPageProvider resultProvider = PageProviderFactory.getInstance(sessionRepository, filter)
        .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(AskerSessionPageProvider.class));
  }

  @Test
  public void retrieveFirstSupportedSessionPageProvider_Should_returnConsultantSessionPageProvider_When_consultantFilterIsSet() {
    Filter filter = new Filter().consultant("consultant");

    SessionPageProvider resultProvider = PageProviderFactory.getInstance(sessionRepository, filter)
        .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(ConsultantSessionPageProvider.class));
  }

  @Test
  public void retrieveFirstSupportedSessionPageProvider_Should_returnConsultingTypeSessionPageProvider_When_consultingTypeFilterIsSet() {
    Filter filter = new Filter().consultingType(5);

    SessionPageProvider resultProvider = PageProviderFactory.getInstance(sessionRepository, filter)
        .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(ConsultingTypeSessionPageProvider.class));
  }

  @Test
  public void retrieveFirstSupportedSessionPageProvider_Should_returnAgencySessionPageProvider_When_allFiltersAreSet() {
    Filter filter = new Filter()
        .agency(10)
        .asker("asker")
        .consultant("consultant")
        .consultingType(5);

    SessionPageProvider resultProvider = PageProviderFactory.getInstance(sessionRepository, filter)
        .retrieveFirstSupportedSessionPageProvider();

    assertThat(resultProvider, instanceOf(AgencySessionPageProvider.class));
  }

}
