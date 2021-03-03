package de.caritas.cob.userservice.api.service.securityheader;

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
    HttpHeaders httpHeaders = new HttpHeaders();

    return this.addCsrfValues(httpHeaders);
  }

  /**
   * Creates the headers containing keycloak token and csrf headers {@link HttpHeaders} object.
   *
   * @return the created {@link HttpHeaders}
   */
  public HttpHeaders getKeycloakAndCsrfHttpHeaders() {
    HttpHeaders header = getCsrfHttpHeaders();
    this.addKeycloakAuthorizationHeader(header);

    return header;
  }

  private void addKeycloakAuthorizationHeader(HttpHeaders httpHeaders) {
    httpHeaders.add("Authorization", "Bearer " + authenticatedUser.getAccessToken());
  }

  private HttpHeaders addCsrfValues(HttpHeaders httpHeaders) {
    String csrfToken = UUID.randomUUID().toString();

    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.add("Cookie", csrfCookieProperty + "=" + csrfToken);
    httpHeaders.add(csrfHeaderProperty, csrfToken);

    return httpHeaders;
  }

}
