package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.port.in.Organizing;
import de.caritas.cob.userservice.api.port.out.AppointmentRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Organizer implements Organizing {

  private final AppointmentRepository appointmentRepository;

  private final ConsultantRepository consultantRepository;

  private final UserServiceMapper mapper;

  @Override
  public Map<String, Object> createAppointment(Map<String, Object> appointmentMap) {
    var consultantId = mapper.consultantIdOf(appointmentMap);
    var consultant = consultantRepository.findById(consultantId).orElseThrow();
    var appointment = mapper.appointmentOf(appointmentMap, consultant);

    var savedAppointment = appointmentRepository.save(appointment);

    return mapper.mapOf(savedAppointment);
  }
}
