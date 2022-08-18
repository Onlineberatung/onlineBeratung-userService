package de.caritas.cob.userservice.api.adapters.rocketchat;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.adapters.rocketchat.config.RocketChatConfig;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.logout.LogoutResponseDTO;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocketChatCredentialsProvider {

  @Value("${rocket.technical.username}")
  private String technicalUsername;

  @Value("${rocket.technical.password}")
  private String technicalPassword;

  @Value("${rocket.systemuser.username}")
  private String systemUsername;

  @Value("${rocket.systemuser.password}")
  private String systemPassword;

  private final @NonNull RestTemplate restTemplate;

  private final RocketChatConfig rocketChatConfig;

  private static final String HEADER_AUTH_TOKEN = "X-Auth-Token";
  private static final String HEADER_USER_ID = "X-User-Id";
  private static final String ENDPOINT_USER_LOGIN = "/login";
  private static final String ENDPOINT_USER_LOGOUT = "/logout";

  // Tokens
  private final AtomicReference<RocketChatCredentials> techUserA = new AtomicReference<>();
  private final AtomicReference<RocketChatCredentials> techUserB = new AtomicReference<>();
  private final AtomicReference<RocketChatCredentials> systemUserA = new AtomicReference<>();
  private final AtomicReference<RocketChatCredentials> systemUserB = new AtomicReference<>();

  /** Get valid technical user credentials */
  public RocketChatCredentials getTechnicalUser() throws RocketChatUserNotInitializedException {
    return observeNonNullOrLatestUser(this.techUserA, this.techUserB);
  }

  private RocketChatCredentials observeNonNullOrLatestUser(
      AtomicReference<RocketChatCredentials> firstUser,
      AtomicReference<RocketChatCredentials> secondUser)
      throws RocketChatUserNotInitializedException {
    if (areBothUsersNull(firstUser.get(), secondUser.get())) {
      throw new RocketChatUserNotInitializedException("No technical user was initialized");
    }

    return oneOfBothUsersNull(firstUser.get(), secondUser.get())
        .orElseGet(() -> retrieveLatestUser(firstUser.get(), secondUser.get()));
  }

  private boolean areBothUsersNull(
      RocketChatCredentials firstUser, RocketChatCredentials secondUser) {
    return isNull(firstUser) && isNull(secondUser);
  }

  private Optional<RocketChatCredentials> oneOfBothUsersNull(
      RocketChatCredentials firstUser, RocketChatCredentials secondUser) {
    if (isNull(firstUser)) {
      return Optional.of(secondUser);
    }
    if (isNull(secondUser)) {
      return Optional.of(firstUser);
    }
    return Optional.empty();
  }

  private RocketChatCredentials retrieveLatestUser(
      RocketChatCredentials firstUser, RocketChatCredentials secondUser) {
    if (firstUser.getTimeStampCreated().isAfter(secondUser.getTimeStampCreated())) {
      return firstUser;
    } else {
      return secondUser;
    }
  }

  /** Get a valid system user */
  public RocketChatCredentials getSystemUser() throws RocketChatUserNotInitializedException {
    return observeNonNullOrLatestUser(this.systemUserA, this.systemUserB);
  }

  @SneakyThrows
  public RocketChatCredentials getSystemUserSneaky() {
    return getSystemUser();
  }

  /** Update the Credentials */
  public void updateCredentials() throws RocketChatLoginException {
    logoutUserWithLongerLoginTime(techUserA, techUserB);
    logoutUserWithLongerLoginTime(systemUserA, systemUserB);
    loginNullUser(this.techUserA, this.techUserB, this.technicalUsername, this.technicalPassword);
    loginNullUser(this.systemUserA, this.systemUserB, this.systemUsername, this.systemPassword);
  }

  private void logoutUserWithLongerLoginTime(
      AtomicReference<RocketChatCredentials> firstUser,
      AtomicReference<RocketChatCredentials> secondUser) {
    if (nonNull(firstUser.get()) && nonNull(secondUser.get())) {
      if (firstUser.get().getTimeStampCreated().isBefore(secondUser.get().getTimeStampCreated())) {
        logoutUser(firstUser);
      } else {
        logoutUser(secondUser);
      }
    }
  }

  private void loginNullUser(
      AtomicReference<RocketChatCredentials> firstUser,
      AtomicReference<RocketChatCredentials> secondUser,
      String username,
      String password)
      throws RocketChatLoginException {
    if (isNull(firstUser.get()) && isNull(secondUser.get())) {
      firstUser.set(obtainRocketCredentialsForUser(username, password));
    } else {
      if (isNull(firstUser.get())) {
        firstUser.set(obtainRocketCredentialsForUser(username, password));
      }

      if (isNull(secondUser.get())) {
        secondUser.set(obtainRocketCredentialsForUser(username, password));
      }
    }
  }

  private RocketChatCredentials obtainRocketCredentialsForUser(String username, String password)
      throws RocketChatLoginException {

    RocketChatCredentials rcc =
        RocketChatCredentials.builder()
            .timeStampCreated(nowInUtc())
            .rocketChatUsername(username)
            .build();

    try {

      ResponseEntity<LoginResponseDTO> response = loginUser(username, password);

      rcc.setRocketChatToken(requireNonNull(response.getBody()).getData().getAuthToken());
      rcc.setRocketChatUserId(requireNonNull(response.getBody()).getData().getUserId());

    } catch (Exception ex) {
      throw new RocketChatLoginException("Could not login " + username + " user in Rocket.Chat");
    }

    if (isNull(rcc.getRocketChatToken()) || isNull(rcc.getRocketChatUserId())) {
      String error =
          "Could not login "
              + username
              + " user in Rocket.Chat correctly, no authToken or UserId received.";
      throw new RocketChatLoginException(error);
    }

    return rcc;
  }

  /**
   * Performs a login with the given credentials and returns the result.
   *
   * @param username the username
   * @param password the password
   * @return the response entity of the login dto
   * @throws RocketChatLoginException on failure
   */
  public ResponseEntity<LoginResponseDTO> loginUser(String username, String password)
      throws RocketChatLoginException {

    try {
      var headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
      map.add("username", username);
      map.add("password", password);

      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

      var url = rocketChatConfig.getApiUrl(ENDPOINT_USER_LOGIN);
      return restTemplate.postForEntity(url, request, LoginResponseDTO.class);
    } catch (Exception ex) {
      throw new RocketChatLoginException(
          String.format("Could not login user (%s) in Rocket.Chat", username));
    }
  }

  /**
   * Performs a logout with the given credentials and returns true on success.
   *
   * @param rcUserId the rocket chat user id
   * @param rcAuthToken the rocket chat auth token
   * @return true if logout was successful
   */
  public boolean logoutUser(String rcUserId, String rcAuthToken) {
    try {
      var headers = getStandardHttpHeaders(rcAuthToken, rcUserId);

      HttpEntity<Void> request = new HttpEntity<>(headers);

      var url = rocketChatConfig.getApiUrl(ENDPOINT_USER_LOGOUT);
      var response = restTemplate.postForEntity(url, request, LogoutResponseDTO.class);

      return response.getStatusCode() == HttpStatus.OK;

    } catch (Exception ex) {
      log.error("Rocket.Chat Error: Could not log out user id ({}) from Rocket.Chat", rcUserId, ex);

      return false;
    }
  }

  /** Logout a RocketChatCredentials-User */
  private void logoutUser(AtomicReference<RocketChatCredentials> user) {
    this.logoutUser(user.get().getRocketChatUserId(), user.get().getRocketChatToken());
    user.set(null);
  }

  /**
   * Returns a HttpHeaders instance with standard settings (Rocket.Chat-Token, Rocket.Chat-User-ID,
   * MediaType)
   *
   * @return a HttpHeaders instance with the standard settings
   */
  private HttpHeaders getStandardHttpHeaders(String rcToken, String rcUserId) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.add(HEADER_AUTH_TOKEN, rcToken);
    httpHeaders.add(HEADER_USER_ID, rcUserId);

    return httpHeaders;
  }
}
