package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Appointment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AppointmentRepository extends CrudRepository<Appointment, UUID> {

  @Query(value = "SELECT * FROM appointment a WHERE a.datetime >= :datetime ORDER BY a.datetime ASC", nativeQuery = true)
  List<Appointment> findAllOrderByDatetimeAfter(Instant datetime);
}
