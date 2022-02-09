package de.caritas.cob.userservice.api.service.user.validation;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.PASSWORD;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.keycloak.login.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.service.KeycloakService;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UserAccountValidatorTest {

  @InjectMocks
  private UserAccountValidator userAccountValidator;
  @Mock
  private KeycloakService keycloakService;

  @Test(expected = BadRequestException.class)
  public void checkPasswordValidity_Should_ThrowBadRequestException_When_KeycloakLoginFails() {
    when(keycloakService.loginUser(anyString(), anyString()))
        .thenThrow(new BadRequestException(ERROR));

    this.userAccountValidator.checkPasswordValidity(USERNAME, PASSWORD);
  }

  @Test
  public void checkPasswordValidity_Should_LogOutUser_When_LoginWasSuccessful() {
    KeycloakLoginResponseDTO loginResponseDTO = new EasyRandom()
        .nextObject(KeycloakLoginResponseDTO.class);
    when(keycloakService.loginUser(anyString(), anyString())).thenReturn(loginResponseDTO);

    this.userAccountValidator.checkPasswordValidity(USERNAME, PASSWORD);

    verify(keycloakService, times(1)).logoutUser(anyString());
  }
}
