package de.caritas.cob.userservice.api.facade.conversation;

import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryResponseDTO;
import de.caritas.cob.userservice.api.service.conversation.anonymous.AnonymousConversationCreatorService;
import de.caritas.cob.userservice.api.service.user.anonymous.AnonymousUserCreatorService;
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

  public CreateAnonymousEnquiryResponseDTO createAnonymousEnquiry(
      CreateAnonymousEnquiryDTO createAnonymousEnquiryDTO) {
    return null;
  }
}
