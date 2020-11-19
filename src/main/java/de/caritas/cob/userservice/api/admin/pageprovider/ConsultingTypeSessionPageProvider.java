package de.caritas.cob.userservice.api.admin.pageprovider;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.model.Filter;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Page provider for {@link Session} filtered by {@link ConsultingType}.
 */
@RequiredArgsConstructor
public class ConsultingTypeSessionPageProvider implements SessionPageProvider {

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
    Integer type = filter.getConsultingType();
    Optional<ConsultingType> consultingType = ConsultingType.valueOf(type);
    if (consultingType.isPresent()) {
      return this.sessionRepository.findByConsultingType(consultingType.get(), pageable);
    }
    return Page.empty(pageable);
  }

  /**
   * Validates the consultant type filter.
   *
   * @return true if filter has consulting type set
   */
  @Override
  public boolean supports() {
    return nonNull(this.filter.getConsultingType());
  }
}
