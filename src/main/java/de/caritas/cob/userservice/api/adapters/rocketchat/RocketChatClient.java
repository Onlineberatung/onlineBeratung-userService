package de.caritas.cob.userservice.api.adapters.rocketchat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class RocketChatClient {

  private static final String HEADER_AUTH_TOKEN = "X-Auth-Token";
  private static final String HEADER_USER_ID = "X-User-Id";

  private final RestTemplate restTemplate;

  private final RocketChatCredentials rocketChatCredentials;

  private final RocketChatCredentialsProvider rcCredentialHelper;

  public RocketChatClient(
      @Qualifier("rocketChatRestTemplate") final RestTemplate restTemplate,
      final RocketChatCredentials rocketChatCredentials,
      final RocketChatCredentialsProvider rocketChatCredentialsProvider) {
    this.restTemplate = restTemplate;
    this.rocketChatCredentials = rocketChatCredentials;
    this.rcCredentialHelper = rocketChatCredentialsProvider;
  }

  public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType) {
    var entity = new HttpEntity<>(request, httpHeaders());

    return restTemplate.postForEntity(url, entity, responseType);
  }

  public <T> ResponseEntity<T> postForEntity(
      String url, String userId, Object request, Class<T> responseType) {
    var entity = new HttpEntity<>(request, httpHeaders(userId));

    return restTemplate.postForEntity(url, entity, responseType);
  }

  public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType) {
    var entity = new HttpEntity<>(httpHeaders());

    return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
  }

  public <T> ResponseEntity<T> getForEntity(String url, String userId, Class<T> responseType) {
    var entity = new HttpEntity<>(httpHeaders(userId));

    return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
  }

  private HttpHeaders httpHeaders() {
    var systemUser = rcCredentialHelper.getSystemUserSneaky();

    var httpHeaders = new HttpHeaders();
    if (systemUser != null) {
      httpHeaders.add(HEADER_AUTH_TOKEN, systemUser.getRocketChatToken());
      httpHeaders.add(HEADER_USER_ID, systemUser.getRocketChatUserId());
    }

    return httpHeaders;
  }

  private HttpHeaders httpHeaders(String userId) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.add(HEADER_AUTH_TOKEN, rocketChatCredentials.getRocketChatToken());
    httpHeaders.add(HEADER_USER_ID, userId);

    return httpHeaders;
  }
}
