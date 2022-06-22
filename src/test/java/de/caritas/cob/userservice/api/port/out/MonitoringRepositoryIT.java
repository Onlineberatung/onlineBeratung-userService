package de.caritas.cob.userservice.api.port.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.caritas.cob.userservice.api.model.Monitoring;
import de.caritas.cob.userservice.api.model.Session;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class MonitoringRepositoryIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  @Autowired
  private MonitoringRepository underTest;

  @Autowired
  private SessionRepository sessionRepository;

  private Session session;

  private Monitoring monitoring;

  @AfterEach
  public void restore() {
    underTest.deleteAll();
    session = null;
  }

  @Test
  void saveShouldSaveMonitoring() {
    givenASession();
    givenValidMonitoring();

    underTest.save(monitoring);

    var persistedSessions = underTest.findBySessionId(monitoring.getSessionId());
    assertNotNull(persistedSessions);
    assertEquals(1, persistedSessions.size());
    var persistedSession = persistedSessions.get(0);
    assertEquals(monitoring.getMonitoringType(), persistedSession.getMonitoringType());
    assertEquals(
        monitoring.getMonitoringOptionList().size(),
        persistedSession.getMonitoringOptionList().size()
    );
  }

  private void givenValidMonitoring() {
    monitoring = easyRandom.nextObject(Monitoring.class);
    monitoring.setSessionId(session.getId());
    monitoring.setKey(RandomStringUtils.randomAlphanumeric(1, 255));
    monitoring.getMonitoringOptionList().forEach(option -> {
      option.setSessionId(monitoring.getSessionId());
      option.setMonitoringType(monitoring.getMonitoringType());
      option.setMonitoringKey(monitoring.getKey());
      option.setKey(RandomStringUtils.randomAlphanumeric(1, 255));
      option.setMonitoring(monitoring);
    });
  }

  private void givenASession() {
    session = sessionRepository.findAll().iterator().next();
  }

}
