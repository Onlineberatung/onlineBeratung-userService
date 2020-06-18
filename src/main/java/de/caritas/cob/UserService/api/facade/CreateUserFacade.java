package de.caritas.cob.UserService.api.facade;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.exception.AgencyServiceHelperException;
import de.caritas.cob.UserService.api.exception.KeycloakException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.responses.BadRequestException;
import de.caritas.cob.UserService.api.helper.UserHelper;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.UserService.api.model.AgencyDTO;
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
import de.caritas.cob.UserService.api.service.helper.AgencyServiceHelper;
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
  private final AgencyServiceHelper agencyServiceHelper;

  @Autowired
  public CreateUserFacade(KeycloakAdminClientHelper keycloakAdminClientHelper,
      UserService userService, RocketChatService rocketChatService,
      UserAgencyService userAgencyService, SessionService sessionService,
      SessionDataService sessionDataService, ConsultingTypeManager consultingTypeManager,
      UserHelper userHelper, AgencyServiceHelper agencyServiceHelper) {
    this.keycloakAdminClientHelper = keycloakAdminClientHelper;
    this.userService = userService;
    this.rocketChatService = rocketChatService;
    this.userAgencyService = userAgencyService;
    this.sessionService = sessionService;
    this.sessionDataService = sessionDataService;
    this.consultingTypeManager = consultingTypeManager;
    this.userHelper = userHelper;
    this.agencyServiceHelper = agencyServiceHelper;
  }

  /**
   * Creates a user in Keycloak and MariaDB. Then creates a session or chat account depending on the
   * provided {@link ConsultingType}
   * 
   * @param user {@link UserDTO}
   * @return
   * 
   * @throws {@link ServiceException}
   */
  public KeycloakCreateUserResponseDTO createUserAndInitializeAccount(final UserDTO user) {

    ConsultingType consultingType =
        ConsultingType.values()[Integer.valueOf(user.getConsultingType())];
    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultantTypeSettings(consultingType);
    KeycloakCreateUserResponseDTO response;
    String userId = null;
    User dbUser = null;

    // Check if non-encrypted username already exists in Keycloak (the encrypted username is being
    // checked while creating the user in Keycloak)
    if (!userHelper.isUsernameAvailable(user.getUsername())) {
      return new KeycloakCreateUserResponseDTO(HttpStatus.CONFLICT,
          new CreateUserResponseDTO(USERNAME_NOT_AVAILABLE, EMAIL_AVAILABLE), null);
    }

    // Check if agency has correct (provided) consulting type
    AgencyDTO agencyDTO = null;
    try {
      agencyDTO = agencyServiceHelper.getAgencyWithoutCaching(user.getAgencyId());
    } catch (AgencyServiceHelperException agencyServiceHelperException) {
      throw new ServiceException(String
          .format("Could not get agency with id %s for Kreuzbund registration", user.getAgencyId()),
          agencyServiceHelperException);
    }
    if (agencyDTO == null) {
      throw new ServiceException(String.format(
          "Could not get agency with id %s for Kreuzbund registration", user.getAgencyId()));
    }
    if (!agencyDTO.getConsultingType().equals(consultingType)) {
      throw new BadRequestException(String.format(
          "The provided agency with id %s is not assigned to the provided consulting type %s",
          user.getAgencyId(), user.getConsultingType()));
    }

    try {
      // Create the user in Keycloak
      response = keycloakAdminClientHelper.createKeycloakUser(user);
      userId = response.getUserId();

    } catch (KeycloakException keycloakEx) {
      throw new ServiceException(keycloakEx);
    } catch (Exception ex) {
      throw new ServiceException(ex);
    }

    if (response.getStatus().equals(HttpStatus.CONFLICT)) {
      return response;
    }

    if (userId != null) {
      String dummyEmail = null;

      try {
        // We need to set the user roles after the user was created in Keycloak
        keycloakAdminClientHelper.updateUserRole(userId);

        // The password also needs to be set after the user was created
        keycloakAdminClientHelper.updatePassword(userId, user.getPassword());

        // Set a dummy e-mail address if user didn't provide one
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
          dummyEmail = keycloakAdminClientHelper.updateDummyEmail(userId, user);
        }

        // Create user in mariaDB
        String userEmailAddress = (user.getEmail() != null) ? user.getEmail() : dummyEmail;
        dbUser = userService.createUser(userId, user.getUsername(), userEmailAddress,
            consultingTypeSettings.isLanguageFormal());

      } catch (Exception ex) {
        rollBackUserAccount(userId, dbUser, null, null);
        throw new ServiceException(ex);
      }

      initializeUserAccount(user, dbUser, consultingTypeSettings);

    } else {
      throw new ServiceException("Could not create Keycloak user");
    }

    return new KeycloakCreateUserResponseDTO(HttpStatus.CREATED);
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
   * Creates a new session for the provided {@link User}
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
   * Creates a new chat/agency relation for the provided {@link User}
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
    rocketChatService.logoutUser(rcUserId, rcUserToken);

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
      throw new ServiceException("Could not create user-agency relationfor Kreuzbund registration",
          serviceException);
    }
  }


  /**
   * Deletes the provided user in Keycloak and MariaDB and its related session or user <->
   * chat/agency relations.
   * 
   * @param userId
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
