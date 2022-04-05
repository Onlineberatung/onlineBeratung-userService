package de.caritas.cob.userservice.api.port.in;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Organizing {

  Map<String, Object> createAppointment(Map<String, Object> appointmentMap);

  Optional<Map<String, Object>> findAppointment(String id);

  List<Map<String, Object>> findAllAppointmentsForToday();
}
