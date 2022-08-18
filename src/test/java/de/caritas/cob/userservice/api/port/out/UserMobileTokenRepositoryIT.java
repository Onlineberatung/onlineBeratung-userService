package de.caritas.cob.userservice.api.port.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserMobileToken;
import org.apache.commons.lang3.RandomStringUtils;
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
class UserMobileTokenRepositoryIT {

  @Autowired private UserMobileTokenRepository underTest;

  @Autowired private UserRepository userRepository;

  private User user;

  private UserMobileToken token;

  @AfterEach
  public void restore() {
    underTest.deleteAll();
    user = null;
    token = null;
  }

  @Test
  void saveShouldSaveToken() {
    givenAUser();
    givenAValidToken();

    var persistedToken = underTest.save(token);

    var optionalToken = underTest.findById(persistedToken.getId());
    assertTrue(optionalToken.isPresent());
    var foundToken = optionalToken.get();
    assertEquals(token.getMobileAppToken(), foundToken.getMobileAppToken());
    assertEquals(token.getUser(), foundToken.getUser());
  }

  private void givenAValidToken() {
    token = new UserMobileToken();
    token.setUser(user);
    token.setMobileAppToken(RandomStringUtils.randomAlphanumeric(1024, 2048));
  }

  private void givenAUser() {
    user = userRepository.findAll().iterator().next();
  }
}
