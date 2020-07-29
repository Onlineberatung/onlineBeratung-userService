package de.caritas.cob.UserService.api.service.helper;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.authorization.Authority;
import de.caritas.cob.UserService.api.authorization.UserRole;
import de.caritas.cob.UserService.api.exception.keycloak.KeycloakException;
import de.caritas.cob.UserService.api.helper.UserHelper;
import de.caritas.cob.UserService.api.model.CreateUserResponseDTO;
import de.caritas.cob.UserService.api.model.UserDTO;
import de.caritas.cob.UserService.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.UserService.api.service.LogService;
import de.caritas.cob.UserService.api.service.helper.aspect.KeycloakAdminClientLogout;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for the KeycloakService. Communicates to the Keycloak Admin API over the Keycloak
 * Admin Client.
 */

@Slf4j
@Service
public class KeycloakAdminClientHelper {
  @Value("${keycloak.auth-server-url}")
  private String KEYCLOAK_SERVER_URL;

  @Value("${keycloak.realm}")
  private String KEYCLOAK_REALM;

  @Value("${keycloakService.admin.username}")
  private String KEYCLOAK_USERNAME;

  @Value("${keycloakService.admin.password}")
  private String KEYCLOAK_PASSWORD;

  @Value("${keycloakService.admin.clientId}")
  private String KEYCLOAK_CLIENT_ID;

  @Value("${keycloakService.user.role}")
  private String KEYCLOAK_USER_ROLE;

  @Value("${keycloakService.techuser.id}")
  private String KEYCLOAK_TECH_USER_ID;

  @Value("${api.error.userRegistered}")
  private String ERROR_USER_REGISTERED;

  @Value("${api.error.keycloakError}")
  private String KEYCLOAK_ERROR;

  @Value("${keycloakApi.error.username}")
  private String KEYCLOAK_ERROR_USERNAME;

  @Value("${keycloakApi.error.email}")
  private String KEYCLOAK_ERROR_EMAIL;

  @Value("${user.password.invalid}")
  private String PASSWORD_INVALID;

  @Value("${api.error.emailConflict}")
  private String EMAIL_CONFLICT;

  @Value("${api.error.usernameConflict}")
  private String USERNAME_CONFLICT;

  private Keycloak keycloakInstance;

  @Autowired
  private UserHelper userHelper;
  @Autowired
  LogService logService;

  /**
   * Instantiate Keycloak Admin Client
   * 
   * @return
   */
  private Keycloak getInstance() {

    this.keycloakInstance = Keycloak.getInstance(KEYCLOAK_SERVER_URL, KEYCLOAK_REALM,
        KEYCLOAK_USERNAME, KEYCLOAK_PASSWORD, KEYCLOAK_CLIENT_ID);

    return this.keycloakInstance;
  }

  /**
   * Creates a user in Keycloak and returns its Keycloak user Id
   * 
   * @param user
   * @return
   */
  public KeycloakCreateUserResponseDTO createKeycloakUser(final UserDTO user) throws Exception {
    return createKeycloakUser(user, null, null);
  }

  /**
   * Creates a user with firstname and lastname in Keycloak and returns its Keycloak user Id
   * 
   * @param user
   * @param firstName
   * @param lastName
   * @return
   */
  @KeycloakAdminClientLogout
  public KeycloakCreateUserResponseDTO createKeycloakUser(final UserDTO user,
      final String firstName, final String lastName) throws Exception {
    UserRepresentation kcUser = getUserRepresentation(user, firstName, lastName);
    Response response = getInstance().realm(KEYCLOAK_REALM).users().create(kcUser);
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
      if (errorMsg.equals(KEYCLOAK_ERROR_EMAIL)) {
        // Only e-mail address is already taken
        emailAvailable = 0;
      } else if (errorMsg.equals(KEYCLOAK_ERROR_USERNAME)) {
        // Username is taken
        usernameAvailable = 0;

        if (!isEmailAvailable(user.getEmail())) {
          // and e-mail address is taken also
          emailAvailable = 0;
        }
      } else {
        throw new KeycloakException(KEYCLOAK_ERROR);
      }
    }

