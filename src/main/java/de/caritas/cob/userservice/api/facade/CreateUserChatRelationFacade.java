package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.isNull;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.DataDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.useragency.UserAgency;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.UserAgencyService;
import de.caritas.cob.userservice.api.service.user.UserService;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps to initialize a chat/agency relation.
 */
@Service
@RequiredArgsConstructor
public class CreateUserChatRelationFacade {

  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull UserHelper userHelper;
  private final @NonNull UserService userService;
  private final @NonNull UserAgencyService userAgencyService;
  private final @NonNull RollbackFacade rollbackFacade;

  /**
   * Creates an user-chat/agency relation for the provided {@link User}. Either provide username and
   * password in {@link UserDTO} (new user account registrations) or valid {@link
   * RocketChatCredentials} (for new consulting type registrations).
   *
   * @param userDTO               {@link UserDTO}
   * @param user                  {@link User}
   * @param rocketChatCredentials {@link RocketChatCredentials}
   */
  public void initializeUserChatAgencyRelation(UserDTO userDTO, User user,
      RocketChatCredentials rocketChatCredentials) {

    DataDTO rcUserCredentials = obtainValidUserCredentials(userDTO, user, rocketChatCredentials);
    updateRocketChatUserIdInDatabase(user, rcUserCredentials.getUserId(),
        Boolean.parseBoolean(userDTO.getTermsAccepted()));
    createUserChatAgencyRelation(userDTO, user);
  }

  private DataDTO obtainValidUserCredentials(UserDTO userDTO, User user,
      RocketChatCredentials rocketChatCredentials) {

    if (isNewUserAccountRegistration(user, rocketChatCredentials)) {
      return validateUserCredentials(obtainRocketChatloginData(userDTO, user), user,
          rocketChatCredentials, Boolean.parseBoolean(userDTO.getTermsAccepted()));
    }
    return new DataDTO(rocketChatCredentials.getRocketChatUserId(),
        rocketChatCredentials.getRocketChatToken(), null);
  }

  private boolean isNewUserAccountRegistration(User user,
      RocketChatCredentials rocketChatCredentials) {
    return (isNull(rocketChatCredentials) || isNull(rocketChatCredentials.getRocketChatUserId()))
        && isNull(user.getRcUserId());
  }

  private DataDTO obtainRocketChatloginData(UserDTO userDTO, User user) {
    try {
      ResponseEntity<LoginResponseDTO> rcUserResponse = rocketChatService
          .loginUserFirstTime(userHelper.encodeUsername(userDTO.getUsername()),
              userDTO.getPassword());

      checkIfRocketLoginSucceeded(userDTO, user, rcUserResponse);

      return rcUserResponse.getBody().getData();
    } catch (RocketChatLoginException exception) {
      rollBackAndLogRocketChatLoginError(user, exception,
          Boolean.parseBoolean(userDTO.getTermsAccepted()));
    }
    return null;
  }

  private void checkIfRocketLoginSucceeded(UserDTO userDTO, User user,
      ResponseEntity<LoginResponseDTO> rcUserResponse) {
    if (isNull(rcUserResponse) || !rcUserResponse.getStatusCode().equals(HttpStatus.OK) || isNull(
        rcUserResponse.getBody())) {
      rollBackAndLogRocketChatLoginError(user, Boolean.parseBoolean(userDTO.getTermsAccepted()));
    }
  }

  private DataDTO validateUserCredentials(DataDTO dataDTO, User user,
      RocketChatCredentials rocketChatCredentials, boolean deleteUserOnRollback) {
    String rcUserToken = obtainValidRcToken(dataDTO, rocketChatCredentials);
    String rcUserId = obtainValidRcUserId(dataDTO, rocketChatCredentials);

    if (isBlank(rcUserToken) || isBlank(rcUserId)) {
      rollBackAndLogRocketChatLoginError(user, deleteUserOnRollback);
    }

    dataDTO.setUserId(rcUserId);
    dataDTO.setAuthToken(rcUserToken);
    return dataDTO;
  }

