package de.caritas.cob.userservice.api.admin.service.session.pageprovider;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.SessionFilter;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import java.util.Arrays;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Page provider for {@link Session} filtered by the consulting ID.
 */
@RequiredArgsConstructor
public class ConsultingTypeSessionPageProvider implements SessionPageProvider {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull SessionFilter sessionFilter;
  private final @NonNull ConsultingTypeManager consultingTypeManager;

  /**
   * Executes the search query on the repository.
   *
   * @param pageable the pageable to split the results
   * @return a {@link Page} object containing the results
   */
  @Override
  public Page<Session> executeQuery(Pageable pageable) {
    Optional<Integer> consultingTypeId = Arrays
        .stream(consultingTypeManager.getAllConsultingTypeIds()).filter(id -> id
            .equals(sessionFilter.getConsultingType())).findFirst();

    if (consultingTypeId.isPresent()) {
      return this.sessionRepository.findByConsultingTypeId(consultingTypeId.get(), pageable);
    } //TODO will be deleted
    return Page.empty(pageable);
  }

  /**
   * Validates the consultant type filter.
   *
   * @return true if filter has consulting type set
   */
  @Override
  public boolean isSupported() {
    return nonNull(this.sessionFilter.getConsultingType());
  }
}
