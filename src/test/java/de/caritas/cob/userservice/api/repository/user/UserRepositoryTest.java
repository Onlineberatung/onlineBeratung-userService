package de.caritas.cob.userservice.api.repository.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  public void findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan_Should_ReturnCorrectUsers() {

    final LocalDateTime dateCheck = LocalDateTime.of(2019, 1, 1, 0, 0, 0, 0);
    final String USER_ID_SHOULD_IN_LIST_1 = "abc9a0a1-c936-45ee-9141-d73dfc0a3999";
    final String USER_ID_SHOULD_IN_LIST_2 = "def9a0a1-c936-45ee-9141-d73dfc0a3888";
    final String USER_ID_SHOULD_NOT_IN_LIST_1 = "hki9a0a1-c936-45ee-9141-d73dfc0a3777";
    final String USER_ID_SHOULD_NOT_IN_LIST_2 = "dlk9a0a1-c936-45ee-9141-d73dfc0a3666";
    final String USER_ID_SHOULD_NOT_IN_LIST_3 = "arb9a0a1-c936-45ee-9141-d73dfc0a3111";
    final String USER_ID_SHOULD_NOT_IN_LIST_4 = "jurea0a1-c936-45ee-9141-d73dfc0a3000";
    final String USER_ID_SHOULD_NOT_IN_LIST_5 = "opiti0a1-c936-45ee-9141-d73dfc0a3000";

    List<User> result =
        userRepository.findAllByDeleteDateNullAndNoRunningSessionsAndCreateDateOlderThan(dateCheck);

    assertThat(result, notNullValue());
    assertThat(result.size(), is(2));
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_IN_LIST_1::equals),
        is(true));
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_IN_LIST_2::equals),
        is(true));
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_NOT_IN_LIST_1::equals),
        is(false));
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_NOT_IN_LIST_2::equals),
        is(false));
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_NOT_IN_LIST_3::equals),
        is(false));
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_NOT_IN_LIST_4::equals),
        is(false));
    assertThat(result.stream().map(User::getUserId).anyMatch(USER_ID_SHOULD_NOT_IN_LIST_5::equals),
        is(false));
  }

}
