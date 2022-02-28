package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_AVAILABLE;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.USERNAME_NOT_AVAILABLE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.config.auth.Authority;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Helper class for the KeycloakService. Communicates to the Keycloak Admin API over the Keycloak
 * Admin Client.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminClientService {

  @Value("${keycloakService.user.role}")
  private String keycloakUserRole;

  @Value("${api.error.keycloakError}")
  private String keycloakError;

  private final UsernameTranscoder usernameTranscoder = new UsernameTranscoder();

  private final @NonNull UserHelper userHelper;
  private final @NonNull KeycloakAdminClientAccessor keycloakAdminClientAccessor;
  private final IdentityClientConfig identityClientConfig;

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
   * @param user      {@link UserDTO}
   * @param firstName first name of user
   * @param lastName  last name of user
   * @return {@link KeycloakCreateUserResponseDTO}
   */
  public KeycloakCreateUserResponseDTO createKeycloakUser(final UserDTO user,
      final String firstName, final String lastName) {
    var kcUser = getUserRepresentation(user, firstName, lastName);
    try (var response = this.keycloakAdminClientAccessor.getUsersResource().create(kcUser)) {

      if (response.getStatus() == HttpStatus.CREATED.value()) {
        return new KeycloakCreateUserResponseDTO(getCreatedUserId(response.getLocation()));
      }
      handleCreateKeycloakUserError(response);
    }
    throw new InternalServerErrorException(
        String.format("Could not create Keycloak account for: %s %nKeycloak error: %s", user,
            keycloakError));
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
    return this.keycloakAdminClientAccessor.getUsersResource()
        .search(email, 0, Integer.MAX_VALUE)
        .stream()
        .anyMatch(userRepresentation -> userRepresentation.getEmail().equals(email));
  }

  private CredentialRepresentation getCredentialRepresentation(final String password) {
    var credentials = new CredentialRepresentation();
    credentials.setType(CredentialRepresentation.PASSWORD);
    credentials.setValue(password);
    credentials.setTemporary(false);

    return credentials;
  }

  private UserRepresentation getUserRepresentation(final UserDTO user, final String firstName,
      final String lastName) {
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
    kcUser.setEnabled(true);

    return kcUser;
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
    updateRole(userId, keycloakUserRole);
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
   * @param role   {@link UserRole}
   */
  public void updateRole(final String userId, final UserRole role) {
    this.updateRole(userId, role.getValue());
  }

  /**
   * Assigns the role with the given name to the given user ID.
   *
   * @param userId   Keycloak user ID
   * @param roleName Keycloak role name
   */
  public void updateRole(final String userId, final String roleName) {
    // Get realm and user resources
    var realmResource = this.keycloakAdminClientAccessor.getRealmResource();
    UsersResource userRessource = realmResource.users();
    UserResource user = userRessource.get(userId);
    var isRoleUpdated = false;

    // Assign role
    var roleRepresentation = realmResource.roles().get(roleName).toRepresentation();
    if (isNull(roleRepresentation.getAttributes())) {
      roleRepresentation.setAttributes(new LinkedHashMap<>());
    }
    user.roles().realmLevel()
        .add(Collections.singletonList(roleRepresentation));

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
   * @param userId   Keycloak user ID
   * @param password user password
   */
  public void updatePassword(final String userId, final String password) {
    var newCredentials = getCredentialRepresentation(password);
    var userResource = this.keycloakAdminClientAccessor.getUsersResource()
        .get(userId);

    userResource.resetPassword(newCredentials);
    log.debug("Updated user credentials for {}", userId);
  }

  /**
   * If user didn't provide an email, set to dummy address (userId@caritas-online-beratung.de). No *
   * success/error status possible, because the Keycloak Client doesn't provide one either. *
   *
   * @param userId Keycloak user ID
   * @param user   {@link UserDTO}
   * @return the (dummy) email address
   */
  public String updateDummyEmail(final String userId, UserDTO user) {
    String dummyEmail = userHelper.getDummyEmail(userId);
    user.setEmail(dummyEmail);
    var userResource = this.keycloakAdminClientAccessor.getUsersResource()
        .get(userId);

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
   * @param userId    Keycloak user ID
   * @param userDTO   {@link UserDTO}
   * @param firstName the new first name
   * @param lastName  the new last name
   */
  public void updateUserData(final String userId, UserDTO userDTO,
      String firstName, String lastName) {
    var userResource = this.keycloakAdminClientAccessor.getUsersResource()
        .get(userId);
    verifyEmail(userResource, userDTO.getEmail());
    userResource.update(getUserRepresentation(userDTO, firstName, lastName));
  }

  private void verifyEmail(UserResource userResource, String email) {
    if (hasEmailAddressChanged(userResource, email) && isEmailNotAvailable(email)) {
      throw new CustomValidationHttpStatusException(EMAIL_NOT_AVAILABLE, HttpStatus.CONFLICT);
    }
  }

  private boolean hasEmailAddressChanged(UserResource userResource, String email) {
    return !userResource.toRepresentation().getEmail().equals(email);
  }

  /**
   * Updates the email address of user with given id in keycloak.
   *
   * @param userId       Keycloak user ID
   * @param emailAddress the email address to set
   */
  public void updateEmail(String userId, String emailAddress) {
    var userResource = this.keycloakAdminClientAccessor.getUsersResource()
        .get(userId);
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
    this.keycloakAdminClientAccessor.getUsersResource()
        .get(userId)
        .remove();
  }

  /**
   * Returns true if the given user has the provided authority.
   *
   * @param userId    Keycloak user ID
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
   * @param userId   Keycloak user ID
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
    return this.keycloakAdminClientAccessor.getUsersResource()
        .get(userId)
        .roles()
        .realmLevel()
        .listAll();
  }

  /**
   * Returns a list of {@link UserRepresentation} containing all users that match the given search
   * string.
   *
   * @param username Keycloak user name
   * @return {@link List} of found users
   */
  public List<UserRepresentation> findByUsername(String username) {
    return this.keycloakAdminClientAccessor.getUsersResource()
        .search(username);
  }

  /**
   * Closes the provided session.
   *
   * @param sessionId Keycloak session ID
   */
  public void closeSession(String sessionId) {
    this.keycloakAdminClientAccessor.getRealmResource()
        .deleteSession(sessionId);
  }

  /**
   * Deactivates the user account.
   *
   * @param userId the user id to be deactivated
   */
  public void deactivateUser(String userId) {
    var userResource = this.keycloakAdminClientAccessor.getUsersResource()
        .get(userId);
    var userRepresentation = userResource.toRepresentation();
    userRepresentation.setEnabled(false);
    userResource.update(userRepresentation);
  }

}
