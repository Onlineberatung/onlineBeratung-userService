package de.caritas.cob.userservice.api.conversation.service1.user.anonymous;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ERROR;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USER_DTO_SUCHT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.conversation.service.user.anonymous.AnonymousUserCreatorService;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.facade.CreateUserFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.model.user.AnonymousUserCredentials;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.Optional;
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
  @SuppressWarnings("unused")
  private CreateUserFacade createUserFacade;
  @Mock
  private IdentityClient identityClient;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  private RollbackFacade rollbackFacade;
  @Mock
  private UserService userService;

  EasyRandom easyRandom = new EasyRandom();

  @Test(expected = InternalServerErrorException.class)
  public void createAnonymousUser_Should_ThrowInternalServerErrorExceptionAndPerformRollback_When_KeycloakLoginFails() {
    KeycloakCreateUserResponseDTO responseDTO = easyRandom
        .nextObject(KeycloakCreateUserResponseDTO.class);
    when(keycloakAdminClientService.createKeycloakUser(any())).thenReturn(responseDTO);
    when(identityClient.loginUser(anyString(), anyString()))
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
    when(identityClient.loginUser(anyString(), anyString())).thenReturn(keycloakLoginResponseDTO);
    LoginResponseDTO loginResponseDTO = easyRandom.nextObject(LoginResponseDTO.class);
    ResponseEntity<LoginResponseDTO> responseEntity = new ResponseEntity<>(loginResponseDTO,
        HttpStatus.OK);
    when(rocketChatService.loginUserFirstTime(USER_DTO_SUCHT.getUsername(),
        USER_DTO_SUCHT.getPassword())).thenReturn(responseEntity);
    when(userService.getUser(any())).thenReturn(Optional.of(mock(User.class)));

    AnonymousUserCredentials credentials = anonymousUserCreatorService
        .createAnonymousUser(USER_DTO_SUCHT);

    assertThat(credentials, instanceOf(AnonymousUserCredentials.class));
    assertThat(credentials.getExpiresIn(), is(keycloakLoginResponseDTO.getExpiresIn()));
    assertThat(credentials.getRefreshExpiresIn(),
        is(keycloakLoginResponseDTO.getRefreshExpiresIn()));
    assertThat(credentials.getAccessToken(), is(keycloakLoginResponseDTO.getAccessToken()));
    assertThat(credentials.getRefreshToken(), is(keycloakLoginResponseDTO.getRefreshToken()));
    verifyNoInteractions(rollbackFacade);
    verify(userService, times(1)).updateRocketChatIdInDatabase(any(), any());
  }

  @Test
  public void createAnonymousUser_Should_throwInternalServerError_When_userToUpdateRocketChatIdDoesNotExist()
      throws RocketChatLoginException {
    KeycloakCreateUserResponseDTO responseDTO = easyRandom
        .nextObject(KeycloakCreateUserResponseDTO.class);
    when(keycloakAdminClientService.createKeycloakUser(any())).thenReturn(responseDTO);
    KeycloakLoginResponseDTO keycloakLoginResponseDTO = easyRandom
        .nextObject(KeycloakLoginResponseDTO.class);
    when(identityClient.loginUser(anyString(), anyString())).thenReturn(keycloakLoginResponseDTO);
    LoginResponseDTO loginResponseDTO = easyRandom.nextObject(LoginResponseDTO.class);
    ResponseEntity<LoginResponseDTO> responseEntity = new ResponseEntity<>(loginResponseDTO,
        HttpStatus.OK);
    when(rocketChatService.loginUserFirstTime(USER_DTO_SUCHT.getUsername(),
        USER_DTO_SUCHT.getPassword())).thenReturn(responseEntity);

    assertThrows(InternalServerErrorException.class,
        () -> anonymousUserCreatorService.createAnonymousUser(USER_DTO_SUCHT));
  }
}
