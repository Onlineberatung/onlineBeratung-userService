package de.caritas.cob.userservice.api.admin.service.session.pageprovider;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Page provider for {@link Session} filtered by agency. */
@RequiredArgsConstructor
public class AgencySessionPageProvider implements SessionPageProvider {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull SessionFilter sessionFilter;

  /**
   * Executes the search query on the repository.
   *
   * @param pageable the pageable to split the results
   * @return a {@link Page} object containing the results
   */
  @Override
  public Page<Session> executeQuery(Pageable pageable) {
    return this.sessionRepository.findByAgencyId(sessionFilter.getAgency().longValue(), pageable);
  }

  /**
   * Validates the agency filter.
   *
   * @return true if filter has agency set
   */
  @Override
  public boolean isSupported() {
    return nonNull(this.sessionFilter.getAgency());
  }
}
