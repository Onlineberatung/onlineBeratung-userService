package de.caritas.cob.userservice.api.port.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.model.Session.RegistrationType;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.SessionData;
import de.caritas.cob.userservice.api.model.SessionData.SessionDataType;
import de.caritas.cob.userservice.api.model.User;
import java.util.List;
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
class SessionRepositoryIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  @Autowired
  private SessionRepository underTest;

  @Autowired
  private UserRepository userRepository;

  private User user;

  private Session session;

  @AfterEach
  public void reset() {
    underTest.delete(session);
    session = null;
    user = null;
  }

  @Test
  void saveShouldSaveSession() {
    givenAUser();
    givenValidSession();

    var persistedSession = underTest.save(session);

    var foundOptionalSession = underTest.findById(persistedSession.getId());
    assertTrue(foundOptionalSession.isPresent());

    var foundSession = foundOptionalSession.get();
    var sessionData = session.getSessionData();
    assertEquals(2, sessionData.size());
    assertEquals(sessionData.get(0), foundSession.getSessionData().get(0));
    assertEquals(sessionData.get(1), foundSession.getSessionData().get(1));
    assertFalse(foundSession.isTeamSession());
    assertFalse(foundSession.isPeerChat());
    assertFalse(foundSession.isMonitoring());
  }

  private void givenValidSession() {
    session = new Session();
    session.setUser(user);
    session.setConsultingTypeId(1);
    session.setRegistrationType(easyRandom.nextObject(RegistrationType.class));
    session.setPostcode(RandomStringUtils.randomNumeric(5));
    session.setLanguageCode(easyRandom.nextObject(LanguageCode.class));
    session.setStatus(easyRandom.nextObject(SessionStatus.class));

    var sessionData1 = new SessionData(
        session, SessionDataType.REGISTRATION, RandomStringUtils.randomAlphanumeric(1, 255),
        RandomStringUtils.randomAlphanumeric(1, 255));

    var sessionData2 = new SessionData(
        session, SessionDataType.REGISTRATION, RandomStringUtils.randomAlphanumeric(1, 255),
        RandomStringUtils.randomAlphanumeric(1, 255));

    session.setSessionData(List.of(sessionData1, sessionData2));
  }

  private void givenAUser() {
    user = userRepository.findAll().iterator().next();
  }
}
