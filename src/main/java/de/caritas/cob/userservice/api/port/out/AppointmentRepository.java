package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Appointment;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AppointmentRepository extends CrudRepository<Appointment, UUID> {

  Optional<Appointment> findByBookingId(Integer bookingId);

  @Query(
      value =
          "SELECT * "
              + "FROM appointment a "
              + "WHERE a.consultant_id = :userId AND a.datetime >= :datetime "
              + "ORDER BY a.datetime",
      nativeQuery = true)
  List<Appointment> findAllOrderByDatetimeAfter(Instant datetime, String userId);

  @Modifying
  @Query(value = "DELETE FROM appointment WHERE `datetime` <= :datetime", nativeQuery = true)
  void deleteOlderThan(Instant datetime);
}
