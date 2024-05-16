package de.caritas.cob.userservice.api.service.user.validation;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.PASSWORD;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class UserAccountValidatorTest {

  @InjectMocks private UserAccountValidator userAccountValidator;
  @Mock private IdentityClient identityClient;

  @Test
  public void checkPasswordValidity_Should_ThrowBadRequestException_When_KeycloakLoginFails() {
    assertThrows(
        BadRequestException.class,
        () -> {
          when(identityClient.loginUser(anyString(), anyString()))
              .thenThrow(new BadRequestException(ERROR));

          this.userAccountValidator.checkPasswordValidity(USERNAME, PASSWORD);
        });
  }

  @Test
  public void checkPasswordValidity_Should_LogOutUser_When_LoginWasSuccessful() {
    KeycloakLoginResponseDTO loginResponseDTO =
        new EasyRandom().nextObject(KeycloakLoginResponseDTO.class);
    when(identityClient.loginUser(anyString(), anyString())).thenReturn(loginResponseDTO);

    this.userAccountValidator.checkPasswordValidity(USERNAME, PASSWORD);

    verify(identityClient, times(1)).logoutUser(anyString());
  }
}
