package de.caritas.cob.userservice.api.adapters.web.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.web.dto.Appointment;
import de.caritas.cob.userservice.api.adapters.web.dto.AppointmentStatus;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateEnquiryMessageResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EnquiryAppointmentDTO;
import de.caritas.cob.userservice.api.adapters.web.mapping.AppointmentDtoMapper;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.facade.assignsession.AssignEnquiryFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.EnquiryData;
import de.caritas.cob.userservice.api.port.in.Organizing;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.statistics.StatisticsService;
import de.caritas.cob.userservice.api.service.user.UserAccountService;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.AppointmentsApi;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "appointment-controller")
public class AppointmentController implements AppointmentsApi {

  private static final String APPOINTMENT_NOT_FOUND = "Appointment (%s) not found.";

  private static final String APPOINTMENT_WITH_BOOKING_ID_NOT_FOUND =
      "Appointment with booking id (%s) not found.";
  private final Organizing organizer;

  private final AppointmentDtoMapper mapper;

  private final AuthenticatedUser currentUser;

  private final @NotNull UserAccountService userAccountProvider;

  private final @NotNull CreateEnquiryMessageFacade createEnquiryMessageFacade;

  private final @NotNull AssignEnquiryFacade assignEnquiryFacade;

  private final @NotNull SessionService sessionService;

  private final @NotNull ConsultantService consultantService;

  private final @NotNull ConsultantRepository consultantRepository;

  private final StatisticsService statisticsService;

  @Override
  public ResponseEntity<Appointment> getAppointment(UUID id) {
    var appointmentMap =
        organizer
            .findAppointment(id.toString())
            .orElseThrow(() -> new NotFoundException(APPOINTMENT_NOT_FOUND, id.toString()));

    var appointment = mapper.appointmentOf(appointmentMap, currentUser.isConsultant());

    return ResponseEntity.ok(appointment);
  }

  @Override
  public ResponseEntity<Appointment> getAppointmentByBookingId(Integer bookingId) {
    var appointmentMap =
        organizer
            .findAppointmentByBookingId(bookingId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        APPOINTMENT_WITH_BOOKING_ID_NOT_FOUND, bookingId.toString()));

    var appointment = mapper.appointmentOf(appointmentMap, currentUser.isConsultant());

    return ResponseEntity.ok(appointment);
  }

  @Override
  public ResponseEntity<Appointment> updateAppointment(UUID id, Appointment appointment) {
    if (isNull(appointment.getId()) || !appointment.getId().equals(id)) {
      throw new BadRequestException("Appointment ID from path and payload mismatch.");
    }
    var savedAppointmentMap =
        organizer
            .findAppointment(id.toString())
            .orElseThrow(() -> new NotFoundException(APPOINTMENT_NOT_FOUND, id.toString()));

    var updatedAppointmentMap = mapper.mapOf(savedAppointmentMap, appointment);
    var savedMap = organizer.upsertAppointment(updatedAppointmentMap);
    var savedAppointment = mapper.appointmentOf(savedMap, true);

    mapper
        .eventOf(id, appointment.getStatus(), currentUser.getUserId())
        .ifPresent(statisticsService::fireEvent);

    return ResponseEntity.ok(savedAppointment);
  }

  @Override
  public ResponseEntity<Void> deleteAppointment(UUID id) {
    if (!organizer.deleteAppointment(id.toString())) {
      throw new NotFoundException(APPOINTMENT_NOT_FOUND, id.toString());
    }

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<Appointment>> getAppointments() {
    var appointments =
        organizer.findAllTodaysAndFutureAppointments(currentUser.getUserId()).stream()
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

    String consultantId = resolveConsultantId(appointment);
    var appointmentMap = mapper.mapOf(appointment, consultantId);
    var savedMap = organizer.upsertAppointment(appointmentMap);
    var savedAppointment = mapper.appointmentOf(savedMap, true);

    return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
  }

  private String resolveConsultantId(Appointment appointment) {
    if (currentUser.getRoles().contains(UserRole.CONSULTANT.getValue())
        && appointment.getConsultantEmail() == null) {
      return currentUser.getUserId();
    } else if (currentUser.getRoles().contains(UserRole.TECHNICAL.getValue())
        && appointment.getConsultantEmail() != null) {
      Optional<Consultant> consultant =
          consultantRepository.findByEmailAndDeleteDateIsNull(appointment.getConsultantEmail());
      if (!consultant.isPresent()) {
        throw new BadRequestException(
            "Consultant doesn't exist for given email " + appointment.getConsultantEmail());
      }
      return consultant.get().getId();
    } else {
      throw new BadRequestException("Can not create appointment for given request.");
    }
  }

  @Override
  public ResponseEntity<CreateEnquiryMessageResponseDTO> createEnquiryAppointment(
      @PathVariable Long sessionId,
      @RequestHeader String rcToken,
      @RequestHeader String rcUserId,
      @RequestBody EnquiryAppointmentDTO enquiryAppointmentDTO) {

    var user = this.userAccountProvider.retrieveValidatedUser();
    var rocketChatCredentials =
        RocketChatCredentials.builder().rocketChatToken(rcToken).rocketChatUserId(rcUserId).build();
    var enquiryData =
        new EnquiryData(
            user,
            sessionId,
            null,
            null,
            rocketChatCredentials,
            enquiryAppointmentDTO.getT(),
            enquiryAppointmentDTO.getCounselorEmail());

    var response = createEnquiryMessageFacade.createEnquiryMessage(enquiryData);

    var consultant =
        consultantService.findConsultantByEmail(enquiryAppointmentDTO.getCounselorEmail());
    var session = sessionService.getSession(sessionId);

    this.assignEnquiryFacade.assignRegisteredEnquiry(
        session.orElseThrow(), consultant.orElseThrow(), true);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }
}
