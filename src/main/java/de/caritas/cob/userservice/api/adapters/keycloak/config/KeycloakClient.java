package de.caritas.cob.userservice.api.adapters.keycloak.config;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class KeycloakClient {

  private final RestTemplate restTemplate;

  public KeycloakClient(@Qualifier("keycloakRestTemplate") final RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public <T> ResponseEntity<T> get(String bearerToken, String url, Class<T> responseType) {
    var httpHeaders = headersWithBearerToken(bearerToken);
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    var entity = new HttpEntity<>(httpHeaders);

    return restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
  }

  public <T> void putForEntity(String bearerToken, String url, @Nullable Object request,
      Class<T> responseType) throws RestClientException {
    var httpHeaders = headersWithBearerToken(bearerToken);
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);

    var entity = new HttpEntity<>(request, httpHeaders);

    restTemplate.put(url, entity, responseType);
  }

  public <T> ResponseEntity<T> postForEntity(String bearerToken, String url,
      @Nullable Object request,
      Class<T> responseType) throws RestClientException {
    var httpHeaders = headersWithBearerToken(bearerToken);
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);

    var entity = new HttpEntity<>(request, httpHeaders);

    return restTemplate.postForEntity(url, entity, responseType);
  }

  public <T> ResponseEntity<T> delete(String bearerToken, String url, Class<T> responseType) {
    var httpHeaders = headersWithBearerToken(bearerToken);
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    var entity = new HttpEntity<>(httpHeaders);

    return restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType);
  }

  @NonNull
  private HttpHeaders headersWithBearerToken(String bearerToken) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.add("Authorization", "Bearer " + bearerToken);

    return httpHeaders;
  }
}
