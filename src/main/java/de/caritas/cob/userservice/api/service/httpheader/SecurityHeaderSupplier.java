package de.caritas.cob.userservice.api.service.httpheader;

import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityHeaderSupplier {

  @Value("${csrf.header.property}")
  private String csrfHeaderProperty;

  @Value("${csrf.cookie.property}")
  private String csrfCookieProperty;

  private final @NonNull AuthenticatedUser authenticatedUser;

  /**
   * Returns a {@link HttpHeaders} instance with needed settings for the services API (CSRF Token).
   *
   * @return {@link HttpHeaders}
   */
  public HttpHeaders getCsrfHttpHeaders() {
    var httpHeaders = new HttpHeaders();

    return this.addCsrfValues(httpHeaders);
  }

  /**
   * Creates the headers containing keycloak token and csrf headers {@link HttpHeaders} object.
   *
   * @return the created {@link HttpHeaders}
   */
  public HttpHeaders getKeycloakAndCsrfHttpHeaders() {
    var header = getCsrfHttpHeaders();
    this.addKeycloakAuthorizationHeader(header, authenticatedUser.getAccessToken());
    return header;
  }

  /**
   * Creates the headers containing keycloak token of technical user and csrf headers {@link
   * HttpHeaders} object.
   *
   * @param accessToken the token used for keycloak authorization header
   * @return the created {@link HttpHeaders}
   */
  public HttpHeaders getKeycloakAndCsrfHttpHeaders(String accessToken) {
    var header = getCsrfHttpHeaders();
    this.addKeycloakAuthorizationHeader(header, accessToken);

    return header;
  }

  private void addKeycloakAuthorizationHeader(HttpHeaders httpHeaders, String accessToken) {
    httpHeaders.add("Authorization", "Bearer " + accessToken);
  }

  private HttpHeaders addCsrfValues(HttpHeaders httpHeaders) {
    var csrfToken = UUID.randomUUID().toString();

    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.add("Cookie", csrfCookieProperty + "=" + csrfToken);
    httpHeaders.add(csrfHeaderProperty, csrfToken);

    return httpHeaders;
  }
}
