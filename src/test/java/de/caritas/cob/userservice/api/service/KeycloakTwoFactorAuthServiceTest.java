package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.OTP_INFO_DTO;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.config.IdentityConfig;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientAccessor;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class KeycloakTwoFactorAuthServiceTest {

  private static final String BEARER_TOKEN = "token";
  private static final String USERNAME = "testuser";

  @InjectMocks
  private KeycloakTwoFactorAuthService keycloakTwoFactorAuthService;

  @Mock
  private RestTemplate restTemplate;
  @Mock
  private KeycloakAdminClientAccessor keycloakAdminClientAccessor;
  @Mock
  private IdentityConfig identityConfig;

  @Before
  public void setup() {
    givenAKeycloakOtpInfoUrl();
    givenAKeycloakOtpSetupUrl();
    givenAKeycloakOtpTeardownUrl();
  }

  @Test
  public void getOtpCredential_Should_Return_ResponseAsOptional_When_RequestWasSuccessfully() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);
    when(this.restTemplate.exchange(anyString(), any(), any(), (Class<Object>) any()))
        .thenReturn(new ResponseEntity(OTP_INFO_DTO, HttpStatus.OK));

    assertEquals(Optional.of(OTP_INFO_DTO),
        keycloakTwoFactorAuthService.getOtpCredential(USERNAME));
  }

  @Test
  public void getOtpCredential_Should_Return_Empty_Optional_When_RequestHasAnError() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);
    when(this.restTemplate.exchange(anyString(), any(), any(), (Class<Object>) any()))
        .thenThrow(new RestClientException("Fail test case"));

    assertEquals(Optional.empty(), keycloakTwoFactorAuthService.getOtpCredential(USERNAME));
  }

  @Test(expected = InternalServerErrorException.class)
  public void setUpOtpCredential_Should_Throw_InternalServerErrorException_When_RequestHasAnError() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);
    doThrow(new RestClientException("Fail test case")).when(restTemplate)
        .put(anyString(), any(), (Class<Object>) any());

    keycloakTwoFactorAuthService
        .setUpOtpCredential(USERNAME, new EasyRandom().nextObject(OtpSetupDTO.class));
  }

  @Test
  public void setUpOtpCredential_ShouldNot_ThrowInternalServerErrorException_When_RequestWasSuccessfully() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);

    assertDoesNotThrow(() -> keycloakTwoFactorAuthService
        .setUpOtpCredential(USERNAME, new EasyRandom().nextObject(OtpSetupDTO.class)));
  }

  @Test(expected = InternalServerErrorException.class)
  public void deleteOtpCredential_Should_ThrowBadRequestException_When_RequestHasAnError() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);
    doThrow(new RestClientException("Fail test case")).when(restTemplate)
        .exchange(anyString(), any(), any(), (Class<Object>) any());

    keycloakTwoFactorAuthService.deleteOtpCredential(USERNAME);
  }

  @Test
  public void deleteOtpCredential_Should_Not_ThrowBadRequestException_When_RequestWasSuccessfully() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);

    assertDoesNotThrow(() -> keycloakTwoFactorAuthService
        .deleteOtpCredential(USERNAME));
  }

  private void givenAKeycloakOtpInfoUrl() {
    when(identityConfig.getOtpInfoUrl()).thenReturn(
        "https://caritas.local/auth/realms/caritas-online-beratung/otp-config/fetch-otp-setup-info/"
    );
  }

  private void givenAKeycloakOtpSetupUrl() {
    when(identityConfig.getOtpSetupUrl()).thenReturn(
        "https://caritas.local/auth/realms/caritas-online-beratung/otp-config/setup-otp/"
    );
  }

  private void givenAKeycloakOtpTeardownUrl() {
    when(identityConfig.getOtpTeardownUrl()).thenReturn(
        "https://caritas.local/auth/realms/caritas-online-beratung/otp-config/delete-otp/"
    );
  }
}
