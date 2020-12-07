package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.isNull;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.SaveUserException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.DataDTO;
import de.caritas.cob.userservice.api.model.rocketchat.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.userAgency.UserAgency;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.UserAgencyService;
import de.caritas.cob.userservice.api.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateUserChatRelationFacade {

  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull UserHelper userHelper;
  private final @NonNull UserService userService;
  private final @NonNull UserAgencyService userAgencyService;
  private final @NonNull RollbackFacade rollbackFacade;

  public void initializeUserChatAgencyRelation(UserDTO userDTO, User user,
      RocketChatCredentials rocketChatCredentials) {

    DataDTO rcUserCredentials = obtainValidUserCredentials(userDTO, user, rocketChatCredentials);
    updateRocketChatUserIdInDatabase(user, rcUserCredentials.getUserId(),
        Boolean.parseBoolean(userDTO.getTermsAccepted()));
    createUserChatAgencyRelation(userDTO, user);
  }

  private DataDTO obtainValidUserCredentials(UserDTO userDTO, User user,
      RocketChatCredentials rocketChatCredentials) {

    if (isNull(rocketChatCredentials.getRocketChatUserId()) && isNull(user.getRcUserId())) {
      return validateUserCredentials(obtainRocketChatloginData(userDTO, user), user,
          rocketChatCredentials, Boolean.parseBoolean(userDTO.getTermsAccepted()));
    }
    return new DataDTO(rocketChatCredentials.getRocketChatUserId(),
        rocketChatCredentials.getRocketChatToken(), null);
  }

  private DataDTO obtainRocketChatloginData(UserDTO userDTO, User user) {
    ResponseEntity<LoginResponseDTO> rcUserResponse = null;

    try {
      rcUserResponse = rocketChatService
          .loginUserFirstTime(userHelper.encodeUsername(userDTO.getUsername()),
              userDTO.getPassword());
    } catch (RocketChatLoginException exception) {
      rollBackAndLogRocketChatLoginError(user, exception,
          Boolean.parseBoolean(userDTO.getTermsAccepted()));
    }

    if (!rcUserResponse.getStatusCode().equals(HttpStatus.OK) || isNull(rcUserResponse.getBody())) {
      rollBackAndLogRocketChatLoginError(user, Boolean.parseBoolean(userDTO.getTermsAccepted()));
    }

    return rcUserResponse.getBody().getData();
  }

  private DataDTO validateUserCredentials(DataDTO dataDTO, User user,
      RocketChatCredentials rocketChatCredentials, boolean deleteUserOnRollback) {
    String rcUserToken =
        isNull(rocketChatCredentials.getRocketChatToken()) ? dataDTO
            .getAuthToken() : rocketChatCredentials.getRocketChatToken();
    String rcUserId =
        isNull(rocketChatCredentials.getRocketChatUserId()) ? dataDTO
            .getUserId()
            : rocketChatCredentials.getRocketChatUserId();

    if (isBlank(rcUserToken) || isBlank(rcUserId)) {
      rollBackAndLogRocketChatLoginError(user, deleteUserOnRollback);
    }

    dataDTO.setUserId(rcUserId);
    dataDTO.setAuthToken(rcUserToken);
    return dataDTO;
  }

  private void updateRocketChatUserIdInDatabase(User user, String rcUserId,
      boolean deleteUserOnRollback) {
    if (isNotBlank(user.getRcUserId())) {
      return;
    }

    User updatedUser = null;
    user.setRcUserId(rcUserId);
    try {
      updatedUser = userService.saveUser(user);
    } catch (SaveUserException e) {
      rollBackAndLogMariaDbError(user, e, deleteUserOnRollback);
    }

    if (isBlank(updatedUser.getRcUserId())) {
      rollBackAndLogMariaDbError(user, null, deleteUserOnRollback);
    }
  }

  private void createUserChatAgencyRelation(UserDTO userDTO, User user) {
    UserAgency userAgency = new UserAgency(user, userDTO.getAgencyId());
    try {
      userAgencyService.saveUserAgency(userAgency);

    } catch (InternalServerErrorException serviceException) {
      rollbackFacade.rollBackUserAccount(RollbackUserAccountInformation.builder()
          .userId(user.getUserId())
          .user(user)
          .userAgency(userAgency)
          .build());
      throw new InternalServerErrorException(
          "Could not create user-agency relation for Kreuzbund registration",
          LogService::logDatabaseError);
    }
  }

  private void rollBackAndLogRocketChatLoginError(User user, boolean deleteUser) {
    rollBackAndLogRocketChatLoginError(user, null, deleteUser);
  }

  private void rollBackAndLogRocketChatLoginError(User user,
      RocketChatLoginException exception, boolean deleteUser) {
    rollBackUserAccount(user, deleteUser);
    throw new InternalServerErrorException(String.format(
        "Rocket.Chat login for Kreuzbund registration was not successful for user %s. %s",
        user.getUsername(), isBlank(exception.getMessage()) ? "" : exception.getMessage()),
        LogService::logRocketChatError);
  }

  private void rollBackAndLogMariaDbError(User user,
      SaveUserException exception, boolean deleteUser) {
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
