package de.caritas.cob.userservice.api.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.keycloak.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;

/**
 * Service for Keycloak REST API calls
 */
@Service
public class KeycloakService {

  @Value("${keycloakApi.login}")
  private String KEYCLOAK_LOGIN_URL;

  @Value("${keycloakApi.logout}")
  private String KEYCLOAK_LOGOUT_URL;

  @Value("${keycloakService.app.clientId}")
  private String KEYCLOAK_CLIENT_ID;

  private final String KEYCLOAK_GRANT_TYPE_PW = "password";
  private final String KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
  private final String BODY_KEY_USERNAME = "username";
  private final String BODY_KEY_PASSWORD = "password";
  private final String BODY_KEY_CLIENT_ID = "client_id";
  private final String BODY_KEY_GRANT_TYPE = "grant_type";
  private final String HEADER_AUTHORIZATION_KEY = "Authorization";
  private final String HEADER_BEARER_KEY = "Bearer ";

  private RestTemplate restTemplate;
  private AuthenticatedUser authenticatedUser;
  private KeycloakAdminClientHelper keycloakAdminClientHelper;

  @Autowired
  public KeycloakService(RestTemplate restTemplate, AuthenticatedUser authenticatedUser,
      KeycloakAdminClientHelper keycloakAdminClientHelper) {
    this.restTemplate = restTemplate;
    this.authenticatedUser = authenticatedUser;
    this.keycloakAdminClientHelper = keycloakAdminClientHelper;
  }

  /**
   * Changes the (Keycloak) password of a user and returns true on success.
   *
   * @param userId   Keycloak user ID
   * @param password Keycloak password
   * @return true if password change was successful
   */
  public boolean changePassword(final String userId, final String password) {

    try {
      keycloakAdminClientHelper.updatePassword(userId, password);

    } catch (Exception ex) {
      LogService.logKeycloakError(
          String.format("Could not change password for user with id %s", userId), ex);
      return false;
    }

    return true;
  }

  /**
   * Performs a Keycloak login and returns the Keycloak {@link LoginResponseDTO} on success
   *
   * @param userName
   * @param password
   *
   * @return
   */
  public Optional<ResponseEntity<LoginResponseDTO>> loginUser(final String userName,
      final String password) {

    ResponseEntity<LoginResponseDTO> response = null;
    HttpHeaders httpHeaders = getFormHttpHeaders();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
    map.add(BODY_KEY_USERNAME, userName);
    map.add(BODY_KEY_PASSWORD, password);
    map.add(BODY_KEY_CLIENT_ID, KEYCLOAK_CLIENT_ID);
    map.add(BODY_KEY_GRANT_TYPE, KEYCLOAK_GRANT_TYPE_PW);
    HttpEntity<MultiValueMap<String, String>> request =
        new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);

    try {
      response = restTemplate.postForEntity(KEYCLOAK_LOGIN_URL, request, LoginResponseDTO.class);

    } catch (HttpClientErrorException http4xxEx) {
      LogService.logKeycloakError(String.format(
          "Could not log in user %s because of Keycloak API response 4xx (wrong credentials)",
          userName), http4xxEx);
      return Optional.of(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    if (response != null && response.getBody() != null) {
      return Optional.of(response);

    }

    return Optional.empty();
  }

  /**
   * Performs a Keycloak logout. This only destroys the Keycloak session, the (offline) access token
   * will still be valid until expiration date/time ends.
   *
   * @param refreshToken
   */
  public boolean logoutUser(final String refreshToken) {

    ResponseEntity<Void> response = null;
    HttpHeaders httpHeaders = getFormHttpHeaders();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
    map.add(BODY_KEY_CLIENT_ID, KEYCLOAK_CLIENT_ID);
    map.add(BODY_KEY_GRANT_TYPE, KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN);
    map.add(KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN, refreshToken);
    HttpEntity<MultiValueMap<String, String>> request =
        new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);

    try {
      response = restTemplate.postForEntity(KEYCLOAK_LOGOUT_URL, request, Void.class);

    } catch (Exception ex) {
      LogService.logKeycloakError(
          String.format("Could not log out user with refresh token %s", refreshToken), ex);
      return false;
    }

    if (response == null || !response.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
      LogService.logKeycloakError(
          String.format("Could not log out user with refresh token %s", refreshToken));
      return false;
    }

    return true;
  }

  /**
   * Creates and returns {@link HttpHeaders} containing the x-www-form-urlencoded {@link MediaType}
   * and the Bearer Authorization token
   *
   * @return
   */
  private HttpHeaders getFormHttpHeaders() {

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    httpHeaders.add(HEADER_AUTHORIZATION_KEY,
        HEADER_BEARER_KEY + authenticatedUser.getAccessToken());

    return httpHeaders;
  }

}
