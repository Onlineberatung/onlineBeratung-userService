package de.caritas.cob.userservice.api.port.in;

import java.util.Map;

public interface Organizing {

  Map<String, Object> createAppointment(Map<String, Object> appointmentMap);
}
