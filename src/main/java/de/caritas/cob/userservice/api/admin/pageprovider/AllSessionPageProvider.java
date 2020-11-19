package de.caritas.cob.userservice.api.admin.pageprovider;

import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Page provider for {@link Session} unfiltered.
 */
@RequiredArgsConstructor
public class AllSessionPageProvider implements SessionPageProvider {

  private final @NonNull SessionRepository sessionRepository;

  /**
   * Executes the search query on the repository.
   *
   * @param pageable the pageable to split the results
   * @return a {@link Page} object containing the results
   */
  @Override
  public Page<Session> executeQuery(Pageable pageable) {
    return sessionRepository.findAll(pageable);
  }

  /**
   * Is always true because full query can always be executed.
   *
   * @return true
   */
  @Override
  public boolean supports() {
    return true;
  }
}
