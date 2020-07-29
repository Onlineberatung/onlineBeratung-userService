package de.caritas.cob.UserService.api.facade;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.container.RocketChatCredentials;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.helper.AgencyHelper;
import de.caritas.cob.UserService.api.helper.UserHelper;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.UserService.api.model.CreateUserResponseDTO;
import de.caritas.cob.UserService.api.model.UserDTO;
import de.caritas.cob.UserService.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.UserService.api.model.rocketChat.login.LoginResponseDTO;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.repository.userAgency.UserAgency;
import de.caritas.cob.UserService.api.service.RocketChatService;
import de.caritas.cob.UserService.api.service.SessionDataService;
import de.caritas.cob.UserService.api.service.SessionService;
import de.caritas.cob.UserService.api.service.UserAgencyService;
import de.caritas.cob.UserService.api.service.UserService;
import de.caritas.cob.UserService.api.service.helper.KeycloakAdminClientHelper;

/**
 * Facade to encapsulate the steps to initialize an user account (create chat/agency relation or a
 * new session).
 *
 */
@Service
public class CreateUserFacade {

  private final int USERNAME_NOT_AVAILABLE = 0;
  private final int EMAIL_AVAILABLE = 1;

  private final KeycloakAdminClientHelper keycloakAdminClientHelper;
  private final UserService userService;
  private final RocketChatService rocketChatService;
  private final UserAgencyService userAgencyService;
  private final SessionService sessionService;
  private final SessionDataService sessionDataService;
  private final ConsultingTypeManager consultingTypeManager;
  private final UserHelper userHelper;
  private final AgencyHelper agencyHelper;

  @Autowired
  public CreateUserFacade(KeycloakAdminClientHelper keycloakAdminClientHelper,
      UserService userService, RocketChatService rocketChatService,
      UserAgencyService userAgencyService, SessionService sessionService,
      SessionDataService sessionDataService, ConsultingTypeManager consultingTypeManager,
      UserHelper userHelper, AgencyHelper agencyHelper) {
    this.keycloakAdminClientHelper = keycloakAdminClientHelper;
    this.userService = userService;
    this.rocketChatService = rocketChatService;
    this.userAgencyService = userAgencyService;
    this.sessionService = sessionService;
    this.sessionDataService = sessionDataService;
    this.consultingTypeManager = consultingTypeManager;
    this.userHelper = userHelper;
    this.agencyHelper = agencyHelper;
  }

  /**
   * Creates a user in Keycloak and MariaDB. Then creates a session or chat account depending on the
   * provided {@link ConsultingType}.
   * 
   * @param user {@link UserDTO}
   * @return {@link KeycloakCreateUserResponseDTO}
   * 
   * @throws {@link ServiceException}
   */
  public KeycloakCreateUserResponseDTO createUserAndInitializeAccount(final UserDTO user) {

    KeycloakCreateUserResponseDTO response;
    String userId = null;

    if (!userHelper.isUsernameAvailable(user.getUsername())) {
      return new KeycloakCreateUserResponseDTO(HttpStatus.CONFLICT,
          new CreateUserResponseDTO(USERNAME_NOT_AVAILABLE, EMAIL_AVAILABLE), null);
    }

    ConsultingType consultingType =
        ConsultingType.values()[Integer.valueOf(user.getConsultingType())];

    if (!agencyHelper.doesConsultingTypeMatchToAgency(user.getAgencyId(), consultingType)) {
      return new KeycloakCreateUserResponseDTO(HttpStatus.BAD_REQUEST);
    }

    try {
      // Create the user in Keycloak
      response = keycloakAdminClientHelper.createKeycloakUser(user);
      userId = response.getUserId();

    } catch (Exception ex) {
      throw new ServiceException(
          String.format("Could not create Keycloak account for: %s", user.toString()), ex);
    }

    if (response.getStatus().equals(HttpStatus.CONFLICT)) {
      return response;
    }

    // Update Keycloak account data and create user and session in MariaDB
    updateAccountData(userId, user, consultingType);

    return new KeycloakCreateUserResponseDTO(HttpStatus.CREATED);
  }

  /**
   * Update the Keycloak account data (roles, password, e-mail address), create the user in MariaDB
   * and initialize a session or chat relation (depending on {@link ConsultingType}).
   * 
   * @param userId Keycloak user ID
   * @param user {@link UserDTO} from registration form
   * @param consultingType {@link ConsultingType}
   */
  private void updateAccountData(String userId, UserDTO user, ConsultingType consultingType) {

    if (userId == null) {
      throw new ServiceException(
          String.format("Could not create Keycloak account for: %s", user.toString()));
    }

    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultantTypeSettings(consultingType);
    String dummyEmail = null;
    User dbUser = null;

    try {
      // We need to set the user roles and password and (dummy) e-mail address after the user was
      // created in Keycloak
      keycloakAdminClientHelper.updateUserRole(userId);
      keycloakAdminClientHelper.updatePassword(userId, user.getPassword());

      if (user.getEmail() == null || user.getEmail().isEmpty()) {
        dummyEmail = keycloakAdminClientHelper.updateDummyEmail(userId, user);
      }

      String userEmailAddress = (user.getEmail() != null) ? user.getEmail() : dummyEmail;
      dbUser = userService.createUser(userId, user.getUsername(), userEmailAddress,
          consultingTypeSettings.isLanguageFormal());

    } catch (Exception ex) {
      rollBackUserAccount(userId, dbUser, null, null);
      throw new ServiceException(
          String.format("Could not update account data on registration for: %s", user.toString()),
          ex);
    }

    initializeUserAccount(user, dbUser, consultingTypeSettings);
  }

