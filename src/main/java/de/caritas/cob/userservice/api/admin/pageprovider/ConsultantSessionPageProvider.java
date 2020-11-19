package de.caritas.cob.userservice.api.admin.pageprovider;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.userservice.api.model.Filter;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
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
  private final @NonNull Filter filter;

  /**
   * Executes the search query on the repository.
   *
   * @param pageable the pageable to split the results
   * @return a {@link Page} object containing the results
   */
  @Override
  public Page<Session> executeQuery(Pageable pageable) {
    return this.sessionRepository.findByConsultantId(filter.getConsultant(), pageable);
  }

  /**
   * Validates the consultant filter.
   *
   * @return true if filter has consultant set
   */
  @Override
  public boolean supports() {
    return isNotBlank(this.filter.getConsultant());
  }
}
