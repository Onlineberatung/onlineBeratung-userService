package de.caritas.cob.userservice.api.admin.service.session.pageprovider;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import java.util.List;

/**
 * Factory class to decide which {@link SessionPageProvider} should be used for special {@link
 * SessionFilter}.
 */
public class PageProviderFactory {

  private final List<SessionPageProvider> pageProviderRegistry;
  private final SessionPageProvider allSessionsPageProvider;

  private PageProviderFactory(SessionRepository sessionRepository, SessionFilter sessionFilter) {
    this.pageProviderRegistry =
        asList(
            new AgencySessionPageProvider(sessionRepository, sessionFilter),
            new AskerSessionPageProvider(sessionRepository, sessionFilter),
            new ConsultantSessionPageProvider(sessionRepository, sessionFilter),
            new ConsultingTypeSessionPageProvider(sessionRepository, sessionFilter));
    this.allSessionsPageProvider = new AllSessionPageProvider(sessionRepository);
  }

  public static PageProviderFactory getInstance(
      SessionRepository sessionRepository, SessionFilter sessionFilter) {
    return new PageProviderFactory(
        requireNonNull(sessionRepository), requireNonNull(sessionFilter));
  }

  /**
   * Retrieves the first supported {@link SessionPageProvider} by given {@link SessionFilter}.
   * Returns the {@link AllSessionPageProvider} if no filter is set.
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
