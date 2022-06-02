package de.caritas.cob.userservice.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class UserServiceMapperTest {

  @InjectMocks
  private UserServiceMapper userServiceMapper;

  @Mock
  @SuppressWarnings("unused")
  private UsernameTranscoder usernameTranscoder;

  @Test
  void saveWalkThroughEnabled() {
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("walkThroughEnabled", true);
    requestData.put("id", "1");
    Consultant consultant = new Consultant();

    userServiceMapper.consultantOf(consultant, requestData);

    assertEquals(true, consultant.getWalkThroughEnabled());
  }

  @Test
  void e2eKeyOfShouldMapIfKeyExists() {
    var map = Map.of("e2eKey", "tmp." + RandomStringUtils.randomAlphanumeric(16));

    var e2eKey = userServiceMapper.e2eKeyOf(map);

    assertTrue(e2eKey.isPresent());
    assertEquals(map.get("e2eKey"), e2eKey.get());
  }

  @Test
  void e2eKeyOfShouldNotMapIfKeyFormatIsWrong() {
    var map = Map.of("e2eKey", RandomStringUtils.randomAlphanumeric(16));

    var e2eKey = userServiceMapper.e2eKeyOf(map);

    assertFalse(e2eKey.isPresent());
  }

  @Test
  void e2eKeyOfShouldNotMapIfKeyDoesNotExist() {
    var map = Map.of("notE2eKey", RandomStringUtils.randomAlphanumeric(16));

    var e2eKey = userServiceMapper.e2eKeyOf(map);

    assertFalse(e2eKey.isPresent());
  }
}
