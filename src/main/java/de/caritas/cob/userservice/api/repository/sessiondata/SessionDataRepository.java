package de.caritas.cob.userservice.api.repository.sessiondata;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import de.caritas.cob.userservice.api.repository.session.Session;

public interface SessionDataRepository extends CrudRepository<SessionData, Long> {

  /**
   * Find the {@link SessionData} by {@link Session} ID
   * 
   * @param sessionId the session ID
   * @return List of {@link SessionData}
   */
  List<SessionData> findBySessionId(Long sessionId);
}
