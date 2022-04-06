package de.caritas.cob.userservice.api.workflow.appointment.scheduler;

import de.caritas.cob.userservice.api.config.AppointmentConfig;
import de.caritas.cob.userservice.api.port.out.AppointmentRepository;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteObsoleteAppointmentsScheduler {

  private final @NonNull AppointmentRepository appointmentRepository;
  private final @NonNull AppointmentConfig appointmentConfig;
  private final @NonNull Clock clock;

  @Scheduled(cron = "#{appointmentConfig.deleteJobCron}")
  public void deleteObsoleteAppointments() {
    if (!appointmentConfig.getDeleteJobEnabled()) {
      return;
    }

    var lifespanInHours = appointmentConfig.getLifespanInHours();
    var olderThanLifespan = clock.instant().minus(lifespanInHours, ChronoUnit.HOURS);

    appointmentRepository.deleteOlderThan(olderThanLifespan);
  }
}
