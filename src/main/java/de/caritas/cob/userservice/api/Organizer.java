package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.port.in.Organizing;
import de.caritas.cob.userservice.api.port.out.AppointmentRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

  @Override
  public Optional<Map<String, Object>> findAppointment(String id) {
    var appointmentMap = new HashMap<String, Object>();
    appointmentRepository.findById(UUID.fromString(id)).ifPresent(appointment ->
        appointmentMap.putAll(mapper.mapOf(appointment))
    );

    return appointmentMap.isEmpty() ? Optional.empty() : Optional.of(appointmentMap);
  }
}
