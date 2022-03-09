package de.caritas.cob.userservice.api.adapters.rocketchat;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RocketChatClient {

  private final RestTemplate restTemplate;

  private final HttpServletRequest httpServletRequest;

  public RocketChatClient(@Qualifier("rocketChatRestTemplate") final RestTemplate restTemplate,
      final HttpServletRequest httpServletRequest) {
    this.restTemplate = restTemplate;
    this.httpServletRequest = httpServletRequest;
  }

  public HttpHeaders httpHeaders(String userId) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.add("X-Auth-Token", httpServletRequest.getHeader("chatConsultantToken"));
    httpHeaders.add("X-User-Id", userId);

    return httpHeaders;
  }

  public <T> ResponseEntity<T> postForEntity(String url, String userId, @Nullable Object request,
      Class<T> responseType) {
    var entity = new HttpEntity<>(request, httpHeaders(userId));

    return restTemplate.postForEntity(url, entity, responseType);
  }
}
