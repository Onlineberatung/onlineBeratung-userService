package de.caritas.cob.userservice.api.admin.service.session;

import de.caritas.cob.userservice.api.admin.service.session.pageprovider.PageProviderFactory;
import de.caritas.cob.userservice.api.admin.service.session.pageprovider.SessionPageProvider;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.model.SessionFilter;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service class to handle administrative operations on sessions.
 */
@Service
@RequiredArgsConstructor
public class SessionAdminService {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull ConsultingTypeManager consultingTypeManager;

  /**
   * Finds existing sessions filtered by {@link SessionFilter} and retrieves all sessions if no filter
   * is set.
   *
   * @param page the current page
   * @param perPage number of items per page
   * @param sessionFilter criteria to filter on sessions
   * @return a generated {@link SessionAdminResultDTO} containing the results
   */
  public SessionAdminResultDTO findSessions(Integer page, Integer perPage, SessionFilter sessionFilter) {
    Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(perPage, 1));

    SessionPageProvider sessionPageProvider =
        PageProviderFactory.getInstance(this.sessionRepository, sessionFilter, consultingTypeManager)
            .retrieveFirstSupportedSessionPageProvider();

    return SessionAdminResultDTOBuilder.getInstance()
        .withPage(page)
        .withPerPage(perPage)
        .withFilter(sessionFilter)
        .withResultPage(sessionPageProvider.executeQuery(pageable))
        .build();
  }

}
