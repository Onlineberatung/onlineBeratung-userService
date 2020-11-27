package de.caritas.cob.userservice.api.service.helper;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;

@Component
public class ServiceHelper {

  @Value("${csrf.header.property}")
  private String csrfHeaderProperty;

  @Value("${csrf.cookie.property}")
  private String csrfCookieProperty;

  @Autowired
  private AuthenticatedUser authenticatedUser;

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
   * Creates the headers containing keylocak token and csrf headers {@link HttpHeaders} object.
   *
   * @return the created {@link HttpHeaders}
   */
  public HttpHeaders getKeycloakAndCsrfHttpHeaders() {
    HttpHeaders header = getCsrfHttpHeaders();
    this.addKeycloakAuthorizationHeader(header);

    return header;
  }

  /**
   * Adds the Rocket.Chat user id, token and group id to the given {@link HttpHeaders} object.
   *
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @param rcGroupId Rocket.Chat group ID
   */
  public HttpHeaders getRocketChatAndCsrfHttpHeaders(RocketChatCredentials rocketChatCredentials,
      String rcGroupId) {
    HttpHeaders header = getCsrfHttpHeaders();
    header.add("rcUserId", rocketChatCredentials.getRocketChatUserId());
    header.add("rcToken", rocketChatCredentials.getRocketChatToken());
    header.add("rcGroupId", rcGroupId);

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
