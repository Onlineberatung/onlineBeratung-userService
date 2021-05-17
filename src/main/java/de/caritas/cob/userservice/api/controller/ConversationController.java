package de.caritas.cob.userservice.api.controller;

import static de.caritas.cob.userservice.api.controller.UserController.COUNT_INVALID_MESSAGE;
import static de.caritas.cob.userservice.api.controller.UserController.MIN_COUNT;
import static de.caritas.cob.userservice.api.controller.UserController.MIN_OFFSET;
import static de.caritas.cob.userservice.api.controller.UserController.OFFSET_INVALID_MESSAGE;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.REGISTERED_ENQUIRY;

import de.caritas.cob.userservice.api.controller.validation.MinValue;
import de.caritas.cob.userservice.api.conversation.facade.AcceptAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.conversation.service.ConversationListResolver;
import de.caritas.cob.userservice.api.facade.conversation.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.model.CreateAnonymousEnquiryResponseDTO;
import de.caritas.cob.userservice.generated.api.conversation.controller.ConversationsApi;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for conversation API requests.
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "conversation-controller")
public class ConversationController implements ConversationsApi {

  private final @NonNull ConversationListResolver conversationListResolver;
  private final @NonNull CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;
  private final @NonNull AcceptAnonymousEnquiryFacade acceptAnonymousEnquiryFacade;

  /**
   * Entry point to retrieve all anonymous enquiries for current authenticated consultant.
   *
   * @param offset Number of items where to start in the query (0 = first item) (required)
   * @param count  Number of items which are being returned (required)
   * @return the {@link ConsultantSessionListResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantSessionListResponseDTO> getAnonymousEnquiries(
      @MinValue(value = MIN_OFFSET, message = OFFSET_INVALID_MESSAGE) Integer offset,
      @MinValue(value = MIN_COUNT, message = COUNT_INVALID_MESSAGE) Integer count,
      @RequestHeader String rcToken) {

    ConsultantSessionListResponseDTO anonymousEnquirySessions =
        this.conversationListResolver
            .resolveConversations(offset, count, ANONYMOUS_ENQUIRY, rcToken);

    return ResponseEntity.ok(anonymousEnquirySessions);
  }

  /**
   * Entry point to retrieve all registered enquiries for current authenticated consultant.
   *
   * @param offset Number of items where to start in the query (0 = first item) (required)
   * @param count  Number of items which are being returned (required)
   * @return the {@link ConsultantSessionListResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantSessionListResponseDTO> getRegisteredEnquiries(
      @MinValue(value = MIN_OFFSET, message = OFFSET_INVALID_MESSAGE) Integer offset,
      @MinValue(value = MIN_COUNT, message = COUNT_INVALID_MESSAGE) Integer count,
      @RequestHeader String rcToken) {

    ConsultantSessionListResponseDTO registeredEnquirySessions =
        this.conversationListResolver
            .resolveConversations(offset, count, REGISTERED_ENQUIRY, rcToken);

    return ResponseEntity.ok(registeredEnquirySessions);
  }

  /**
   * Entry point to accept an existing anonymous enquiry for current authenticated consultant.
   *
   * @param sessionId the identifier of the existing anonymous session (required)
   * @return the {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> acceptAnonymousEnquiry(Long sessionId) {
    this.acceptAnonymousEnquiryFacade.acceptAnonymousEnquiry(sessionId);
    return ResponseEntity.ok().build();
  }

  /**
   * Starts a new anonymous conversation enquiry for the given consulting type and returns all
   * needed user information for this conversation.
   *
   * @param createAnonymousEnquiryDTO {@link CreateAnonymousEnquiryDTO} (required)
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
