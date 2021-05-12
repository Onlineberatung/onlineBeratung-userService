package de.caritas.cob.userservice.api.service.user.anonymous;

import static de.caritas.cob.userservice.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_DTO_SUCHT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.facade.CreateUserFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.keycloak.login.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.model.user.AnonymousUserCredentials;
import de.caritas.cob.userservice.api.service.KeycloakService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class AnonymousUserCreatorServiceTest {

  @InjectMocks
  private AnonymousUserCreatorService anonymousUserCreatorService;
  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;
  @Mock
  private CreateUserFacade createUserFacade;
  @Mock
  private KeycloakService keycloakService;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  private RollbackFacade rollbackFacade;

  EasyRandom easyRandom = new EasyRandom();

  @Test(expected = InternalServerErrorException.class)
  public void createAnonymousUser_Should_ThrowInternalServerErrorExceptionAndPerformRollback_When_KeycloakLoginFails() {
    KeycloakCreateUserResponseDTO responseDTO = easyRandom
        .nextObject(KeycloakCreateUserResponseDTO.class);
    when(keycloakAdminClientService.createKeycloakUser(any())).thenReturn(responseDTO);
    when(keycloakService.loginUser(anyString(), anyString()))
        .thenThrow(new BadRequestException(ERROR));

    anonymousUserCreatorService.createAnonymousUser(USER_DTO_SUCHT);

    verifyNoInteractions(rollbackFacade);
    verifyNoInteractions(rocketChatService);
  }

  @Test(expected = InternalServerErrorException.class)
  public void createAnonymousUser_Should_ThrowInternalServerErrorExceptionAndPerformRollback_When_RocketChatLoginFails()
      throws RocketChatLoginException {
    KeycloakCreateUserResponseDTO responseDTO = easyRandom
        .nextObject(KeycloakCreateUserResponseDTO.class);
    when(keycloakAdminClientService.createKeycloakUser(any())).thenReturn(responseDTO);
    RocketChatLoginException exception = easyRandom.nextObject(RocketChatLoginException.class);
    when(rocketChatService.loginUserFirstTime(USER_DTO_SUCHT.getUsername(),
        USER_DTO_SUCHT.getPassword())).thenThrow(exception);

    anonymousUserCreatorService.createAnonymousUser(USER_DTO_SUCHT);

    verifyNoInteractions(rollbackFacade);
    verifyNoInteractions(rocketChatService);
  }

  @Test
  public void createAnonymousUser_Should_ReturnAnonymousUserCredentials()
      throws RocketChatLoginException {
    KeycloakCreateUserResponseDTO responseDTO = easyRandom
        .nextObject(KeycloakCreateUserResponseDTO.class);
    when(keycloakAdminClientService.createKeycloakUser(any())).thenReturn(responseDTO);
    KeycloakLoginResponseDTO keycloakLoginResponseDTO = easyRandom
        .nextObject(KeycloakLoginResponseDTO.class);
    when(keycloakService.loginUser(anyString(), anyString())).thenReturn(keycloakLoginResponseDTO);
    LoginResponseDTO loginResponseDTO = easyRandom.nextObject(LoginResponseDTO.class);
    ResponseEntity<LoginResponseDTO> responseEntity = new ResponseEntity<>(loginResponseDTO,
        HttpStatus.OK);
    when(rocketChatService.loginUserFirstTime(USER_DTO_SUCHT.getUsername(),
        USER_DTO_SUCHT.getPassword())).thenReturn(responseEntity);

    AnonymousUserCredentials credentials = anonymousUserCreatorService
        .createAnonymousUser(USER_DTO_SUCHT);

    assertThat(credentials, instanceOf(AnonymousUserCredentials.class));
    verifyNoInteractions(rollbackFacade);
  }
}
