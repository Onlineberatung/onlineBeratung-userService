package de.caritas.cob.userservice.api.helper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.KeycloakService;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UserVerifierTest {

  @InjectMocks
  private UserVerifier userVerifier;
  @Mock
  private KeycloakService keycloakService;

  EasyRandom easyRandom = new EasyRandom();

  @Test
  public void checkIfUsernameIsAvailable_Should_ThrowCustomValidationHttpStatusException_When_UsernameIsNotAvailable() {
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    when(keycloakService.isUsernameAvailable(userDTO.getUsername())).thenReturn(false);

    try {
      userVerifier.checkIfUsernameIsAvailable(userDTO);
    } catch (CustomValidationHttpStatusException exception) {
      assertThat(exception, instanceOf(CustomValidationHttpStatusException.class));
      assertEquals(exception.getHttpStatus(), HttpStatus.CONFLICT);
    }
  }

  @Test
  public void checkIfUsernameIsAvailable_ShouldNot_ThrowException_When_UsernameIsAvailable() {
    UserDTO userDTO = easyRandom.nextObject(UserDTO.class);
    when(keycloakService.isUsernameAvailable(userDTO.getUsername())).thenReturn(true);

    userVerifier.checkIfUsernameIsAvailable(userDTO);
  }
}
