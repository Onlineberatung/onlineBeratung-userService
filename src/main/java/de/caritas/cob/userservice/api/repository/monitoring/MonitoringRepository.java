package de.caritas.cob.userservice.api.repository.monitoring;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import de.caritas.cob.userservice.api.repository.session.Session;

public interface MonitoringRepository extends CrudRepository<Monitoring, Long> {

  /**
   * Find a {@link Monitoring} by a {@link Session} id
   * 
   * @param sessionId
   * @return
   */
  List<Monitoring> findBySessionId(Long sessionId);
}
