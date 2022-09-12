package de.caritas.cob.userservice.api.adapters.web.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserDTOTest {

  private final EasyRandom easyRandom = new EasyRandom();

  private UserDTO userDTO;

  @BeforeEach
  void reset() {
    userDTO = null;
  }

  @Test
  void getUserAgeShouldReturnNullOnNull() {
    givenAUserDto(null);

    assertNull(userDTO.getUserAge());
  }

  @Test
  void getUserAgeShouldReturnNullOnNullString() {
    givenAUserDto("null");

    assertNull(userDTO.getUserAge());
  }

  @Test
  void getUserAgeShouldReturnNullOnNonNumericString() {
    givenAUserDto(RandomStringUtils.randomAlphabetic(3));

    assertNull(userDTO.getUserAge());
  }

  @Test
  void getUserAgeShouldReturnIntegerOnNumericString() {
    var age = RandomStringUtils.randomNumeric(2);
    givenAUserDto(age);

    assertEquals(Integer.parseInt(age), userDTO.getUserAge());
  }

  private void givenAUserDto(String age) {
    userDTO = easyRandom.nextObject(UserDTO.class);
    userDTO.setAge(age);
  }
}
