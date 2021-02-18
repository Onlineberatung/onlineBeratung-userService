package de.caritas.cob.userservice.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.rocketchat.login.DataDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class KeycloakServiceTest {

  private final String FIELD_NAME_ROCKET_CHAT_SYSTEM_AUTH_TOKEN = "systemUserAuthToken";
  private final String FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID = "systemUserId";
  private final String USER_ID = "asdh89sdfsjodifjsdf";
  private final String OLD_PW = "oldP@66w0rd!";
  private final String NEW_PW = "newP@66w0rd!";
  private final String REFRESH_TOKEN = "s09djf0w9ejf09wsejf09wjef";
  private final LoginResponseDTO LOGIN_RESPONSE_DTO_SYSTEM_USER =
      new LoginResponseDTO("status", new DataDTO(FIELD_NAME_ROCKET_CHAT_SYSTEM_AUTH_TOKEN,
          FIELD_NAME_ROCKET_CHAT_SYSTEM_USER_ID, null));

  @InjectMocks
  private KeycloakService keycloakService;
  @Mock
  private RestTemplate restTemplate;
  @Mock
  private Logger logger;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private KeycloakAdminClientService keycloakAdminClientService;


  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    setField(keycloakService, "keycloakLoginUrl",
        "http://caritas.local/auth/realms/caritas-online-beratung/protocol/openid-connect/token");
    setField(keycloakService, "keycloakLogoutUrl",
        "http://caritas.local/auth/realms/caritas-online-beratung/protocol/openid-connect/logout");
    setField(keycloakService, "keycloakClientId", "app");
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void changePassword_Should_ReturnTrue_WhenKeycloakPasswordChangeWasSuccessful() {
    assertTrue(keycloakService.changePassword(USER_ID, NEW_PW));
  }

  @Test
  public void changePassword_Should_ReturnFalseAndLogError_WhenKeycloakPasswordChangeFailsWithException() {
    Exception exception = new RuntimeException();
    doThrow(exception).when(keycloakAdminClientService).updatePassword(USER_ID, NEW_PW);

    assertFalse(keycloakService.changePassword(USER_ID, NEW_PW));
    verify(logger, atLeastOnce()).info(anyString(), anyString(), anyString());
  }

  @Test
  public void loginUser_Should_ReturnHttpStatusOK_WhenKeycloakLoginWasSuccessful() {
    when(restTemplate.postForEntity(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<LoginResponseDTO>>any())).thenReturn(
        new ResponseEntity<>(LOGIN_RESPONSE_DTO_SYSTEM_USER, HttpStatus.OK));

    HttpStatus status = keycloakService.loginUser(USER_ID, OLD_PW).get().getStatusCode();

    assertEquals(HttpStatus.OK, status);
  }

  @Test
  public void loginUser_Should_ReturnBadRequestAndLogError_WhenKeycloakLoginFailsWithException() {
    HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
    when(restTemplate.postForEntity(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<LoginResponseDTO>>any())).thenThrow(exception);

    HttpStatus status = keycloakService.loginUser(USER_ID, OLD_PW).get().getStatusCode();

    assertEquals(HttpStatus.BAD_REQUEST, status);
    verify(logger, atLeastOnce()).info(anyString(), anyString(), anyString());
  }

  @Test
  public void logoutUser_Should_ReturnTrue_WhenKeycloakLoginWasSuccessful() {
    when(restTemplate.postForEntity(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<Void>>any()))
        .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));

    assertTrue(keycloakService.logoutUser(REFRESH_TOKEN));
  }

  @Test
  public void logoutUser_Should_ReturnFalseAndLogError_WhenKeycloakLogoutFailsWithException() {
    RestClientException exception = new RestClientException("error");
    when(restTemplate.postForEntity(ArgumentMatchers.anyString(), any(), any()))
        .thenThrow(exception);

    boolean response = keycloakService.logoutUser(REFRESH_TOKEN);

    assertFalse(response);
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void logoutUser_Should_ReturnFalseAndLogError_WhenKeycloakLogoutFails() {
    when(restTemplate.postForEntity(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<Void>>any()))
        .thenReturn(new ResponseEntity<Void>(HttpStatus.BAD_REQUEST));

    boolean response = keycloakService.logoutUser(REFRESH_TOKEN);

    assertFalse(response);
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }
}
