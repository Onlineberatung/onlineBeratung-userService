package de.caritas.cob.userservice.api.adapters.web.controller;

import static java.util.Objects.isNull;
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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(tags = "appointment-controller")
public class AppointmentController implements AppointmentsApi {

  private final Organizing organizer;

  private final AppointmentDtoMapper mapper;

  private final AuthenticatedUser currentUser;

  public AppointmentController(Organizing organizer, AppointmentDtoMapper mapper,
      @Qualifier("AuthenticatedOrAnonymousUser") AuthenticatedUser authenticatedUser) {
    this.organizer = organizer;
    this.mapper = mapper;
    this.currentUser = authenticatedUser;
  }

  @Override
  public ResponseEntity<Appointment> getAppointment(UUID id) {
    var appointmentMap = organizer.findAppointment(id.toString()).orElseThrow(() ->
        new NotFoundException("Appointment (%s) not found.", id.toString())
    );

    var appointment = mapper.appointmentOf(appointmentMap, currentUser.isConsultant());

    return ResponseEntity.ok(appointment);
  }

  @Override
  public ResponseEntity<Appointment> updateAppointment(UUID id, Appointment appointment) {
    if (isNull(appointment.getId()) || !appointment.getId().equals(id)) {
      throw new BadRequestException("Appointment ID from path and payload mismatch.");
    }
    var savedAppointmentMap = organizer.findAppointment(id.toString()).orElseThrow(() ->
        new NotFoundException("Appointment (%s) not found.", id.toString())
    );
    var updatedAppointmentMap = mapper.mapOf(savedAppointmentMap, appointment);
    var savedMap = organizer.upsertAppointment(updatedAppointmentMap);
    var savedAppointment = mapper.appointmentOf(savedMap, true);

    return ResponseEntity.ok(savedAppointment);
  }

  @Override
  public ResponseEntity<Void> deleteAppointment(UUID id) {
    if (!organizer.deleteAppointment(id.toString())) {
      throw new NotFoundException("Appointment (%s) not found.", id.toString());
    }

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<Appointment>> getAppointments() {
    var appointments = organizer.findAllTodaysAndFutureAppointments().stream()
        .map(appointmentMap -> mapper.appointmentOf(appointmentMap, currentUser.isConsultant()))
        .collect(Collectors.toList());

    return ResponseEntity.ok(appointments);
  }

  @Override
  public ResponseEntity<Appointment> createAppointment(Appointment appointment) {
    if (nonNull(appointment.getId())) {
      throw new BadRequestException("Appointment ID must be null.");
    }
    if (!appointment.getStatus().equals(AppointmentStatus.CREATED)) {
      throw new BadRequestException("The initial appointment status must be 'created.'");
    }

    var appointmentMap = mapper.mapOf(appointment, currentUser);
    var savedMap = organizer.upsertAppointment(appointmentMap);
    var savedAppointment = mapper.appointmentOf(savedMap, true);

    return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
  }
}
