package de.caritas.cob.userservice.api.adapters.keycloak;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_AVAILABLE;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.USERNAME_NOT_AVAILABLE;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.config.auth.Authority;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import de.caritas.cob.userservice.api.model.Success;
import de.caritas.cob.userservice.api.model.SuccessWithEmail;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/** Service for Keycloak REST API calls. */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService implements IdentityClient {

  private static final String KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
  private static final String BODY_KEY_CLIENT_ID = "client_id";
  private static final String BODY_KEY_GRANT_TYPE = "grant_type";
  private static final String ENDPOINT_OPENID_CONNECT_LOGIN = "/token";
  private static final String ENDPOINT_OPENID_CONNECT_LOGOUT = "/logout";
  private static final String ENDPOINT_OTP_INFO = "/fetch-otp-setup-info/{username}";
  private static final String ENDPOINT_OTP_SETUP = "/setup-otp/{username}";
  private static final String ENDPOINT_OTP_TEARDOWN = "/delete-otp/{username}";
  private static final String ENDPOINT_OTP_VERIFY_EMAIL = "/send-verification-mail/{username}";
  private static final String ENDPOINT_OTP_FINISH_EMAIL = "/setup-otp-mail/{username}";
  private static final String LOCALE = "locale";

  private final @NonNull RestTemplate restTemplate;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull UserAccountInputValidator userAccountInputValidator;
  private final @NonNull IdentityClientConfig identityClientConfig;
  private final @NonNull KeycloakClient keycloakClient;
  private final @NonNull KeycloakMapper keycloakMapper;
  private final @NonNull UserHelper userHelper;

  private final UsernameTranscoder usernameTranscoder = new UsernameTranscoder();

  @Value("${keycloak.config.app-client-id}")
  private String keycloakClientId;

  @Value("${api.error.keycloakError}")
  private String keycloakError;

  @Value("${multitenancy.enabled}")
  private Boolean multiTenancyEnabled;

  /**
   * Changes the (Keycloak) password of a user and returns true on success.
   *
   * @param userId Keycloak user ID
   * @param password Keycloak password
   * @return true if password change was successful
   */
  public boolean changePassword(final String userId, final String password) {
    try {
      updatePassword(userId, password);
    } catch (Exception ex) {
      log.info("Could not change password for user with id {}", userId);
      return false;
    }

    return true;
  }

  public void changeLanguage(final String userId, final String locale) {
    UserResource userResource = keycloakClient.getUsersResource().get(userId);
    var user = userResource.toRepresentation();

    changeLanguageForTheUser(locale, userResource, user);
  }

  protected void changeLanguageForTheUser(
      String locale, UserResource userResource, UserRepresentation user) {
    if (needToUpdateLocale(locale, user)) {
      user.getAttributes().put(LOCALE, Lists.newArrayList(locale));
      userResource.update(user);
    } else {
      log.debug("Skipping language update in keycloak");
    }
  }

  private boolean needToUpdateLocale(String locale, UserRepresentation userRepresentation) {
    return !userRepresentation.getAttributes().containsKey(LOCALE)
        || !userRepresentation.getAttributes().get(LOCALE).contains(locale);
  }

  /**
   * Performs a Keycloak login and returns the Keycloak {@link KeycloakLoginResponseDTO} on success.
   *
   * @param userName the username
   * @param password the password
   * @return {@link KeycloakLoginResponseDTO}
   */
  public KeycloakLoginResponseDTO loginUser(final String userName, final String password) {
    var entity = loginRequest(userName, password);
    var url = identityClientConfig.getOpenIdConnectUrl(ENDPOINT_OPENID_CONNECT_LOGIN);

    try {
      return restTemplate.postForEntity(url, entity, KeycloakLoginResponseDTO.class).getBody();

    } catch (RestClientResponseException exception) {
      throw new BadRequestException(
          String.format(
              "Could not log in user %s into Keycloak: %s", userName, exception.getMessage()),
          exception);
    }
  }

  private HttpEntity<MultiValueMap<String, String>> loginRequest(String userName, String password) {
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("username", userName);
    map.add("password", password);
    map.add(BODY_KEY_CLIENT_ID, keycloakClientId);
    map.add(BODY_KEY_GRANT_TYPE, "password");

    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    return new HttpEntity<>(map, httpHeaders);
  }

  @Override
  public boolean verifyIgnoringOtp(String username, String password) {
    var entity = loginRequest(username, password);
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
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add(BODY_KEY_CLIENT_ID, keycloakClientId);
    map.add(BODY_KEY_GRANT_TYPE, KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN);
    map.add(KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN, refreshToken);

    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    httpHeaders.add("Authorization", "Bearer " + authenticatedUser.getAccessToken());
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
    updateEmail(userId, emailAddress);
  }

  public void changeEmailAddress(String username, String emailAddress) {
    var lowerEmailAddress = emailAddress.toLowerCase();
    var usersResource = keycloakClient.getUsersResource();
    var userRepresentation = usersResource.search(username).get(0);
    if (!lowerEmailAddress.equals(userRepresentation.getEmail())) {
      userRepresentation.setEmail(lowerEmailAddress);
      usersResource.get(userRepresentation.getId()).update(userRepresentation);
    }
  }

  public void deleteEmailAddress() {
    updateDummyEmail(authenticatedUser.getUserId());
  }

  @Override
  public Map<String, String> findUserByEmail(String email) {
    return keycloakClient.getUsersResource().search(email, 0, Integer.MAX_VALUE).stream()
        .filter(userRepresentation -> userRepresentation.getEmail().equals(email))
        .findFirst()
        .map(keycloakMapper::mapOf)
        .orElseGet(Map::of);
  }

  @Override
  public OtpInfoDTO getOtpCredential(String userName) {
    var bearerToken = keycloakClient.getBearerToken();
    var requestUrl = identityClientConfig.getOtpUrl(ENDPOINT_OTP_INFO, userName);
    var response = keycloakClient.get(bearerToken, requestUrl, OtpInfoDTO.class);

    return response.getBody();
  }

  @Override
  public boolean setUpOtpCredential(String userName, String initialCode, String secret) {
    var otpSetupDTO = keycloakMapper.otpSetupDtoOf(initialCode, secret, null);
    var bearerToken = keycloakClient.getBearerToken();
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
    var bearerToken = keycloakClient.getBearerToken();
    var requestUrl = identityClientConfig.getOtpUrl(ENDPOINT_OTP_TEARDOWN, userName);
    keycloakClient.delete(bearerToken, requestUrl, Void.class);
  }

  @Override
  public Optional<String> initiateEmailVerification(String username, String email) {
    var otpSetupDTO = keycloakMapper.otpSetupDtoOf(null, null, email);
    var bearerToken = keycloakClient.getBearerToken();
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
    var bearerToken = keycloakClient.getBearerToken();
    var requestUrl = identityClientConfig.getOtpUrl(ENDPOINT_OTP_FINISH_EMAIL, username);

    try {
      var response =
          keycloakClient.postForEntity(
              bearerToken, requestUrl, otpSetupDTO, SuccessWithEmail.class);
      return keycloakMapper.mapOf(response);
    } catch (HttpClientErrorException exception) {
      return keycloakMapper.mapOf(exception);
    }
  }

  /**
   * Creates a user in Keycloak and returns its Keycloak user ID.
   *
   * @param user {@link UserDTO}
   * @return {@link KeycloakCreateUserResponseDTO}
   */
  public KeycloakCreateUserResponseDTO createKeycloakUser(final UserDTO user) {
    return createKeycloakUser(user, null, null);
  }

  /**
   * Creates a user with firstname and lastname in Keycloak and returns its Keycloak user ID.
   *
   * @param user {@link UserDTO}
   * @param firstName first name of user
   * @param lastName last name of user
   * @return {@link KeycloakCreateUserResponseDTO}
   */
  public KeycloakCreateUserResponseDTO createKeycloakUser(
      final UserDTO user, final String firstName, final String lastName) {
    var locale =
        isNull(user.getPreferredLanguage()) ? "de" : user.getPreferredLanguage().toString();
    var kcUser = getUserRepresentation(user, firstName, lastName, locale);
    try (var response = keycloakClient.getUsersResource().create(kcUser)) {
      if (response.getStatus() == HttpStatus.CREATED.value()) {
        return new KeycloakCreateUserResponseDTO(getCreatedUserId(response.getLocation()));
      }
      handleCreateKeycloakUserError(response);
    }
    throw new InternalServerErrorException(
        String.format(
            "Could not create Keycloak account for: %s %nKeycloak error: %s", user, keycloakError));
  }

  private void handleCreateKeycloakUserError(Response response) {
    String errorMsg = response.readEntity(ErrorRepresentation.class).getErrorMessage();
    if (errorMsg.equals(identityClientConfig.getErrorMessageDuplicatedEmail())) {
      throw new CustomValidationHttpStatusException(EMAIL_NOT_AVAILABLE, HttpStatus.CONFLICT);
    }
    if (errorMsg.equals(identityClientConfig.getErrorMessageDuplicatedUsername())) {
      throw new CustomValidationHttpStatusException(USERNAME_NOT_AVAILABLE, HttpStatus.CONFLICT);
    }
  }

  /**
   * Returns true if the given username does not exist in Keycloak yet or false if it already
   * exists.
   *
   * @param username (decoded or encoded)
   * @return true if does not exist, else false
   */
  public boolean isUsernameAvailable(String username) {
    List<UserRepresentation> keycloakDecodedUserList =
        findByUsername(usernameTranscoder.decodeUsername(username));
    List<UserRepresentation> keycloakEncodedUserList =
        findByUsername(usernameTranscoder.encodeUsername(username));

    return Stream.concat(keycloakDecodedUserList.stream(), keycloakEncodedUserList.stream())
        .noneMatch(user -> doesUsernameMatch(username, user));
  }

  private boolean doesUsernameMatch(String username, UserRepresentation user) {
    return user.getUsername().equalsIgnoreCase(usernameTranscoder.decodeUsername(username))
        || user.getUsername().equalsIgnoreCase(usernameTranscoder.encodeUsername(username));
  }

  @Synchronized
  private boolean isEmailNotAvailable(String email) {
    return keycloakClient.getUsersResource().search(email, 0, Integer.MAX_VALUE).stream()
        .anyMatch(userRepresentation -> userRepresentation.getEmail().equals(email));
  }

  private CredentialRepresentation getCredentialRepresentation(final String password) {
    var credentials = new CredentialRepresentation();
    credentials.setType(CredentialRepresentation.PASSWORD);
    credentials.setValue(password);
    credentials.setTemporary(false);

    return credentials;
  }

  private UserRepresentation getUserRepresentation(
      final UserDTO user, final String firstName, final String lastName) {
    return getUserRepresentation(user, firstName, lastName, null);
  }

  private UserRepresentation getUserRepresentation(
      final UserDTO user, final String firstName, final String lastName, final String locale) {
    var kcUser = new UserRepresentation();
    kcUser.setUsername(user.getUsername());
    kcUser.setEmail(user.getEmail());
    kcUser.setEmailVerified(true);
    if (nonNull(firstName)) {
      kcUser.setFirstName(firstName);
    }
    if (nonNull(lastName)) {
      kcUser.setLastName(lastName);
    }
    if (nonNull(locale)) {
      kcUser.singleAttribute(LOCALE, locale);
    }
    kcUser.setEnabled(true);

    updateTenantId(user, kcUser);

    return kcUser;
  }

  private void updateTenantId(UserDTO userDTO, UserRepresentation kcUser) {
    if (TRUE.equals(multiTenancyEnabled)) {
      Map<String, List<String>> attributes = new HashMap<>();
      var list = new ArrayList<String>();
      if (userDTO.getTenantId() != null) {
        list.add(userDTO.getTenantId().toString());
      } else {
        list.add(TenantContext.getCurrentTenant().toString());
      }
      attributes.put("tenantId", list);
      kcUser.setAttributes(attributes);
    }
  }

  private String getCreatedUserId(final URI location) {
    if (nonNull(location)) {
      String path = location.getPath();
      return path.substring(path.lastIndexOf('/') + 1);
    }

    return null;
  }

  /**
   * Assigns the role "user" to the given user ID.
   *
   * @param userId Keycloak user ID
   */
  public void updateUserRole(final String userId) {
    updateRole(userId, "user");
  }

  public void ensureRole(final String userId, final String roleName) {
    if (!userHasRole(userId, roleName)) {
      updateRole(userId, roleName);
    }
  }

  /**
   * Assigns the given {@link UserRole} to the given user ID.
   *
   * @param userId Keycloak user ID
   * @param role {@link UserRole}
   */
  public void updateRole(final String userId, final UserRole role) {
    this.updateRole(userId, role.getValue());
  }

  @Override
  public void removeRoleIfPresent(final String userId, final String roleName) {
    // Get realm and user resources
    var realmResource = keycloakClient.getRealmResource();
    UsersResource userRessource = realmResource.users();
    UserResource user = userRessource.get(userId);
    // Remove role
    var optionalRole = findRole(user, roleName);
    if (optionalRole.isPresent()) {
      RoleRepresentation roleRepresentation =
          realmResource.roles().get(optionalRole.get()).toRepresentation();
      if (roleRepresentation != null) {
        user.roles().realmLevel().remove(Collections.singletonList(roleRepresentation));
      }
    }
  }

  Optional<String> findRole(UserResource user, String roleName) {

    List<RoleRepresentation> userRoles = user.roles().realmLevel().listAll();
    if (userRoles != null) {
      return userRoles.stream()
          .filter(role -> role.getName() != null && role.getName().equals(roleName))
          .map(RoleRepresentation::getName)
          .findFirst();
    }
    return Optional.empty();
  }

  /**
   * Assigns the role with the given name to the given user ID.
   *
   * @param userId Keycloak user ID
   * @param roleName Keycloak role name
   */
  public void updateRole(final String userId, final String roleName) {
    // Get realm and user resources
    var realmResource = keycloakClient.getRealmResource();
    UsersResource userRessource = realmResource.users();
    UserResource user = userRessource.get(userId);
    var isRoleUpdated = false;

    // Assign role
    var roleRepresentation = realmResource.roles().get(roleName).toRepresentation();
    if (isNull(roleRepresentation.getAttributes())) {
      roleRepresentation.setAttributes(new LinkedHashMap<>());
    }
    user.roles().realmLevel().add(Collections.singletonList(roleRepresentation));

    // Check if role has been assigned successfully
    List<RoleRepresentation> userRoles = user.roles().realmLevel().listAll();
    for (RoleRepresentation role : userRoles) {
      if (role.toString().equalsIgnoreCase(roleName)) {
        log.debug("Added role \"user\" to {}", userId);
        isRoleUpdated = true;
      }
    }

    if (!isRoleUpdated) {
      throw new KeycloakException("Could not update user role");
    }
  }

  /**
   * Updates the Keycloak password for a user.
   *
   * @param userId Keycloak user ID
   * @param password user password
   */
  public void updatePassword(final String userId, final String password) {
    var newCredentials = getCredentialRepresentation(password);
    var userResource = keycloakClient.getUsersResource().get(userId);

    userResource.resetPassword(newCredentials);
    log.debug("Updated user credentials for {}", userId);
  }

  /**
   * If user didn't provide an email, set to dummy address (userId@online-beratung.de). No *
   * success/error status possible, because the Keycloak Client doesn't provide one either. *
   *
   * @param userId Keycloak user ID
   * @param user {@link UserDTO}
   * @return the (dummy) email address
   */
  public String updateDummyEmail(final String userId, UserDTO user) {
    String dummyEmail = userHelper.getDummyEmail(userId);
    user.setEmail(dummyEmail);
    var userResource = keycloakClient.getUsersResource().get(userId);

    userResource.update(getUserRepresentation(user, null, null));
    log.debug("Set email dummy for {} to {}", userId, dummyEmail);

    return dummyEmail;
  }

  /**
   * Sets a user's dummy email
   *
   * @param userId user ID
   */
  public void updateDummyEmail(String userId) {
    updateEmail(userId, userHelper.getDummyEmail(userId));
  }

  /**
   * Updates first name, last name and email address of user with given id in keycloak.
   *
   * @param userId Keycloak user ID
   * @param userDTO {@link UserDTO}
   * @param firstName the new first name
   * @param lastName the new last name
   */
  public void updateUserData(
      final String userId, UserDTO userDTO, String firstName, String lastName) {
    var userResource = keycloakClient.getUsersResource().get(userId);
    verifyEmail(userResource, userDTO.getEmail());
    userResource.update(getUserRepresentation(userDTO, firstName, lastName));
  }

  private void verifyEmail(UserResource userResource, String email) {
    if (hasEmailAddressChanged(userResource, email) && isEmailNotAvailable(email)) {
      throw new CustomValidationHttpStatusException(EMAIL_NOT_AVAILABLE, HttpStatus.CONFLICT);
    }
  }

  private boolean hasEmailAddressChanged(UserResource userResource, String email) {
    UserRepresentation userRepresentation = userResource.toRepresentation();
    if (userRepresentation != null && userRepresentation.getEmail() != null) {
      return !userRepresentation.getEmail().equals(email);
    } else {
      return !ObjectUtils.isEmpty(email);
    }
  }

  /**
   * Updates the email address of user with given id in keycloak.
   *
   * @param userId Keycloak user ID
   * @param emailAddress the email address to set
   */
  public void updateEmail(String userId, String emailAddress) {
    var userResource = keycloakClient.getUsersResource().get(userId);
    verifyEmail(userResource, emailAddress);
    UserRepresentation representation = userResource.toRepresentation();
    representation.setEmail(emailAddress);
    userResource.update(representation);
  }

  /**
   * Delete the user if something went wrong during the registration process.
   *
   * @param userId Keycloak user ID
   */
  public void rollBackUser(String userId) {
    try {
      deleteUser(userId);
      log.debug("User {} has been removed due to rollback", userId);
    } catch (Exception e) {
      log.error("Keycloak error: User could not be removed/rolled back: {}", userId);
    }
  }

  /**
   * Deletes the user with the given user id in keycloak.
   *
   * @param userId the userId
   */
  public void deleteUser(String userId) {
    keycloakClient.getUsersResource().get(userId).remove();
  }

  /**
   * Returns true if the given user has the provided authority.
   *
   * @param userId Keycloak user ID
   * @param authority Keycloak authority
   * @return true if user hast provided authority
   */
  public boolean userHasAuthority(String userId, String authority) {
    try {
      return getUserRoles(userId).stream()
          .map(role -> UserRole.getRoleByValue(role.getName()))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(Authority::getAuthoritiesByUserRole)
          .anyMatch(currentAuthority -> currentAuthority.contains(authority));
    } catch (Exception ex) {
      var error = String.format("Could not get roles for user id %s", userId);
      log.error("Keycloak error: " + error, ex);
      throw new KeycloakException(error);
    }
  }

  /**
   * Returns true if the given user has the provided role.
   *
   * @param userId Keycloak user ID
   * @param userRole Keycloak role
   * @return true if user hast provided role
   */
  public boolean userHasRole(String userId, String userRole) {
    try {
      return getUserRoles(userId).stream()
          .map(this::toUserRole)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(UserRole::getValue)
          .anyMatch(userRole::equals);
    } catch (Exception ex) {
      var error = String.format("Could not get roles for user id %s", userId);
      log.error("Keycloak error: " + error, ex);
      throw new KeycloakException(error);
    }
  }

  private Optional<UserRole> toUserRole(RoleRepresentation roleRepresentation) {
    return UserRole.getRoleByValue(roleRepresentation.getName());
  }

  private List<RoleRepresentation> getUserRoles(String userId) {
    return keycloakClient.getUsersResource().get(userId).roles().realmLevel().listAll();
  }

  /**
   * Returns a list of {@link UserRepresentation} containing all users that match the given search
   * string.
   *
   * @param username Keycloak user name
   * @return {@link List} of found users
   */
  public List<UserRepresentation> findByUsername(String username) {
    return keycloakClient.getUsersResource().search(username);
  }

  public UserRepresentation getById(String userId) {
    UserResource userResource = keycloakClient.getUsersResource().get(userId);
    if (userResource == null) {
      log.error("Could not get user with id {} from keycloak", userId);
      throw new KeycloakException("User with id not found in keycloak: " + userId);
    }
    return userResource.toRepresentation();
  }

  /**
   * Closes the provided session.
   *
   * @param sessionId Keycloak session ID
   */
  public void closeSession(String sessionId) {
    keycloakClient.getRealmResource().deleteSession(sessionId);
  }

  /**
   * Deactivates the user account.
   *
   * @param userId the user id to be deactivated
   */
  public void deactivateUser(String userId) {
    var userResource = keycloakClient.getUsersResource().get(userId);
    var userRepresentation = userResource.toRepresentation();
    userRepresentation.setEnabled(false);
    userResource.update(userRepresentation);
  }
}
