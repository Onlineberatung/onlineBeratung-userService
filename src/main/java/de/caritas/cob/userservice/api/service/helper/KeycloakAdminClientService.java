package de.caritas.cob.userservice.api.service.helper;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_AVAILABLE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.authorization.Authorities;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.CreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.service.LogService;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import org.keycloak.admin.client.resource.RealmResource;
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
@Service
@RequiredArgsConstructor
public class KeycloakAdminClientService {

  @Value("${keycloakService.user.role}")
  private String keycloakUserRole;

  @Value("${keycloakService.techuser.id}")
  private String keycloakTechUserId;

  @Value("${api.error.keycloakError}")
  private String keycloakError;

  @Value("${keycloakApi.error.username}")
  private String keycloakErrorUsername;

  @Value("${keycloakApi.error.email}")
  private String keycloakErrorEmail;

  private final @NonNull UserHelper userHelper;
  private final @NonNull KeycloakAdminClientAccessor keycloakAdminClientAccessor;

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
    UserRepresentation kcUser = getUserRepresentation(user, firstName, lastName);
    Response response = this.keycloakAdminClientAccessor.getUsersResource()
        .create(kcUser);
    KeycloakCreateUserResponseDTO keycloakResponse = new KeycloakCreateUserResponseDTO();
    int usernameAvailable = 1;
    int emailAvailable = 1;

    if (response.getStatus() == HttpStatus.CREATED.value()) {
      return new KeycloakCreateUserResponseDTO(getCreatedUserId(response.getLocation()));
    } else {
      String errorMsg = response.readEntity(ErrorRepresentation.class).getErrorMessage();
      keycloakResponse.setStatus(HttpStatus.CONFLICT);

      // Check whether username and/or e-mail address are already taken and set the appropriate
      // error codes and messages
      if (errorMsg.equals(keycloakErrorEmail)) {
        // Only e-mail address is already taken
        emailAvailable = 0;
      } else if (errorMsg.equals(keycloakErrorUsername)) {
        // Username is taken
        usernameAvailable = 0;

        if (isEmailNotAvailable(user.getEmail())) {
          // and e-mail address is taken also
          emailAvailable = 0;
        }
      } else {
        throw new KeycloakException(keycloakError);
      }
    }

    if (keycloakResponse.getStatus().equals(HttpStatus.CONFLICT)) {
      keycloakResponse.setResponseDTO(
          new CreateUserResponseDTO().usernameAvailable(usernameAvailable)
              .emailAvailable(emailAvailable));
    }

