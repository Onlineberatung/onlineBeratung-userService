package de.caritas.cob.userservice.api.adapters.web.controller;

import de.caritas.cob.userservice.api.adapters.web.dto.Appointment;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.AppointmentsApi;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "appointment-controller")
public class AppointmentController implements AppointmentsApi {

  @Override
  public ResponseEntity<Appointment> getAppointment(UUID id) {
    return ResponseEntity.ok(new Appointment());
  }

  @Override
  public ResponseEntity<Appointment> updateAppointment(UUID id, Appointment appointment) {
    return ResponseEntity.ok(new Appointment());
  }

  @Override
  public ResponseEntity<Void> deleteAppointment(UUID id) {
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<Appointment>> getAppointments() {
    return ResponseEntity.ok(List.of(new Appointment()));
  }

  @Override
  public ResponseEntity<Appointment> createAppointment(Appointment appointment) {
    appointment.setId(UUID.randomUUID());

    return new ResponseEntity<>(appointment, HttpStatus.CREATED);
  }
}
