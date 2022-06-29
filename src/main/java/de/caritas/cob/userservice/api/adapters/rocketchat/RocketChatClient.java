package de.caritas.cob.userservice.api.adapters.rocketchat;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.WebUtils;

@Component
@Slf4j
public class RocketChatClient {

  private static final String HEADER_AUTH_TOKEN = "X-Auth-Token";
  private static final String HEADER_USER_ID = "X-User-Id";

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

  public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType) {
    var entity = new HttpEntity<>(request, httpHeaders());

    return restTemplate.postForEntity(url, entity, responseType);
  }

  public <T> ResponseEntity<T> postForEntity(String url, String userId, Object request,
      Class<T> responseType) {
    var entity = new HttpEntity<>(request, httpHeaders(userId));

    return restTemplate.postForEntity(url, entity, responseType);
  }

  public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType) {
    var entity = new HttpEntity<>(httpHeaders());

    return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
  }

  private HttpHeaders httpHeaders() {
    var systemUser = rcCredentialHelper.getSystemUserSneaky();

    var httpHeaders = new HttpHeaders();
    httpHeaders.add(HEADER_AUTH_TOKEN, systemUser.getRocketChatToken());
    httpHeaders.add(HEADER_USER_ID, systemUser.getRocketChatUserId());

    return httpHeaders;
  }

  private HttpHeaders httpHeaders(String userId) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.add(HEADER_AUTH_TOKEN, rcToken());
    httpHeaders.add(HEADER_USER_ID, userId);

    return httpHeaders;
  }

  private String rcToken() {
    var rcToken = httpServletRequest.getHeader("rcToken");
    if (isNull(rcToken)) {
      var cookie = WebUtils.getCookie(httpServletRequest, "rc_token");
      if (nonNull(cookie)) {
        rcToken = cookie.getValue();
      } else {
        throw new BadRequestException("Neither rcToken in header nor rc_token in cookie.");
      }
    }

    return rcToken;
  }
}
