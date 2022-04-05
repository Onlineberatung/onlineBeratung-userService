package de.caritas.cob.userservice.api.port.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.caritas.cob.userservice.api.model.Appointment;
import de.caritas.cob.userservice.api.model.Consultant;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class AppointmentRepositoryIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  private Consultant consultant;

  private Appointment appointment;

  @Autowired
  private AppointmentRepository underTest;

  @Autowired
  private ConsultantRepository consultantRepository;

  @AfterEach
  public void restore() {
    underTest.deleteAll();
    consultant = null;
  }

  @Test
  public void saveShouldSaveAppointmentAndReturnId() {
    givenAValidConsultant();
    givenAValidAppointment();

    assertEquals(0, underTest.count());
    var savedAppointment = underTest.save(appointment);

    assertNotNull(savedAppointment.getId());
  }

  private void givenAValidConsultant() {
    consultant = consultantRepository.findAll().iterator().next();
  }

  private void givenAValidAppointment() {
    appointment = easyRandom.nextObject(Appointment.class);
    appointment.setConsultant(consultant);
    appointment.setId(null);
    var desc = appointment.getDescription();
    if (desc.length() > 300) {
      appointment.setDescription(desc.substring(0, 300));
    }
  }
}

