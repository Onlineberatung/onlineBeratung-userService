package de.caritas.cob.userservice.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class AccountManagerTest {

  private AccountManager accountManager;

  @Mock
  private ConsultantRepository consultantRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserServiceMapper userServiceMapper;

  @Before
  public void init() {
    accountManager = new AccountManager(consultantRepository, userRepository, userServiceMapper);
  }

  @Test
  public void saveWalkThroughEnabled() {
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("walkThroughEnabled", true);
    requestData.put("id", "1");
    when(userRepository.findByUserIdAndDeleteDateIsNull("1")).thenReturn(Optional.empty());
    Consultant consultant = new Consultant();
    when(consultantRepository.findByIdAndDeleteDateIsNull("1")).thenReturn(Optional.of(consultant));
    when(consultantRepository.save(consultant)).thenReturn(consultant);
    accountManager.patchUser(requestData);
    assertEquals(consultant.getWalkThroughEnabled(), true);
  }

}
