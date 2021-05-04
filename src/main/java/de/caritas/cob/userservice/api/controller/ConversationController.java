package de.caritas.cob.userservice.api.controller;

import de.caritas.cob.userservice.api.facade.conversation.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryResponseDTO;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.generated.api.conversation.controller.ConversationsApi;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for conversation API requests.
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "conversation-controller")
public class ConversationController implements ConversationsApi {

  private final @NonNull CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;

  /**
   * Starts a new anonymous conversation enquiry for the given consulting type and returns all
   * needed user information for this conversation.
   *
   * @param createAnonymousEnquiryDTO  {@link CreateAnonymousEnquiryDTO} (required)
   * @return {@link ResponseEntity} containing {@link CreateAnonymousEnquiryResponseDTO} body
   */
  @Override
  public ResponseEntity<CreateAnonymousEnquiryResponseDTO> createAnonymousEnquiry(
      @Valid @RequestBody CreateAnonymousEnquiryDTO createAnonymousEnquiryDTO) {

    var createAnonymousEnquiryResponseDTO = createAnonymousEnquiryFacade
            .createAnonymousEnquiry(createAnonymousEnquiryDTO);

    return new ResponseEntity<>(createAnonymousEnquiryResponseDTO, HttpStatus.CREATED);
  }
}
