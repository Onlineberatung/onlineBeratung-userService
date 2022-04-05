package de.caritas.cob.userservice.api.port.in;

import java.util.Map;
import java.util.Optional;

public interface Organizing {

  Map<String, Object> createAppointment(Map<String, Object> appointmentMap);

  Optional<Map<String, Object>> findAppointment(String id);
}
