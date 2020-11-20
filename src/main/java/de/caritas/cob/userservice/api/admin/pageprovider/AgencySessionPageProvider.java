package de.caritas.cob.userservice.api.admin.pageprovider;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.model.Filter;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Page provider for {@link Session} filtered by agency.
 */
@RequiredArgsConstructor
public class AgencySessionPageProvider implements SessionPageProvider {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull Filter filter;

  /**
   * Executes the search query on the repository.
   *
   * @param pageable the pageable to split the results
   * @return a {@link Page} object containing the results
   */
  @Override
  public Page<Session> executeQuery(Pageable pageable) {
    return this.sessionRepository.findByAgencyId(filter.getAgency().longValue(), pageable);
  }

  /**
   * Validates the agency filter.
   *
   * @return true if filter has agency set
   */
  @Override
  public boolean isSupported() {
    return nonNull(this.filter.getAgency());
  }
}