  private String obtainValidRcToken(DataDTO dataDTO, RocketChatCredentials rocketChatCredentials) {
    return isNull(rocketChatCredentials) || isNull(rocketChatCredentials.getRocketChatToken())
        ? dataDTO.getAuthToken()
        : rocketChatCredentials.getRocketChatToken();
  }

  private String obtainValidRcUserId(DataDTO dataDTO, RocketChatCredentials rocketChatCredentials) {
    return isNull(rocketChatCredentials) || isNull(rocketChatCredentials.getRocketChatUserId())
        ? dataDTO.getUserId()
        : rocketChatCredentials.getRocketChatUserId();
  }

  private void updateRocketChatUserIdInDatabase(User user, String rcUserId,
      boolean deleteUserOnRollback) {
    if (isNotBlank(user.getRcUserId())) {
      return;
    }

    user.setRcUserId(rcUserId);
    try {
      User updatedUser = userService.saveUser(user);
      checkUpdatedUserId(user, updatedUser, deleteUserOnRollback);
    } catch (Exception e) {
      rollBackAndLogMariaDbError(user, e, deleteUserOnRollback);
    }

  }

  private void checkUpdatedUserId(User user, User updatedUser, boolean deleteUserOnRollback) {
    if (isBlank(updatedUser.getRcUserId())) {
      rollBackAndLogMariaDbError(user,
          new IllegalArgumentException("Rocket.Chat user ID is empty."), deleteUserOnRollback);
    }
  }

  private void createUserChatAgencyRelation(UserDTO userDTO, User user) {
    UserAgency userAgency = new UserAgency(user, userDTO.getAgencyId());
    checkIfAlreadyAssignedToAgency(user, userAgency);

    try {
      userAgencyService.saveUserAgency(userAgency);

    } catch (InternalServerErrorException serviceException) {
      rollbackFacade.rollBackUserAccount(RollbackUserAccountInformation.builder()
          .userId(user.getUserId())
          .user(user)
          .userAgency(userAgency)
          .rollBackUserAccount(Boolean.parseBoolean(userDTO.getTermsAccepted()))
          .build());
      throw new InternalServerErrorException(
          "Could not create user-agency relation for group chat registration",
          LogService::logDatabaseError);
    }
  }

  private void checkIfAlreadyAssignedToAgency(User user, UserAgency userAgency) {
    List<UserAgency> userAgencies = userAgencyService.getUserAgenciesByUser(user);

    if (userAgencies.stream()
        .anyMatch(agency -> agency.getAgencyId().equals(userAgency.getAgencyId()))) {
      throw new BadRequestException(String
          .format("User %s already assigned to chat relation agency %s", user.getUserId(),
              userAgency.getAgencyId()));
    }
  }

  private void rollBackAndLogRocketChatLoginError(User user, boolean deleteUser) {
    rollBackAndLogRocketChatLoginError(user,
        new RocketChatLoginException("Rocket.Chat login error."), deleteUser);
  }

  private void rollBackAndLogRocketChatLoginError(User user,
      RocketChatLoginException exception, boolean deleteUser) {
    rollBackUserAccount(user, deleteUser);
    throw new InternalServerErrorException(String.format(
        "Rocket.Chat login for Group Chat registration was not successful for user %s. %s",
        user.getUsername(), isBlank(exception.getMessage()) ? "" : exception.getMessage()),
        LogService::logRocketChatError);
  }

  private void rollBackAndLogMariaDbError(User user,
      Exception exception, boolean deleteUser) {
    rollBackUserAccount(user, deleteUser);
    throw new InternalServerErrorException(String.format(
        "Could not update Rocket.Chat user ID for user %s. %s",
        user.getUsername(), exception.getMessage()), LogService::logDatabaseError);
  }

  private void rollBackUserAccount(User user, boolean deleteUser) {
    rollbackFacade.rollBackUserAccount(RollbackUserAccountInformation.builder()
        .userId(user.getUserId())
        .user(user)
        .rollBackUserAccount(deleteUser)
        .build());
  }
}