    if (keycloakResponse.getStatus().equals(HttpStatus.CONFLICT)) {
      keycloakResponse.setResponseDTO(new CreateUserResponseDTO(usernameAvailable, emailAvailable));
    }

    return keycloakResponse;
  }

  /**
   * Checks if given email address is already existing in Keycloak user database.
   * 
   * @param email
   * @return
   */
  @KeycloakAdminClientLogout
  private boolean isEmailAvailable(String email) throws Exception {
    // Get user resource and change e-mail address of technical user
    UserResource techUserResource =
        getInstance().realm(KEYCLOAK_REALM).users().get(KEYCLOAK_TECH_USER_ID);
    UserRepresentation userRepresentation = techUserResource.toRepresentation();
    String originalEmail = userRepresentation.getEmail();
    userRepresentation.setEmail(email);
    // Try to update technical user's e-mail address
    try {
      techUserResource.update(userRepresentation);
    } catch (Exception e) {
      log.debug("E-Mail address already existing in Keycloak: {}", email);
      return false;
    }

    // Reset technical user
    userRepresentation.setEmail(originalEmail);
    techUserResource.update(userRepresentation);

    return true;
  }

  /**
   * Creates and returns the Keycloak CredentialRepresentation object
   * 
   * @param password
   * @return
   */
  private CredentialRepresentation getCredentialRepresentation(final String password) {
    CredentialRepresentation credentials = new CredentialRepresentation();
    credentials.setType(CredentialRepresentation.PASSWORD);
    credentials.setValue(password);
    credentials.setTemporary(false);

    return credentials;
  }

  /**
   * Creates and returns the Keycloak UserRepresentation object filled with the data from the
   * UserDTO
   * 
   * @param
   * @return
   */
  private UserRepresentation getUserRepresentation(final UserDTO user, final String firstName,
      final String lastName) {
    UserRepresentation kcUser = new UserRepresentation();
    kcUser.setUsername(user.getUsername());
    kcUser.setEmail(user.getEmail());
    kcUser.setEmailVerified(true);
    if (firstName != null) {
      kcUser.setFirstName(firstName);
    }
    if (lastName != null) {
      kcUser.setLastName(lastName);
    }
    kcUser.setEnabled(true);

    return kcUser;
  }

  /**
   * Returns the UserId of the recently created Keycloak user.
   * 
   * @param location The URI of the Keycloak API response
   * @return
   */
  private String getCreatedUserId(final URI location) {
    if (location != null) {
      String path = location.getPath();
      return path.substring(path.lastIndexOf('/') + 1);
    }

    return null;
  }

  /**
   * Assigns the role "user" to the given user Id.
   * 
   * @param userId
   * @return
   */
  @KeycloakAdminClientLogout
  public void updateUserRole(final String userId) throws Exception {
    updateRole(userId, KEYCLOAK_USER_ROLE);
  }

  /**
   * 
   * Assigns the role with the given name to the given user Id.
   * 
   * @param userId
   * @param roleName
   */
  @KeycloakAdminClientLogout
  public void updateRole(final String userId, final String roleName) {
    // Get realm and user resources
    RealmResource realmResource = getInstance().realm(KEYCLOAK_REALM);
    UsersResource userRessource = realmResource.users();
    UserResource user = userRessource.get(userId);
    boolean isRoleUpdated = false;

    // Assign role
    user.roles().realmLevel()
        .add(Arrays.asList(realmResource.roles().get(roleName).toRepresentation()));

    // Check if role has been assigned successfully
    List<RoleRepresentation> userRoles = user.roles().realmLevel().listAll();
    for (RoleRepresentation role : userRoles) {
      if (role.toString().toUpperCase().equals(roleName.toUpperCase())) {
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
   * @param userId
   * @param password
   */
  @KeycloakAdminClientLogout
  public void updatePassword(final String userId, final String password) throws Exception {
    CredentialRepresentation newCredentials = getCredentialRepresentation(password);
    UserResource userResource = getInstance().realm(KEYCLOAK_REALM).users().get(userId);

    userResource.resetPassword(newCredentials);
    log.debug("Updated user credentials for {}", userId);
  }

  /**
   * If user didn't provide an email, set to dummy address (userId@caritas-online-beratung.de). No
   * success/error status possible, because the Keycloak Client doesn't provide one either.
   * 
   * @param userId
   */
  @KeycloakAdminClientLogout
  public String updateDummyEmail(final String userId, UserDTO user) throws Exception {
    String dummyEmail = userHelper.getDummyEmail(userId);
    user.setEmail(dummyEmail);
    UserResource userResource = getInstance().realm(KEYCLOAK_REALM).users().get(userId);

    userResource.update(getUserRepresentation(user, null, null));
    log.debug("Set email dummy for {} to {}", userId, dummyEmail);

    return dummyEmail;
  }

  /**
   * Delete the user if something went wrong during the registration process
   */
  @KeycloakAdminClientLogout
  public void rollBackUser(String userId) {
    try {
      getInstance().realm(KEYCLOAK_REALM).users().get(userId).remove();
      log.debug("User {} has been removed due to rollback", userId);
    } catch (Exception e) {
      log.error("User could not be removed/rolled back: {}", userId);
    }
  }

  /**
   * Returns true if the given user has the provided authority.
   * 
   * @param userId
   * @param authority
   * @return
   */
  @KeycloakAdminClientLogout
  public boolean userHasAuthority(String userId, String authority) {

    List<RoleRepresentation> userRoles = null;

    try {
      userRoles = getUserRoles(userId);

    } catch (Exception ex) {
      String error = String.format("Could not get roles for user id %s", userId);
      logService.logKeycloakError(error, ex);
      throw new KeycloakException(error);
    }

    for (RoleRepresentation role : userRoles) {
      Optional<UserRole> userRoleOptional = UserRole.getRoleByValue(role.getName());
      if (userRoleOptional.isPresent()) {
        List<String> authorities = Authority.getAuthoritiesByUserRole(userRoleOptional.get());
        if (authorities.contains(authority)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Returns a list of {@link RoleRepresentation} containing all the assigned Keycloak roles for the
   * provided user id.
   * 
   * @param userId
   * @return
   */
  @KeycloakAdminClientLogout
  private List<RoleRepresentation> getUserRoles(String userId) {
    return getInstance().realm(KEYCLOAK_REALM).users().get(userId).roles().realmLevel().listAll();
  }

  /**
   * Returns a list of {@link UserRepresentation} containing all users that match the given search
   * string.
   * 
   * @param username
   * @return
   */
  @KeycloakAdminClientLogout
  public List<UserRepresentation> findByUsername(String username) {
    return getInstance().realm(KEYCLOAK_REALM).users().search(username);
  }

  public void closeSession(String sessionId) {
    getInstance().realm(KEYCLOAK_REALM).deleteSession(sessionId);
  }

  /**
   * Closes the Keycloak Admin CLI instance
   */
  public void closeInstance() {
    /**
     * The Keycloak.close() method does actually only close the connection and does NOT delete the
     * session at the moment. There is already an issue for this. Will be implemented in a "future"
     * version: https://issues.jboss.org/browse/KEYCLOAK-7895
     * 
     * TODO
     * 
     * -> Thus this close() functionality is commented out (to only maintain one open session at
     * once).
     * 
     */
    // if (this.keycloakInstance != null && !this.keycloakInstance.isClosed()) {
    // this.keycloakInstance.close();
    // this.keycloakInstance = null;
    // }
  }
}
