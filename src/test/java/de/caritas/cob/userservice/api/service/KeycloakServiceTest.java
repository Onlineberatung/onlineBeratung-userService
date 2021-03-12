package de.caritas.cob.userservice.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.keycloak.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import org.jeasy.random.EasyRandom;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class KeycloakServiceTest {

  private final String USER_ID = "asdh89sdfsjodifjsdf";
  private final String OLD_PW = "oldP@66w0rd!";
  private final String NEW_PW = "newP@66w0rd!";
  private final String REFRESH_TOKEN = "s09djf0w9ejf09wsejf09wjef";

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

  @Mock
  private UserAccountInputValidator userAccountInputValidator;

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
  public void loginUser_Should_ReturnLoginResponseDTO_When_KeycloakLoginWasSuccessful() {
    LoginResponseDTO loginResponseDTO = new EasyRandom().nextObject(LoginResponseDTO.class);
    when(restTemplate.postForEntity(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<LoginResponseDTO>>any()))
        .thenReturn(new ResponseEntity<>(loginResponseDTO, HttpStatus.OK));

    LoginResponseDTO response = keycloakService.loginUser(USER_ID, OLD_PW);

    assertThat(response, instanceOf(LoginResponseDTO.class));
  }

  @Test
  public void loginUser_Should_ReturnBadRequest_When_KeycloakLoginFails() {
    RestClientResponseException exception = mock(RestClientResponseException.class);
    when(restTemplate.postForEntity(ArgumentMatchers.anyString(), any(),
        ArgumentMatchers.<Class<LoginResponseDTO>>any())).thenThrow(exception);

    try {
      keycloakService.loginUser(USER_ID, OLD_PW);
      fail("Expected exception: BadRequestException");
    } catch (BadRequestException badRequestException) {
      assertTrue("Excepted BadRequestException thrown", true);
    }
  }

  @Test
  public void logoutUser_Should_ReturnTrue_WhenKeycloakLoginWasSuccessful() {
    when(restTemplate
        .postForEntity(ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<Void>>any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

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
    when(restTemplate
        .postForEntity(ArgumentMatchers.anyString(), any(), ArgumentMatchers.<Class<Void>>any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    boolean response = keycloakService.logoutUser(REFRESH_TOKEN);

    assertFalse(response);
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void changeEmailAddress_Should_useServicesCorrectly() {
    when(this.authenticatedUser.getUserId()).thenReturn("userId");
    String email = "mail";

    this.keycloakService.changeEmailAddress(email);

    verify(this.userAccountInputValidator, times(1)).validateEmailAddress(email);
    verify(this.authenticatedUser, times(1)).getUserId();
    verify(this.keycloakAdminClientService, times(1)).updateEmail("userId", email);
  }
}
