package de.caritas.cob.userservice.api.helper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Provides helper methods for requests.
 */
public class RequestHelper {

  private static final String HEADER_AUTHORIZATION_KEY = "Authorization";
  private static final String HEADER_BEARER_KEY = "Bearer ";

  private RequestHelper() { }

  /**
   * Create a {@link HttpHeaders} instance with bearer token header.
   *
   * @param bearerToken the bearer token
   * @param mediaType {@link MediaType} of the request
   * @return instance of {@link HttpHeaders}
   */
  public static HttpHeaders getAuthorizedHttpHeaders(String bearerToken, MediaType mediaType) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(mediaType);
    httpHeaders.add(HEADER_AUTHORIZATION_KEY,
        HEADER_BEARER_KEY + bearerToken);
    return httpHeaders;
  }

  /**
   * Create a {@link HttpHeaders} instance with content type application/x-www-form-urlencoded.
   *
   * @return instance of {@link HttpHeaders}
   */
  public static HttpHeaders getFormHttpHeaders() {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    return httpHeaders;
  }

}
