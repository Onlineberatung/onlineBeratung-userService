package de.caritas.cob.userservice.api.port.out;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.config.JpaAuditingConfiguration;
import de.caritas.cob.userservice.api.model.User;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
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
class UserRepositoryIT {

  private User user;

  @Autowired private UserRepository userRepository;

  @AfterEach
  public void reset() {
    if (Objects.nonNull(user)) {
      userRepository.delete(user);
    }
  }

  @Test
  void
      findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThanShouldReturnUserSubsetOnPastDate() {
    final LocalDateTime dateCheck = LocalDateTime.of(2019, 1, 1, 0, 0, 0, 0);
    final String USER_ID_SHOULD_IN_LIST_1 = "abc9a0a1-c936-45ee-9141-d73dfc0a3999";
    final String USER_ID_SHOULD_IN_LIST_2 = "def9a0a1-c936-45ee-9141-d73dfc0a3888";

    List<User> result =
        userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(dateCheck);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_IN_LIST_1::equals))
        .isTrue();
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_IN_LIST_2::equals))
        .isTrue();
  }

  @Test
  void
      findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThanShouldReturnAllUsersOnTomorrowAsDate() {
    var startOfTomorrow = LocalDateTime.now().with(LocalTime.MIDNIGHT).plusDays(1);

    var users =
        userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(
            startOfTomorrow);

    assertNotNull(users);
    assertEquals(12, users.size());
    users.stream().forEach(this::assertThatHasNotificationSettings);
  }

  private void assertThatHasNotificationSettings(User user) {
    assertThat(user.isNotificationsEnabled());
    assertThat(user.getNotificationsSettings()).isNull();
  }

  @Test
  void saveShouldWriteAuditingData() {
    givenPersistedUser();

    assertNotNull(user.getCreateDate());
    assertNotNull(user.getUpdateDate());
    assertEquals(user.getCreateDate(), user.getUpdateDate());
  }

  @Test
  void saveShouldWriteDefaultLanguage() {
    givenPersistedUser();

    assertEquals(LanguageCode.de, user.getLanguageCode());
  }

  private void givenPersistedUser() {
    var user =
        new User(
            UUID.randomUUID().toString(),
            0L,
            RandomStringUtils.randomAlphabetic(255),
            RandomStringUtils.randomAlphabetic(255),
            false);
    this.user = userRepository.save(user);
  }
}
