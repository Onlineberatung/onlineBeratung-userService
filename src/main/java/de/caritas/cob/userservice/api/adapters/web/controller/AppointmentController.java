package de.caritas.cob.userservice.api.adapters.web.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.Appointment;
import de.caritas.cob.userservice.api.adapters.web.dto.AppointmentStatus;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateEnquiryMessageResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EnquiryAppointmentDTO;
import de.caritas.cob.userservice.api.adapters.web.mapping.AppointmentDtoMapper;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.facade.assignsession.AssignEnquiryFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.AppointmentData;
import de.caritas.cob.userservice.api.model.EnquiryData;
import de.caritas.cob.userservice.api.port.in.Organizing;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.AppointmentsApi;
import io.swagger.annotations.Api;
import java.util.List;
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

  private final Organizing organizer;

  private final AppointmentDtoMapper mapper;

  private final AuthenticatedUser currentUser;

  private final @NotNull ValidatedUserAccountProvider userAccountProvider;

  private final @NotNull CreateEnquiryMessageFacade createEnquiryMessageFacade;

  private final @NotNull AssignEnquiryFacade assignEnquiryFacade;

  private final @NotNull SessionService sessionService;

  private final @NotNull ConsultantService consultantService;

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
    var appointments = organizer.findAllTodaysAndFutureAppointments(currentUser.getUserId())
        .stream()
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

  @Override
  public ResponseEntity<CreateEnquiryMessageResponseDTO> createEnquiryAppointment(
      @PathVariable Long sessionId, @RequestHeader String rcToken, @RequestHeader String rcUserId,
      @RequestBody EnquiryAppointmentDTO enquiryAppointmentDTO) {

    var user = this.userAccountProvider.retrieveValidatedUser();
    var rocketChatCredentials = RocketChatCredentials.builder()
        .rocketChatToken(rcToken)
        .rocketChatUserId(rcUserId)
        .build();
    String language = null;

    AppointmentData appointmentData = new AppointmentData(enquiryAppointmentDTO.getTitle(), enquiryAppointmentDTO.getUserName(), enquiryAppointmentDTO.getCounselor(), enquiryAppointmentDTO.getDate(), enquiryAppointmentDTO.getDuration());

    var enquiryData = new EnquiryData(user, sessionId, null, language,
        rocketChatCredentials, enquiryAppointmentDTO.getT(), enquiryAppointmentDTO.getOrg(), appointmentData);

    var response = createEnquiryMessageFacade.createEnquiryMessage(enquiryData);

    var consultant = consultantService.getConsultantByEmail(enquiryAppointmentDTO.getCounselor());
    var session = sessionService.getSession(sessionId);
    this.assignEnquiryFacade.assignRegisteredEnquiry(session.get(), consultant.get());

    return new ResponseEntity<>(response, HttpStatus.CREATED);


  }
}
