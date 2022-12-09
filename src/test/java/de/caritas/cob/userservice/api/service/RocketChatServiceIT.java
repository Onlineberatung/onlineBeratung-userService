package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceDTO.PresenceStatus;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ActiveProfiles("testing")
class RocketChatServiceIT {

  @Autowired private MessageClient underTest;

  @MockBean
  @Qualifier("rocketChatRestTemplate")
  private RestTemplate restTemplate;

  @MockBean private RocketChatCredentialsProvider rcCredentialsProvider;

  private String chatUserId;
  private PresenceDTO presenceDto;

  @AfterEach
  void reset() {
    chatUserId = null;
    presenceDto = null;
  }

  @Test
  void isLoggedInShouldReturnPositiveStatusWhenOnline()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.ONLINE);

    var isLoggedIn = underTest.isLoggedIn(chatUserId).orElseThrow();

    assertTrue(isLoggedIn);
  }

  @Test
  void isLoggedInShouldReturnPositiveStatusWhenBusy() throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.BUSY);

    var isLoggedIn = underTest.isLoggedIn(chatUserId).orElseThrow();

    assertTrue(isLoggedIn);
  }

  @Test
  void isLoggedInShouldReturnPositiveStatusWhenAway() throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.AWAY);

    var isLoggedIn = underTest.isLoggedIn(chatUserId).orElseThrow();

    assertTrue(isLoggedIn);
  }

  @Test
  void isLoggedInShouldReturnNegativeStatusWhenOffline()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.OFFLINE);

    var isLoggedIn = underTest.isLoggedIn(chatUserId).orElseThrow();

    assertFalse(isLoggedIn);
  }

  @Test
  void isLoggedInShouldReturnEmptyOnClientError() throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAnErroneousPresenceResponse();

    var isLoggedIn = underTest.isLoggedIn(chatUserId);

    assertTrue(isLoggedIn.isEmpty());
  }

  @Test
  void isLoggedInShouldReturnEmptyOnRemoteServerError()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAnInvalidPresenceResponse();

    var isLoggedIn = underTest.isLoggedIn(chatUserId);

    assertTrue(isLoggedIn.isEmpty());
  }

  private void givenAValidPresenceResponse(PresenceStatus present) {
    presenceDto = new PresenceDTO();
    presenceDto.setPresence(present);
    presenceDto.setSuccess(true);

    whenPresenceIsRequested().thenReturn(ResponseEntity.ok(presenceDto));
  }

  private void givenAnInvalidPresenceResponse() {
    whenPresenceIsRequested().thenReturn(ResponseEntity.ok(null));
  }

  private void givenAnErroneousPresenceResponse() {
    var errorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "error", null, null);
    whenPresenceIsRequested().thenThrow(errorException);
  }

  private void givenAValidChatUserId() {
    chatUserId = RandomStringUtils.randomAlphanumeric(17);
  }

  private OngoingStubbing<ResponseEntity<PresenceDTO>> whenPresenceIsRequested() {
    return when(
        restTemplate.exchange(
            eq("https://testing.com/api/v1/users.getPresence?userId=" + chatUserId),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            ArgumentMatchers.<Class<PresenceDTO>>any()));
  }

  private void givenAValidRocketChatSystemUser() throws RocketChatUserNotInitializedException {
    when(rcCredentialsProvider.getSystemUserSneaky()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(rcCredentialsProvider.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
  }
}
