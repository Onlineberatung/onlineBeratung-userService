package de.caritas.cob.userservice.api.adapters.rocketchat;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.service.rocketchat.RocketChatCredentialsProvider;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class RocketChatClient {

  private final RestTemplate restTemplate;

  private final HttpServletRequest httpServletRequest;

  private final RocketChatCredentialsProvider rcCredentialHelper;

  public RocketChatClient(@Qualifier("rocketChatRestTemplate") final RestTemplate restTemplate,
      final HttpServletRequest httpServletRequest,
      final RocketChatCredentialsProvider rocketChatCredentialsProvider) {
    this.restTemplate = restTemplate;
    this.httpServletRequest = httpServletRequest;
    this.rcCredentialHelper = rocketChatCredentialsProvider;
  }

  public HttpHeaders httpHeaders() {
    return httpHeaders(null);
  }

  public HttpHeaders httpHeaders(String userId) {
    var httpHeaders = new HttpHeaders();

    if (isNull(userId)) {
      var systemUser = rcCredentialHelper.getSystemUserSneaky();
      httpHeaders.add("X-Auth-Token", systemUser.getRocketChatToken());
      httpHeaders.add("X-User-Id", systemUser.getRocketChatUserId());
    } else {
      httpHeaders.add("X-Auth-Token", httpServletRequest.getHeader("rcToken"));
      httpHeaders.add("X-User-Id", userId);
    }

    return httpHeaders;
  }

  public <T> ResponseEntity<T> postForEntity(String url, String userId, @Nullable Object request,
      Class<T> responseType) {
    var entity = new HttpEntity<>(request, httpHeaders(userId));

    log.info("body: {}, header: {} ", entity.getBody(), entity.getHeaders());
    var responseEntity = restTemplate.postForEntity(url, entity, responseType);
    log.info("response: {}", responseEntity.getBody());

    return responseEntity;
  }

  public <T> ResponseEntity<T> getForEntity(String url, String userId, Class<T> responseType) {
    var entity = new HttpEntity<>(httpHeaders(userId));

    return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
  }
}
