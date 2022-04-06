package de.caritas.cob.userservice.api.adapters.web.mapping;

import static java.util.Objects.nonNull;

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
    if (nonNull(appointment.getId())) {
      appointmentMap.put("id", appointment.getId().toString());
    }
    appointmentMap.put("description", appointment.getDescription());
    appointmentMap.put("datetime", appointment.getDatetime().toString());
    appointmentMap.put("status", appointment.getStatus().getValue());
    appointmentMap.put("consultantId", authenticatedUser.getUserId());

    return appointmentMap;
  }

  public Map<String, Object> mapOf(Map<String, Object> initMap, Appointment overridingAppointment) {
    initMap.put("description", overridingAppointment.getDescription());
    initMap.put("datetime", overridingAppointment.getDatetime().toString());
    initMap.put("status", overridingAppointment.getStatus().getValue());

    return initMap;
  }

  public Appointment appointmentOf(Map<String, Object> savedMap, boolean fullDto) {
    var appointment = new Appointment();
    var status = (String) savedMap.get("status");
    appointment.setId(UUID.fromString((String) savedMap.get("id")));
    appointment.setStatus(AppointmentStatus.fromValue(status));

    if (fullDto) {
      appointment.setDescription((String) savedMap.get("description"));
      appointment.setDatetime(Instant.parse((String) savedMap.get("datetime")));
    }

    return appointment;
  }
}
