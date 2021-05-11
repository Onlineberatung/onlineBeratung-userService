package de.caritas.cob.userservice.api.facade.conversation;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.user.AnonymousUserCredentials;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.service.conversation.anonymous.AnonymousConversationCreatorService;
import de.caritas.cob.userservice.api.service.user.anonymous.AnonymousUserCreatorService;
import de.caritas.cob.userservice.api.service.user.anonymous.AnonymousUsernameRegistry;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps of creating a new anonymous conversation.
 */
@Service
@RequiredArgsConstructor
public class CreateAnonymousEnquiryFacade {

  private final @NonNull AnonymousUserCreatorService anonymousUserCreatorService;
  private final @NonNull AnonymousConversationCreatorService anonymousConversationCreatorService;
  private final @NonNull AnonymousUsernameRegistry usernameRegistry;
  private final @NonNull UserHelper userHelper;

  private static final String DEFAULT_ANONYMOUS_POSTCODE = "00000";

  public CreateAnonymousEnquiryResponseDTO createAnonymousEnquiry(
      CreateAnonymousEnquiryDTO createAnonymousEnquiryDTO) {

    checkIfConsultingTypeHasAnonymousConsulting(createAnonymousEnquiryDTO);

    var userDto = buildUserDto(createAnonymousEnquiryDTO);
    AnonymousUserCredentials credentials = anonymousUserCreatorService.createAnonymousUser(userDto);
    var session =
        anonymousConversationCreatorService.createAnonymousConversation(userDto, credentials);

    return new CreateAnonymousEnquiryResponseDTO()
        .userName(userDto.getUsername())
        .accessToken(credentials.getAccessToken())
        .refreshToken(credentials.getRefreshToken())
        .rcUserId(credentials.getRocketChatCredentials().getRocketChatUserId())
        .rcToken(credentials.getRocketChatCredentials().getRocketChatToken())
        .rcGroupId(session.getGroupId())
        .sessionId(session.getId());
  }

  private void checkIfConsultingTypeHasAnonymousConsulting(
      CreateAnonymousEnquiryDTO createAnonymousEnquiryDTO) {
    var consultingType =
        ConsultingType.values()[createAnonymousEnquiryDTO.getConsultingType()];
    List<ConsultingType> blockedConsultingTypes = List.of(ConsultingType.U25,
        ConsultingType.REGIONAL, ConsultingType.KREUZBUND, ConsultingType.SUPPORTGROUPVECHTA);

    if (blockedConsultingTypes.contains(consultingType)) {
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
