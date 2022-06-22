package de.caritas.cob.userservice.api.admin.service.session.pageprovider;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Page provider for {@link Session} filtered by consultant.
 */
@RequiredArgsConstructor
public class ConsultantSessionPageProvider implements SessionPageProvider {

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
    return this.sessionRepository.findByConsultantId(sessionFilter.getConsultant(), pageable);
  }

  /**
   * Validates the consultant filter.
   *
   * @return true if filter has consultant set
   */
  @Override
  public boolean isSupported() {
    return isNotBlank(this.sessionFilter.getConsultant());
  }
}
