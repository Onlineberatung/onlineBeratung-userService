package de.caritas.cob.userservice.api.port.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.caritas.cob.userservice.api.config.JpaAuditingConfiguration;
import de.caritas.cob.userservice.api.model.UserAgency;
import java.util.Objects;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Import(JpaAuditingConfiguration.class)
class UserAgencyRepositoryIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  private UserAgency userAgency;

  @Autowired private UserAgencyRepository underTest;

  @Autowired private UserRepository userRepository;

  @AfterEach
  public void reset() {
    if (Objects.nonNull(userAgency)) {
      underTest.delete(userAgency);
    }
  }

  @Test
  void saveShouldWriteAuditingData() {
    givenPersistedUserAgency();

    assertNotNull(userAgency.getCreateDate());
    assertNotNull(userAgency.getUpdateDate());
    assertEquals(userAgency.getCreateDate(), userAgency.getUpdateDate());
  }

  private void givenPersistedUserAgency() {
    var user = userRepository.findAll().iterator().next();
    var userAgency = new UserAgency(user, easyRandom.nextLong());
    this.userAgency = underTest.save(userAgency);
  }
}
