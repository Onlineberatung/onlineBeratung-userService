package de.caritas.cob.userservice.api.adapters.web.controller;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.Appointment;
import de.caritas.cob.userservice.api.adapters.web.dto.AppointmentStatus;
import de.caritas.cob.userservice.api.adapters.web.mapping.AppointmentDtoMapper;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.port.in.Organizing;
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

  private final Organizing organizer;

  private final AppointmentDtoMapper mapper;

  private final AuthenticatedUser authenticatedUser;

  @Override
  public ResponseEntity<Appointment> getAppointment(UUID id) {
    var idString = id.toString();
    var appointmentMap = organizer.findAppointment(idString).orElseThrow(() ->
        new NotFoundException("Appointment (%s) not found.", idString)
    );

    var appointment = mapper.appointmentOf(appointmentMap, authenticatedUser.isConsultant());

    return ResponseEntity.ok(appointment);
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
    if (nonNull(appointment.getId())) {
      throw new BadRequestException("Appointment ID must be null.");
    }
    if (!appointment.getStatus().equals(AppointmentStatus.CREATED)) {
      throw new BadRequestException("The initial appointment status must be 'created.'");
    }

    var appointmentMap = mapper.mapOf(appointment, authenticatedUser);
    var savedMap = organizer.createAppointment(appointmentMap);
    var savedAppointment = mapper.appointmentOf(savedMap, true);

    return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
  }
}
