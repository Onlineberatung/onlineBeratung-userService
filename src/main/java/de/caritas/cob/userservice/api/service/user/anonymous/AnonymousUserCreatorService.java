package de.caritas.cob.userservice.api.service.user.anonymous;

import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.facade.CreateUserFacade;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryResponseDTO;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.KeycloakService;
import de.caritas.cob.userservice.api.service.conversation.anonymous.AnonymousConversationCreatorService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/** TODO */
@Service
@RequiredArgsConstructor
public class AnonymousUserCreatorService {

  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull AnonymousUsernameRegistry usernameRegistry;
  private final @NonNull CreateUserFacade createUserFacade;
  private final @NonNull KeycloakService keycloakService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull AnonymousConversationCreatorService anonymousConversationCreatorService;

  /**
   * TODO.
   *
   * @param createAnonymousEnquiryDTO
   * @return
   */
  public CreateAnonymousEnquiryResponseDTO createAnonymousUser(
      CreateAnonymousEnquiryDTO createAnonymousEnquiryDTO) {

    var userDto = buildUserDto(createAnonymousEnquiryDTO);
    KeycloakCreateUserResponseDTO response = keycloakAdminClientService.createKeycloakUser(userDto);
    createUserFacade.updateKeycloakAccountAndCreateDatabaseUserAccount(response.getUserId(),
        userDto, UserRole.ANONYMOUS);

    var loginResponseDTO = keycloakService.loginUser(userDto.getUsername(), userDto.getPassword());

    ResponseEntity<de.caritas.cob.userservice.api.model.rocketchat.login.LoginResponseDTO>
        rcLoginResponseDto = null;
    try {
      rcLoginResponseDto =
          rocketChatService.loginUserFirstTime(userDto.getUsername(), "Testtest!12");
    } catch (RocketChatLoginException e) {
      throw new InternalServerErrorException("Could not Login in Rocket.Chat");
    }

    // TODO rausziehen/refactoren
    Session session =
        anonymousConversationCreatorService.createAnonymousConversation(
            response.getUserId(), userDto, RocketChatCredentials.builder()
                .rocketChatUserId(rcLoginResponseDto.getBody().getData().getUserId())
                .rocketChatToken(rcLoginResponseDto.getBody().getData().getAuthToken())
            .build(),
            ConsultingType.values()[createAnonymousEnquiryDTO.getConsultingType()]);

    return new CreateAnonymousEnquiryResponseDTO()
        .userName(userDto.getUsername())
        .accessToken(loginResponseDTO.getAccess_token())
        .refreshToken(loginResponseDTO.getRefresh_token())
        .rcUserId(rcLoginResponseDto.getBody().getData().getUserId())
        .rcToken(rcLoginResponseDto.getBody().getData().getAuthToken())
        .rcGroupId(session.getGroupId())
        .sessionId(session.getId());
  }

  private UserDTO buildUserDto(CreateAnonymousEnquiryDTO createAnonymousEnquiryDTO) {
    return UserDTO.builder()
        .consultingType(String.valueOf(createAnonymousEnquiryDTO.getConsultingType()))
        .username(usernameRegistry.generateUniqueUsername())
        .password("Testtest!12") // TODO
        .postcode("00000")
        .termsAccepted("true")
        .build();
  }
}
