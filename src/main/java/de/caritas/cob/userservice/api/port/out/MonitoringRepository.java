package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Monitoring;
import de.caritas.cob.userservice.api.model.Session;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface MonitoringRepository extends CrudRepository<Monitoring, Long> {

  /**
   * Find a {@link Monitoring} by a {@link Session} id
   *
   * @param sessionId session ID
   * @return monitoring list
   */
  List<Monitoring> findBySessionId(Long sessionId);
}
