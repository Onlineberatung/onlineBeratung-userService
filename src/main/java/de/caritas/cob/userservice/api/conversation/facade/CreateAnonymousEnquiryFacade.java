package de.caritas.cob.userservice.api.conversation.facade;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import de.caritas.cob.userservice.api.adapters.web.dto.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAnonymousEnquiryResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.conversation.model.AnonymousUserCredentials;
import de.caritas.cob.userservice.api.conversation.service.AnonymousConversationCreatorService;
import de.caritas.cob.userservice.api.conversation.service.user.anonymous.AnonymousUserCreatorService;
import de.caritas.cob.userservice.api.conversation.service.user.anonymous.AnonymousUsernameRegistry;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade to encapsulate the steps of creating a new anonymous conversation. */
@Service
@RequiredArgsConstructor
public class CreateAnonymousEnquiryFacade {

  private final @NonNull AnonymousUserCreatorService anonymousUserCreatorService;
  private final @NonNull AnonymousConversationCreatorService anonymousConversationCreatorService;
  private final @NonNull AnonymousUsernameRegistry usernameRegistry;
  private final @NonNull UserHelper userHelper;
  private final @NonNull ConsultingTypeManager consultingTypeManager;

  private static final String DEFAULT_ANONYMOUS_POSTCODE = "00000";

  /**
   * Creates an anonymous user account and its corresponding anonymous conversation resp. session
   * and returns all needed user credentials.
   *
   * @param createAnonymousEnquiryDTO {@link CreateAnonymousEnquiryDTO}
   * @return {@link CreateAnonymousEnquiryResponseDTO}
   */
  public CreateAnonymousEnquiryResponseDTO createAnonymousEnquiry(
      CreateAnonymousEnquiryDTO createAnonymousEnquiryDTO) {

    checkIfConsultingTypeHasAnonymousConsulting(createAnonymousEnquiryDTO.getConsultingType());

    var userDto = buildUserDto(createAnonymousEnquiryDTO);
    AnonymousUserCredentials credentials = anonymousUserCreatorService.createAnonymousUser(userDto);
    var session =
        anonymousConversationCreatorService.createAnonymousConversation(userDto, credentials);

    return new CreateAnonymousEnquiryResponseDTO()
        .userName(userDto.getUsername())
        .accessToken(credentials.getAccessToken())
        .refreshToken(credentials.getRefreshToken())
        .expiresIn(credentials.getExpiresIn())
        .refreshExpiresIn(credentials.getRefreshExpiresIn())
        .rcUserId(credentials.getRocketChatCredentials().getRocketChatUserId())
        .rcToken(credentials.getRocketChatCredentials().getRocketChatToken())
        .rcGroupId(session.getGroupId())
        .sessionId(session.getId());
  }

  private void checkIfConsultingTypeHasAnonymousConsulting(int consultingTypeId) {
    var consultingTypeSettings = consultingTypeManager.getConsultingTypeSettings(consultingTypeId);

    if (isFalse(consultingTypeSettings.getIsAnonymousConversationAllowed())) {
      throw new BadRequestException("Consulting type does not support anonymous conversations.");
    }
  }

  private UserDTO buildUserDto(CreateAnonymousEnquiryDTO createAnonymousEnquiryDTO) {
    return UserDTO.builder()
        .consultingType(String.valueOf(createAnonymousEnquiryDTO.getConsultingType()))
        .username(usernameRegistry.generateUniqueUsername())
        .password(userHelper.getRandomPassword())
        .postcode(DEFAULT_ANONYMOUS_POSTCODE)
        .termsAccepted("true")
        .build();
  }
}
