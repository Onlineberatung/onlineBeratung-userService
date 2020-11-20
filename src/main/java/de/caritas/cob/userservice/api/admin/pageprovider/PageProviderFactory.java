package de.caritas.cob.userservice.api.admin.pageprovider;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.model.Filter;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import java.util.List;

/**
 * Factory class to decide which {@link SessionPageProvider} should be used for special
 * {@link Filter}.
 */
public class PageProviderFactory {

  private final List<SessionPageProvider> pageProviderRegistry;
  private final SessionPageProvider allSessionsPageProvider;

  private PageProviderFactory(SessionRepository sessionRepository, Filter filter) {
    this.pageProviderRegistry = asList(
        new AgencySessionPageProvider(sessionRepository, filter),
        new AskerSessionPageProvider(sessionRepository, filter),
        new ConsultantSessionPageProvider(sessionRepository, filter),
        new ConsultingTypeSessionPageProvider(sessionRepository, filter)
    );
    this.allSessionsPageProvider = new AllSessionPageProvider(sessionRepository);
  }

  public static PageProviderFactory getInstance(SessionRepository sessionRepository,
      Filter filter) {
    return new PageProviderFactory(requireNonNull(sessionRepository), requireNonNull(filter));
  }

  /**
   * Retrieves the first supported {@link SessionPageProvider} by given {@link Filter}. Returns
   * the {@link AllSessionPageProvider} if no filter is set.
   *
   * @return the dedicated {@link SessionPageProvider}
   */
  public SessionPageProvider retrieveFirstSupportedSessionPageProvider() {
    return this.pageProviderRegistry.stream()
        .filter(SessionPageProvider::isSupported)
        .findFirst()
        .orElse(this.allSessionsPageProvider);
  }

}
