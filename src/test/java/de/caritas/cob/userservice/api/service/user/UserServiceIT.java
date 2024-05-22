package de.caritas.cob.userservice.api.service.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.port.out.UserMobileTokenRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class UserServiceIT {

  @Autowired private UserService userService;

  @Autowired private UserRepository userRepository;

  @Autowired private UserMobileTokenRepository userMobileTokenRepository;

  @BeforeEach
  void clearTokens() {
    this.userMobileTokenRepository.deleteAll();
  }

  @Test
  void addMobileTokensToUser_Should_persistMobileTokens_When_tokensAreUnique() {
    var userId = this.userRepository.findAll().iterator().next().getUserId();

    this.userService.addMobileAppToken(userId, "token");
    this.userService.addMobileAppToken(userId, "token2");
    this.userService.addMobileAppToken(userId, "token3");

    var resultUser = this.userService.getUser(userId);
    assertThat(resultUser.isPresent(), is(true));
    var userTokens = resultUser.get().getUserMobileTokens();
    assertThat(userTokens, hasSize(3));
  }

  @Test
  void addMobileTokensToUser_Should_throwConflictException_When_tokenAlreadyExists() {
    var userId = this.userRepository.findAll().iterator().next().getUserId();

    this.userService.addMobileAppToken(userId, "token");

    assertThrows(
        ConflictException.class, () -> this.userService.addMobileAppToken(userId, "token"));
  }
}
