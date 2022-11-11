package de.caritas.cob.userservice.api.conversation.service.user.anonymous;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.LoginResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.conversation.model.AnonymousUserCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.facade.CreateUserFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackFacade;
import de.caritas.cob.userservice.api.facade.rollback.RollbackUserAccountInformation;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/** Service to create anonymous user accounts. */
@Service
@RequiredArgsConstructor
public class AnonymousUserCreatorService {

  private final @NonNull CreateUserFacade createUserFacade;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull RollbackFacade rollbackFacade;
  private final @NonNull UserService userService;

  /**
   * Creates an anonymous user account in Keycloak, MariaDB and Rocket.Chat.
   *
   * @param userDto {@link UserDTO}
   * @return {@link AnonymousUserCredentials}
   */
  public AnonymousUserCredentials createAnonymousUser(UserDTO userDto) {

    KeycloakCreateUserResponseDTO response = identityClient.createKeycloakUser(userDto);
    createUserFacade.updateIdentityAndCreateAccount(
        response.getUserId(), userDto, UserRole.ANONYMOUS);

    KeycloakLoginResponseDTO kcLoginResponseDTO;
    ResponseEntity<LoginResponseDTO> rcLoginResponseDto;
    try {
      kcLoginResponseDTO = identityClient.loginUser(userDto.getUsername(), userDto.getPassword());
      rcLoginResponseDto =
          rocketChatService.loginUserFirstTime(userDto.getUsername(), userDto.getPassword());
    } catch (RocketChatLoginException | BadRequestException e) {
      rollBackAnonymousUserAccount(response.getUserId());
      throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
    }

    var anonymousUserCredentials =
        AnonymousUserCredentials.builder()
            .userId(response.getUserId())
            .accessToken(kcLoginResponseDTO.getAccessToken())
            .expiresIn(kcLoginResponseDTO.getExpiresIn())
            .refreshToken(kcLoginResponseDTO.getRefreshToken())
            .refreshExpiresIn(kcLoginResponseDTO.getRefreshExpiresIn())
            .rocketChatCredentials(obtainRocketChatCredentials(rcLoginResponseDto))
            .build();

    updateRocketChatUserIdInDatabase(anonymousUserCredentials);

    return anonymousUserCredentials;
  }

  private void rollBackAnonymousUserAccount(String userId) {
    rollbackFacade.rollBackUserAccount(
        RollbackUserAccountInformation.builder().userId(userId).rollBackUserAccount(true).build());
  }

  private RocketChatCredentials obtainRocketChatCredentials(
      ResponseEntity<LoginResponseDTO> response) {
    return RocketChatCredentials.builder()
        .rocketChatUserId(response.getBody().getData().getUserId())
        .rocketChatToken(response.getBody().getData().getAuthToken())
        .build();
  }

  private void updateRocketChatUserIdInDatabase(AnonymousUserCredentials anonymousUserCredentials) {
    var user =
        userService
            .getUser(anonymousUserCredentials.getUserId())
            .orElseThrow(
                () ->
                    new InternalServerErrorException(
                        String.format(
                            "Could not get user %s to update the rocket chat user id.",
                            anonymousUserCredentials.getUserId())));
    userService.updateRocketChatIdInDatabase(
        user, anonymousUserCredentials.getRocketChatCredentials().getRocketChatUserId());
  }
}
