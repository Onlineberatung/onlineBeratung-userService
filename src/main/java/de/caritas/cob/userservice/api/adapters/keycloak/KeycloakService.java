package de.caritas.cob.userservice.api.adapters.keycloak;

import static de.caritas.cob.userservice.api.helper.RequestHelper.getAuthorizedHttpHeaders;
import static de.caritas.cob.userservice.api.helper.RequestHelper.getFormHttpHeaders;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.keycloak.config.KeycloakClient;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.Success;
import de.caritas.cob.userservice.api.model.SuccessWithEmail;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientAccessor;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * Service for Keycloak REST API calls.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService implements IdentityClient {

  private static final String KEYCLOAK_GRANT_TYPE_PW = "password";
  private static final String KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
  private static final String BODY_KEY_USERNAME = "username";
  private static final String BODY_KEY_PASSWORD = "password";
  private static final String BODY_KEY_CLIENT_ID = "client_id";
  private static final String BODY_KEY_GRANT_TYPE = "grant_type";
  private static final String ENDPOINT_OPENID_CONNECT_LOGIN = "/token";
  private static final String ENDPOINT_OPENID_CONNECT_LOGOUT = "/logout";
  private static final String ENDPOINT_OTP_INFO = "/fetch-otp-setup-info/{username}";
  private static final String ENDPOINT_OTP_SETUP = "/setup-otp/{username}";
  private static final String ENDPOINT_OTP_TEARDOWN = "/delete-otp/{username}";
  private static final String ENDPOINT_OTP_VERIFY_EMAIL = "/send-verification-mail/{username}";
  private static final String ENDPOINT_OTP_FINISH_EMAIL = "/setup-otp-mail/{username}";

  private final @NonNull RestTemplate restTemplate;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull UserAccountInputValidator userAccountInputValidator;
  private final @NonNull IdentityClientConfig identityClientConfig;
  private final @NonNull KeycloakAdminClientAccessor keycloakAdminClientAccessor;
  private final @NonNull KeycloakClient keycloakClient;
  private final @NonNull KeycloakMapper keycloakMapper;

  @Value("${keycloakService.app.clientId}")
  private String keycloakClientId;

  /**
   * Changes the (Keycloak) password of a user and returns true on success.
   *
   * @param userId   Keycloak user ID
   * @param password Keycloak password
   * @return true if password change was successful
   */
  public boolean changePassword(final String userId, final String password) {
    try {
      keycloakAdminClientService.updatePassword(userId, password);
    } catch (Exception ex) {
      log.info("Could not change password for user with id {}", userId);
      return false;
    }

    return true;
  }

  /**
   * Performs a Keycloak login and returns the Keycloak {@link KeycloakLoginResponseDTO} on
   * success.
   *
   * @param userName the username
   * @param password the password
   * @return {@link KeycloakLoginResponseDTO}
   */
  public KeycloakLoginResponseDTO loginUser(final String userName, final String password) {

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add(BODY_KEY_USERNAME, userName);
    map.add(BODY_KEY_PASSWORD, password);
    map.add(BODY_KEY_CLIENT_ID, keycloakClientId);
    map.add(BODY_KEY_GRANT_TYPE, KEYCLOAK_GRANT_TYPE_PW);
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, getFormHttpHeaders());

    try {
      return restTemplate
          .postForEntity(
              identityClientConfig.getOpenIdConnectUrl(ENDPOINT_OPENID_CONNECT_LOGIN),
              request, KeycloakLoginResponseDTO.class
          )
          .getBody();

    } catch (RestClientResponseException exception) {
      throw new BadRequestException(String.format("Could not log in user %s into Keycloak: %s",
          userName, exception.getMessage()), exception);
    }
  }


  @Override
  public boolean verifyIgnoringOtp(String username, String password) {
    var requestPayload = Map.of(
        BODY_KEY_USERNAME, username,
        BODY_KEY_PASSWORD, password,
        BODY_KEY_CLIENT_ID, keycloakClientId,
        BODY_KEY_GRANT_TYPE, KEYCLOAK_GRANT_TYPE_PW
    );
    var entity = new HttpEntity<>(requestPayload, getFormHttpHeaders());
    var url = identityClientConfig.getOpenIdConnectUrl(ENDPOINT_OPENID_CONNECT_LOGIN);

    ResponseEntity<KeycloakLoginResponseDTO> loginResponse;
    try {
      loginResponse = restTemplate.postForEntity(url, entity, KeycloakLoginResponseDTO.class);
    } catch (HttpClientErrorException exception) {
      return exception.getStatusCode().equals(HttpStatus.BAD_REQUEST)
          && exception.getResponseBodyAsString().contains("Missing totp"); // but password correct
    }

    var responsePayload = loginResponse.getBody();
    if (nonNull(responsePayload) && nonNull(responsePayload.getRefreshToken())) {
      logoutUser(responsePayload.getRefreshToken());
    }

    return true;
  }

  /**
   * Performs a Keycloak logout. This only destroys the Keycloak session, the (offline) access token
   * will still be valid until expiration date/time ends.
   *
   * @param refreshToken the refreshToken
   * @return true if logout was successful
   */
  public boolean logoutUser(final String refreshToken) {

    var httpHeaders =
        getAuthorizedHttpHeaders(authenticatedUser.getAccessToken(),
            MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add(BODY_KEY_CLIENT_ID, keycloakClientId);
    map.add(BODY_KEY_GRANT_TYPE, KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN);
    map.add(KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN, refreshToken);
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, httpHeaders);

    var url = identityClientConfig.getOpenIdConnectUrl(ENDPOINT_OPENID_CONNECT_LOGOUT);
    try {
      var response = restTemplate.postForEntity(url, request, Void.class);
      return wasLogoutSuccessful(response, refreshToken);
    } catch (Exception ex) {
      log.error("Keycloak error: Could not log out user with refresh token {}", refreshToken, ex);

      return false;
    }
  }

  private boolean wasLogoutSuccessful(ResponseEntity<Void> responseEntity, String refreshToken) {
    if (!responseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
      log.error("Keycloak error: Could not log out user with refresh token {}", refreshToken);

      return false;
    }
    return true;
  }

  /**
   * Updates the email address of user with given id in keycloak.
   *
   * @param emailAddress the email address to set
   */
  public void changeEmailAddress(String emailAddress) {
    this.userAccountInputValidator.validateEmailAddress(emailAddress);
    String userId = this.authenticatedUser.getUserId();
    this.keycloakAdminClientService.updateEmail(userId, emailAddress);
  }

  public void changeEmailAddress(String username, String emailAddress) {
    var lowerEmailAddress = emailAddress.toLowerCase();
    var usersResource = keycloakAdminClientAccessor.getUsersResource();
    var userRepresentation = usersResource.search(username).get(0);
    if (!lowerEmailAddress.equals(userRepresentation.getEmail())) {
      userRepresentation.setEmail(lowerEmailAddress);
      usersResource.get(userRepresentation.getId()).update(userRepresentation);
    }
  }

  public void deleteEmailAddress() {
    keycloakAdminClientService.updateDummyEmail(authenticatedUser.getUserId());
  }

  @Override
  public Map<String, String> findUserByEmail(String email) {
    return keycloakAdminClientAccessor.getUsersResource()
        .search(email, 0, Integer.MAX_VALUE)
        .stream()
        .filter(userRepresentation -> userRepresentation.getEmail().equals(email))
        .findFirst()
        .map(keycloakMapper::mapOf)
        .orElseGet(Map::of);
  }

  @Override
  public OtpInfoDTO getOtpCredential(String userName) {
    var bearerToken = keycloakAdminClientAccessor.getBearerToken();
    var requestUrl = identityClientConfig.getOtpUrl(ENDPOINT_OTP_INFO, userName);
    var response = keycloakClient.get(bearerToken, requestUrl, OtpInfoDTO.class);

    return response.getBody();
  }

  @Override
  public boolean setUpOtpCredential(String userName, String initialCode, String secret) {
    var otpSetupDTO = keycloakMapper.otpSetupDtoOf(initialCode, secret, null);
    var bearerToken = keycloakAdminClientAccessor.getBearerToken();
    var requestUrl = identityClientConfig.getOtpUrl(ENDPOINT_OTP_SETUP, userName);

    try {
      keycloakClient.putForEntity(bearerToken, requestUrl, otpSetupDTO, OtpInfoDTO.class);
      return true;
    } catch (HttpClientErrorException exception) {
      if (exception.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
        return false;
      } else {
        throw exception;
      }
    }
  }

  @Override
  public void deleteOtpCredential(String userName) {
    var bearerToken = keycloakAdminClientAccessor.getBearerToken();
    var requestUrl = identityClientConfig.getOtpUrl(ENDPOINT_OTP_TEARDOWN, userName);
    keycloakClient.delete(bearerToken, requestUrl, Void.class);
  }

  @Override
  public Optional<String> initiateEmailVerification(String username, String email) {
    var otpSetupDTO = keycloakMapper.otpSetupDtoOf(null, null, email);
    var bearerToken = keycloakAdminClientAccessor.getBearerToken();
    var requestUrl = identityClientConfig.getOtpUrl(ENDPOINT_OTP_VERIFY_EMAIL, username);

    try {
      keycloakClient.putForEntity(bearerToken, requestUrl, otpSetupDTO, Success.class);
      return Optional.empty();
    } catch (RestClientException exception) {
      return Optional.of("Keycloak answered: " + exception.getMessage());
    }
  }

  @Override
  public Map<String, String> finishEmailVerification(String username, String initialCode) {
    var otpSetupDTO = keycloakMapper.otpSetupDtoOf(initialCode, null, null);
    var bearerToken = keycloakAdminClientAccessor.getBearerToken();
    var requestUrl = identityClientConfig.getOtpUrl(ENDPOINT_OTP_FINISH_EMAIL, username);

    try {
      var response = keycloakClient.postForEntity(bearerToken, requestUrl, otpSetupDTO,
          SuccessWithEmail.class);
      return keycloakMapper.mapOf(response);
    } catch (HttpClientErrorException exception) {
      return keycloakMapper.mapOf(exception);
    }
  }

  /**
   * Deactivates the user account.
   *
   * @param userId the user id to be deactivated
   */
  public void deactivateUser(String userId) {
    var userResource = keycloakAdminClientAccessor.getUsersResource().get(userId);
    var userRepresentation = userResource.toRepresentation();
    userRepresentation.setEnabled(false);
    userResource.update(userRepresentation);
  }
}
