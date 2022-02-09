package de.caritas.cob.userservice.api.adapters.keycloak.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class KeycloakRestTemplate {

  private final RestTemplate restTemplate;

  public <T> ResponseEntity<T> get(String bearerToken, String url, Class<T> responseType) {
    var httpHeaders = httpHeaders(bearerToken);
    var entity = new HttpEntity<>(httpHeaders);

    return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
  }

  public <T> void putForEntity(String bearerToken, String url, @Nullable Object request,
      Class<T> responseType) throws RestClientException {
    var httpHeaders = httpHeaders(bearerToken);
    var entity = new HttpEntity<>(request, httpHeaders);

    restTemplate.put(url, entity, responseType);
  }

  public <T> ResponseEntity<T> delete(String bearerToken, String url, Class<T> responseType) {
    var httpHeaders = httpHeaders(bearerToken);
    var entity = new HttpEntity<>(httpHeaders);

    return restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType);
  }

  @NonNull
  private HttpHeaders httpHeaders(String bearerToken) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    httpHeaders.add("Authorization", "Bearer " + bearerToken);

    return httpHeaders;
  }
}
