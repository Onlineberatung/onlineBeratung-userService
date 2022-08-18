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
class AppointmentRepositoryIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  private Consultant consultant;

  private Appointment appointment;

  @Autowired private AppointmentRepository underTest;

  @Autowired private ConsultantRepository consultantRepository;

  @AfterEach
  public void reset() {
    underTest.deleteAll();
    consultant = null;
  }

  @Test
  void saveShouldSaveAppointmentAndReturnId() {
    givenAValidConsultant();
    givenAValidAppointment(false);

    assertEquals(0, underTest.count());
    var savedAppointment = underTest.save(appointment);

    assertNotNull(savedAppointment.getId());
  }

  @Test
  void deleteShouldKeepConsultant() {
    givenAValidConsultant();
    givenAValidAppointment(true);

    assertEquals(1, underTest.count());
    var countConsultants = consultantRepository.count();
    underTest.delete(appointment);

    assertEquals(countConsultants, consultantRepository.count());
    assertEquals(0, underTest.count());
  }

  private void givenAValidConsultant() {
    consultant = consultantRepository.findAll().iterator().next();
  }

  private void givenAValidAppointment(boolean toSave) {
    appointment = easyRandom.nextObject(Appointment.class);
    appointment.setConsultant(consultant);
    appointment.setId(null);
    var desc = appointment.getDescription();
    if (desc.length() > 300) {
      appointment.setDescription(desc.substring(0, 300));
    }
    if (toSave) {
      underTest.save(appointment);
    }
  }
}
