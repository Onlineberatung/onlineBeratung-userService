package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Appointment;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface AppointmentRepository extends CrudRepository<Appointment, UUID> {

}
