package de.caritas.cob.userservice.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class UserServiceMapperTest {

  @InjectMocks
  private UserServiceMapper userServiceMapper;

  @Mock
  private UsernameTranscoder usernameTranscoder;

  @Test
  public void saveWalkThroughEnabled() {
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("walkThroughEnabled", true);
    requestData.put("id", "1");
    Consultant consultant = new Consultant();

    userServiceMapper.consultantOf(consultant, requestData);

    assertEquals(true, consultant.getWalkThroughEnabled());
  }

}
