package de.caritas.cob.userservice.api.admin.pageprovider;

import de.caritas.cob.userservice.api.repository.session.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Defines functionality to provide paged {@link Session}.
 */
public interface SessionPageProvider {

  /**
   * Executes the repository method.
   *
   * @param pageable the pageable to split the results
   * @return a {@link Page} containing the results
   */
  Page<Session> executeQuery(Pageable pageable);

  /**
   * Decides if the implementation supports a special query.
   *
   * @return true if query execution is supported
   */
  default boolean isSupported() {
    return true;
  }

}
