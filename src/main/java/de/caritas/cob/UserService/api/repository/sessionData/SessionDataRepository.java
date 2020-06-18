package de.caritas.cob.UserService.api.repository.sessionData;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import de.caritas.cob.UserService.api.repository.session.Session;

public interface SessionDataRepository extends CrudRepository<SessionData, Long> {

  /**
   * Find the {@link SessionData} by {@link Session} id
   * 
   * @param sessionId
   * @return List of {@link SessionData}
   */
  List<SessionData> findBySessionId(Long sessionId);

}
