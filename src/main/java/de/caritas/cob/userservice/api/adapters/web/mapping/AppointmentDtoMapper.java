package de.caritas.cob.userservice.api.adapters.web.mapping;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.Appointment;
import de.caritas.cob.userservice.api.adapters.web.dto.AppointmentStatus;
import de.caritas.cob.userservice.api.service.statistics.event.StartVideoCallStatisticsEvent;
import de.caritas.cob.userservice.api.service.statistics.event.StatisticsEvent;
import de.caritas.cob.userservice.api.service.statistics.event.StopVideoCallStatisticsEvent;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentDtoMapper {

  private static final String DESCRIPTION = "description";
  private static final String DATETIME = "datetime";
  private static final String STATUS = "status";

  public Map<String, Object> mapOf(Appointment appointment, String consultantId) {
    var appointmentMap = new HashMap<String, Object>();
    if (nonNull(appointment.getId())) {
      appointmentMap.put("id", appointment.getId().toString());
    }
    appointmentMap.put(DESCRIPTION, appointment.getDescription());
    appointmentMap.put(DATETIME, appointment.getDatetime().toString());
    appointmentMap.put(STATUS, appointment.getStatus().getValue());
    appointmentMap.put("consultantId", consultantId);
    appointmentMap.put("bookingId", appointment.getBookingId());

    return appointmentMap;
  }

  public Map<String, Object> mapOf(Map<String, Object> initMap, Appointment overridingAppointment) {
    initMap.put(DESCRIPTION, overridingAppointment.getDescription());
    initMap.put(DATETIME, overridingAppointment.getDatetime().toString());
    initMap.put(STATUS, overridingAppointment.getStatus().getValue());

    return initMap;
  }

  public Appointment appointmentOf(Map<String, Object> savedMap, boolean fullDto) {
    var appointment = new Appointment();
    var status = (String) savedMap.get(STATUS);
    appointment.setId(UUID.fromString((String) savedMap.get("id")));
    appointment.setStatus(AppointmentStatus.fromValue(status));

    if (fullDto) {
      appointment.setDescription((String) savedMap.get(DESCRIPTION));
      appointment.setDatetime(Instant.parse((String) savedMap.get(DATETIME)));
    }

    return appointment;
  }

  public Optional<StatisticsEvent> eventOf(UUID id, AppointmentStatus status, String userId) {
    StatisticsEvent event;
    if (status == AppointmentStatus.STARTED) {
      event = new StartVideoCallStatisticsEvent(userId, id);
    } else if (status == AppointmentStatus.PAUSED) {
      event = new StopVideoCallStatisticsEvent(userId, id);
    } else {
      event = null;
    }

    return Optional.ofNullable(event);
  }
}
