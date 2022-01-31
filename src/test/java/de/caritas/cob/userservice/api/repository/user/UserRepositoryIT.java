package de.caritas.cob.userservice.api.repository.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class UserRepositoryIT {

  @Autowired
  private UserRepository userRepository;

  @Test
  public void findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThanShouldReturnUserSubsetOnPastDate() {
    final LocalDateTime dateCheck = LocalDateTime.of(2019, 1, 1, 0, 0, 0, 0);
    final String USER_ID_SHOULD_IN_LIST_1 = "abc9a0a1-c936-45ee-9141-d73dfc0a3999";
    final String USER_ID_SHOULD_IN_LIST_2 = "def9a0a1-c936-45ee-9141-d73dfc0a3888";

    List<User> result =
        userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(dateCheck);

    assertThat(result, notNullValue());
    assertThat(result.size(), is(2));
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_IN_LIST_1::equals),
        is(true));
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_IN_LIST_2::equals),
        is(true));
  }

  @Test
  public void findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThanShouldReturnAllUsersOnTomorrowAsDate() {
    var startOfTomorrow = LocalDateTime.now().with(LocalTime.MIDNIGHT).plusDays(1);

    var users = userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(
        startOfTomorrow);

    assertNotNull(users);
    assertEquals(12, users.size());
  }
}
