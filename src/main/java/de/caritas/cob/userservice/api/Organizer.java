package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.config.AppointmentConfig;
import de.caritas.cob.userservice.api.port.in.Organizing;
import de.caritas.cob.userservice.api.port.out.AppointmentRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class Organizer implements Organizing {

  private final AppointmentRepository appointmentRepository;

  private final AppointmentConfig appointmentConfig;

  private final ConsultantRepository consultantRepository;

  private final UserServiceMapper mapper;

  private final Clock clock;

  @Override
  public Map<String, Object> upsertAppointment(Map<String, Object> appointmentMap) {
    var consultantId = mapper.consultantIdOf(appointmentMap);
    var consultant = consultantRepository.findById(consultantId).orElseThrow();
    var appointment = mapper.appointmentOf(appointmentMap, consultant);

    var savedAppointment = appointmentRepository.save(appointment);

    return mapper.mapOf(savedAppointment);
  }

  @Override
  public Optional<Map<String, Object>> findAppointment(String id) {
    var appointmentMap = new HashMap<String, Object>();
    appointmentRepository
        .findById(UUID.fromString(id))
        .ifPresent(appointment -> appointmentMap.putAll(mapper.mapOf(appointment)));

    return appointmentMap.isEmpty() ? Optional.empty() : Optional.of(appointmentMap);
  }

  @Override
  public Optional<Map<String, Object>> findAppointmentByBookingId(Integer bookingId) {
    var appointmentMap = new HashMap<String, Object>();
    appointmentRepository
        .findByBookingId(bookingId)
        .ifPresent(appointment -> appointmentMap.putAll(mapper.mapOf(appointment)));

    return appointmentMap.isEmpty() ? Optional.empty() : Optional.of(appointmentMap);
  }

  @Override
  public List<Map<String, Object>> findAllTodaysAndFutureAppointments(String userId) {
    var startOfDay = clock.instant().truncatedTo(ChronoUnit.DAYS);
    var futureAppointments = appointmentRepository.findAllOrderByDatetimeAfter(startOfDay, userId);

    return futureAppointments.stream().map(mapper::mapOf).collect(Collectors.toList());
  }

  @Override
  public boolean deleteAppointment(String id) {
    try {
      appointmentRepository.deleteById(UUID.fromString(id));
      return true;
    } catch (EmptyResultDataAccessException e) {
      return false;
    }
  }

  @Profile("!testing")
  @Scheduled(cron = "#{appointmentConfig.deleteJobCron}")
  @Transactional
  @Override
  public void deleteObsoleteAppointments() {
    if (!appointmentConfig.getDeleteJobEnabled()) {
      return;
    }
    var lifespanInHours = appointmentConfig.getLifespanInHours();
    var olderThanLifespan = clock.instant().minus(lifespanInHours, ChronoUnit.HOURS);

    appointmentRepository.deleteOlderThan(olderThanLifespan);
  }
}
