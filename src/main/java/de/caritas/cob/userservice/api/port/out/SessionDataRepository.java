package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.SessionData;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SessionDataRepository extends CrudRepository<SessionData, Long> {

  /**
   * Find the {@link SessionData} by {@link Session} ID
   *
   * @param sessionId the session ID
   * @return List of {@link SessionData}
   */
  List<SessionData> findBySessionId(Long sessionId);
}
