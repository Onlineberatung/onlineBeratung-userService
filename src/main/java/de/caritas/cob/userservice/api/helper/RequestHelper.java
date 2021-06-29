package de.caritas.cob.userservice.api.helper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class RequestHelper {

  private static final String HEADER_AUTHORIZATION_KEY = "Authorization";
  private static final String HEADER_BEARER_KEY = "Bearer ";

  private RequestHelper(){
    throw new IllegalStateException("Utility class");
  }

  public static HttpHeaders getAuthorizedFormHttpHeaders(String bearerToken) {
    var httpHeaders = getFormHttpHeaders();
    httpHeaders.add(HEADER_AUTHORIZATION_KEY,
        HEADER_BEARER_KEY + bearerToken);

    return httpHeaders;
  }

  public static HttpHeaders getFormHttpHeaders() {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    return httpHeaders;
  }

}
