package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.testHelper.TestConstants.OTP_INFO_DTO;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.OtpSetupDTO;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientAccessor;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

  @Test
  public void getOtpCredential_Should_Return_Response_As_Optional_When_Request_Was_Successfully() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);
    when(this.restTemplate.exchange(anyString(), Mockito.any(),
        Mockito.any(), (Class<Object>) Mockito.any())).thenReturn(new ResponseEntity(OTP_INFO_DTO, HttpStatus.OK));
    Assertions.assertEquals(Optional.of(OTP_INFO_DTO), keycloakTwoFactorAuthService.getOtpCredential(USERNAME));
  }

  @Test
  public void getOtpCredential_Should_Return_Empty_Optional_When_Request_Has_An_Error() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);
    when(this.restTemplate.exchange(anyString(), Mockito.any(),
        Mockito.any(), (Class<Object>) Mockito.any())).thenThrow(new RestClientException("Fail test case"));
    Assertions.assertEquals(Optional.empty(), keycloakTwoFactorAuthService.getOtpCredential(USERNAME));
  }

  @Test
  public void setUpOtpCredential_Should_Throw_Bad_Request_Exception_When_Request_Has_An_Error() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);
    Mockito.doThrow(new RestClientException("Fail test case")).when(restTemplate)
        .put(anyString(), Mockito.any(), (Class<Object>) Mockito.any());
    Assertions.assertThrows(BadRequestException.class, () -> keycloakTwoFactorAuthService
        .setUpOtpCredential(USERNAME, new EasyRandom().nextObject(OtpSetupDTO.class)));
  }

  @Test
  public void setUpOtpCredential_Should_Not_Throw_Bad_Request_Exception_When_Request_Was_Successfully() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);
    Assertions.assertDoesNotThrow(() -> keycloakTwoFactorAuthService
        .setUpOtpCredential(USERNAME, new EasyRandom().nextObject(OtpSetupDTO.class)));
  }

  @Test
  public void deleteOtpCredential_Should_Throw_Bad_Request_Exception_When_Request_Has_An_Error() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);
    Mockito.doThrow(new RestClientException("Fail test case")).when(restTemplate)
        .exchange(anyString(), Mockito.any(), Mockito.any(), (Class<Object>) Mockito.any());
    Assertions.assertThrows(InternalServerErrorException.class, () -> keycloakTwoFactorAuthService
        .deleteOtpCredential(USERNAME));
  }

  @Test
  public void deleteOtpCredential_Should_Not_Throw_Bad_Request_Exception_When_Request_Was_Successfully() {
    when(this.keycloakAdminClientAccessor.getBearerToken()).thenReturn(BEARER_TOKEN);
    Assertions.assertDoesNotThrow(() -> keycloakTwoFactorAuthService
        .deleteOtpCredential(USERNAME));
  }

}