  /**
   * Initializes the provided {@link User} account depending on the consulting type. Consulting type
   * KREUZBUND will get a chat/agency relation, all others will be provided with a session.
   * 
   * @param user {@link UserDTO}
   * @param dbUser {@link User}
   * @param consultingTypeSettings {@link ConsultingTypeSettings}
   */
  private void initializeUserAccount(UserDTO user, User dbUser,
      ConsultingTypeSettings consultingTypeSettings) {

    if (consultingTypeSettings.getConsultingType().equals(ConsultingType.KREUZBUND)) {
      createUserChatAgencyRelation(user, dbUser, consultingTypeSettings);

    } else {
      createUserSession(user, dbUser, consultingTypeSettings);

    }

  }

  /**
   * Creates a new session for the provided {@link User}.
   * 
   * @param user {@link UserDTO}
   * @param dbUser {@link User}
   * @param consultingTypeSettings {@link ConsultingTypeSettings}
   * 
   * @throws {@link ServiceException}
   */
  private void createUserSession(UserDTO user, User dbUser,
      ConsultingTypeSettings consultingTypeSettings) {
    Session session = null;

    try {
      session =
          sessionService.initializeSession(dbUser, user, consultingTypeSettings.isMonitoring());

      // Save session data
      sessionDataService.saveSessionDataFromRegistration(session, user);

    } catch (Exception ex) {
      rollBackUserAccount(dbUser.getUserId(), dbUser, session, null);
      throw new ServiceException(ex);
    }
  }

  /**
   * Creates a new chat/agency relation for the provided {@link User}.
   * 
   * @param user {@link UserDTO}
   * @param dbUser {@link User}
   * @param consultingTypeSettings {@link ConsultingTypeSettings}
   * 
   * @throws {@link ServiceException}
   */
  private void createUserChatAgencyRelation(UserDTO user, User dbUser,
      ConsultingTypeSettings consultingTypeSettings) {

    // Log in user to Rocket.Chat
    ResponseEntity<LoginResponseDTO> rcUserResponse = rocketChatService
        .loginUserFirstTime(userHelper.encodeUsername(user.getUsername()), user.getPassword());

    if (!rcUserResponse.getStatusCode().equals(HttpStatus.OK) || rcUserResponse.getBody() == null) {
      rollBackUserAccount(dbUser.getUserId(), dbUser, null, null);
      throw new ServiceException(String.format(
          "Rocket.Chat login for Kreuzbund registration was not successful for user %s.",
          user.getUsername()));
    }

    String rcUserToken = rcUserResponse.getBody().getData().getAuthToken();
    String rcUserId = rcUserResponse.getBody().getData().getUserId();
    if (rcUserToken == null || rcUserToken.equals(StringUtils.EMPTY) || rcUserId == null
        || rcUserId.equals(StringUtils.EMPTY)) {
      rollBackUserAccount(dbUser.getUserId(), dbUser, null, null);
      throw new ServiceException(String.format(
          "Rocket.Chat login for Kreuzbund registration was not successful for user %s.",
          user.getUsername()));
    }

    // Log out user from Rocket.Chat
    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .RocketChatUserId(rcUserId).RocketChatToken(rcUserToken).build();
    rocketChatService.logoutUser(rocketChatCredentials);

    // Update rcUserId in user table
    dbUser.setRcUserId(rcUserId);
    User updatedUser = userService.saveUser(dbUser);
    if (updatedUser.getRcUserId() == null || updatedUser.getRcUserId().equals(StringUtils.EMPTY)) {
      rollBackUserAccount(dbUser.getUserId(), dbUser, null, null);
      throw new ServiceException(
          String.format("Could not update Rocket.Chat user id for user %s", user.getUsername()));
    }

    // Create user-agency-relation
    UserAgency userAgency = new UserAgency(dbUser, user.getAgencyId());
    try {
      userAgencyService.saveUserAgency(userAgency);

    } catch (ServiceException serviceException) {
      rollBackUserAccount(dbUser.getUserId(), dbUser, null, userAgency);
      throw new ServiceException("Could not create user-agency relation for Kreuzbund registration",
          serviceException);
    }
  }

  /**
   * Deletes the provided user in Keycloak and MariaDB and its related session or user <->
   * chat/agency relations.
   * 
   * @param userId Keycloak user ID
   * @param session {@link Session}
   * @param dbUser {@link User}
   */
  private void rollBackUserAccount(String userId, User dbUser, Session session,
      UserAgency userAgency) {

    keycloakAdminClientHelper.rollBackUser(userId);
    if (userAgency != null) {
      userAgencyService.deleteUserAgency(userAgency);
    }
    if (session != null) {
      sessionService.deleteSession(session);
    }
    if (dbUser != null) {
      userService.deleteUser(dbUser);
    }
  }

}
