package de.caritas.cob.userservice.api.adapters.web.mapping;

import de.caritas.cob.userservice.api.adapters.web.dto.Appointment;
import de.caritas.cob.userservice.api.adapters.web.dto.AppointmentStatus;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AppointmentDtoMapper {

  public Map<String, Object> mapOf(Appointment appointment, AuthenticatedUser authenticatedUser) {
    var appointmentMap = new HashMap<String, Object>();
    appointmentMap.put("id", appointment.getId());
    appointmentMap.put("description", appointment.getDescription());
    appointmentMap.put("datetime", appointment.getDatetime().toString());
    appointmentMap.put("status", appointment.getStatus().getValue());
    appointmentMap.put("consultantId", authenticatedUser.getUserId());

    return appointmentMap;
  }

  public Appointment appointmentOf(Map<String, Object> savedMap) {
    var appointment = new Appointment();
    appointment.setId(UUID.fromString((String) savedMap.get("id")));
    appointment.setDescription((String) savedMap.get("description"));
    var status = (String) savedMap.get("status");
    appointment.setStatus(AppointmentStatus.fromValue(status));
    appointment.setDatetime(Instant.parse((String) savedMap.get("datetime")));

    return appointment;
  }
}