    return keycloakResponse;
  }

  /**
   * Returns true if the decoded username does not exist in Keycloak yet or false if it already
   * exists.
   *
   * @param username (decoded or encoded)
   * @return true if does not exist, else false
   */
  public boolean isUsernameAvailable(String username) {
    List<UserRepresentation> keycloakUserList = findByUsername(userHelper.decodeUsername(username));
    for (UserRepresentation userRep : keycloakUserList) {
      if (userRep.getUsername().equalsIgnoreCase(userHelper.decodeUsername(username))) {
        return false;
      }
    }

    return true;
  }

  @Synchronized
  private boolean isEmailNotAvailable(String email) {
    // Get user resource and change e-mail address of technical user
    UserResource techUserResource =
        this.keycloakAdminClientAccessor.getUsersResource()
            .get(keycloakTechUserId);
    UserRepresentation userRepresentation = techUserResource.toRepresentation();
    String originalEmail = userRepresentation.getEmail();
    userRepresentation.setEmail(email);
    // Try to update technical user's e-mail address
    try {
      techUserResource.update(userRepresentation);
    } catch (Exception e) {
      LogService.logDebug(String.format("E-Mail address already existing in Keycloak: %s", email));
      return true;
    }

    // Reset technical user
    userRepresentation.setEmail(originalEmail);
    techUserResource.update(userRepresentation);

    return false;
  }

  private CredentialRepresentation getCredentialRepresentation(final String password) {
    CredentialRepresentation credentials = new CredentialRepresentation();
    credentials.setType(CredentialRepresentation.PASSWORD);
    credentials.setValue(password);
    credentials.setTemporary(false);

    return credentials;
  }

  private UserRepresentation getUserRepresentation(final UserDTO user, final String firstName,
      final String lastName) {
    UserRepresentation kcUser = new UserRepresentation();
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

  /**
   * Assigns the role with the given name to the given user ID.
   *
   * @param userId   Keycloak user ID
   * @param roleName Keycloak role name
   */
  public void updateRole(final String userId, final String roleName) {
    // Get realm and user resources
    RealmResource realmResource = this.keycloakAdminClientAccessor.getRealmResource();
    UsersResource userRessource = realmResource.users();
    UserResource user = userRessource.get(userId);
    boolean isRoleUpdated = false;

    // Assign role
    RoleRepresentation roleRepresentation = realmResource.roles().get(roleName).toRepresentation();
    if (isNull(roleRepresentation.getAttributes())) {
      roleRepresentation.setAttributes(new LinkedHashMap<>());
    }
    user.roles().realmLevel()
        .add(Collections.singletonList(roleRepresentation));

    // Check if role has been assigned successfully
    List<RoleRepresentation> userRoles = user.roles().realmLevel().listAll();
    for (RoleRepresentation role : userRoles) {
      if (role.toString().equalsIgnoreCase(roleName)) {
        LogService.logDebug(String.format("Added role \"user\" to %s", userId));
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
    CredentialRepresentation newCredentials = getCredentialRepresentation(password);
    UserResource userResource = this.keycloakAdminClientAccessor.getUsersResource()
        .get(userId);

    userResource.resetPassword(newCredentials);
    LogService.logDebug(String.format("Updated user credentials for %s", userId));
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
    UserResource userResource = this.keycloakAdminClientAccessor.getUsersResource()
        .get(userId);

    userResource.update(getUserRepresentation(user, null, null));
    LogService.logDebug(String.format("Set email dummy for %s to %s", userId, dummyEmail));

    return dummyEmail;
  }

  /**
   * Updates first name, last name and email address of user wth given id in keycloak.
   *
   * @param userId Keycloak user ID
   * @param userDTO {@link UserDTO}
   * @param firstName the new first name
   * @param lastName the new last name
   */
  public void updateUserData(final String userId, UserDTO userDTO,
      String firstName, String lastName) {
    if (isEmailNotAvailable(userDTO.getEmail())) {
      throw new CustomValidationHttpStatusException(EMAIL_NOT_AVAILABLE);
    }
    UserResource userResource = this.keycloakAdminClientAccessor.getUsersResource()
        .get(userId);
    userResource.update(getUserRepresentation(userDTO, firstName, lastName));
  }

  /**
   * Delete the user if something went wrong during the registration process.
   *
   * @param userId Keycloak user ID
   */
  public void rollBackUser(String userId) {
    try {
      this.keycloakAdminClientAccessor.getUsersResource()
          .get(userId)
          .remove();
      LogService.logDebug(String.format("User %s has been removed due to rollback", userId));
    } catch (Exception e) {
      LogService
          .logKeycloakError(String.format("User could not be removed/rolled back: %s", userId));
    }
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
          .map(Authorities::getAuthoritiesByUserRole)
          .anyMatch(currentAuthority -> currentAuthority.contains(authority));
    } catch (Exception ex) {
      String error = String.format("Could not get roles for user id %s", userId);
      LogService.logKeycloakError(error, ex);
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

    List<RoleRepresentation> userRoles = null;

    try {
      userRoles = getUserRoles(userId);

    } catch (Exception ex) {
      String error = String.format("Could not get roles for user id %s", userId);
      LogService.logKeycloakError(error, ex);
      throw new KeycloakException(error);
    }

    for (RoleRepresentation role : userRoles) {
      Optional<UserRole> userRoleOptional = UserRole.getRoleByValue(role.getName());
      if (userRoleOptional.isPresent()) {
        if (userRoleOptional.get().getValue().equals(userRole)) {
          return true;
        }
      }
    }

    return false;
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

}
