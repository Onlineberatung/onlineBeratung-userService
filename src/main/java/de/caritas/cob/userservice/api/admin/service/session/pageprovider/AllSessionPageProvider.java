package de.caritas.cob.userservice.api.admin.service.session.pageprovider;

import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Page provider for {@link Session} unfiltered. */
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
}
