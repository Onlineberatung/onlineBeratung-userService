package de.caritas.cob.userservice.api.adapters.web.controller;

import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ANONYMOUS_ENQUIRY;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ARCHIVED_SESSION;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.ARCHIVED_TEAM_SESSION;
import static de.caritas.cob.userservice.api.conversation.model.ConversationListType.REGISTERED_ENQUIRY;

import de.caritas.cob.userservice.api.adapters.web.dto.AnonymousEnquiry;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAnonymousEnquiryDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAnonymousEnquiryResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.mapping.ConversationDtoMapper;
import de.caritas.cob.userservice.api.conversation.facade.AcceptAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.conversation.facade.CreateAnonymousEnquiryFacade;
import de.caritas.cob.userservice.api.conversation.facade.FinishAnonymousConversationFacade;
import de.caritas.cob.userservice.api.conversation.service.ConversationListResolver;
import de.caritas.cob.userservice.api.exception.httpresponses.ForbiddenException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.port.in.Messaging;
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

/** Controller for conversation API requests. */
@RestController
@RequiredArgsConstructor
@Api(tags = "conversation-controller")
public class ConversationController implements ConversationsApi {

  private final @NonNull ConversationListResolver conversationListResolver;
  private final @NonNull CreateAnonymousEnquiryFacade createAnonymousEnquiryFacade;
  private final @NonNull AcceptAnonymousEnquiryFacade acceptAnonymousEnquiryFacade;
  private final @NonNull FinishAnonymousConversationFacade finishAnonymousConversationFacade;
  private final ConversationDtoMapper mapper;
  private final Messaging messenger;
  private final AuthenticatedUser authenticatedUser;

  /**
   * Entry point to retrieve all anonymous enquiries for current authenticated consultant.
   *
   * @param offset Number of items where to start in the query (0 = first item) (required)
   * @param count Number of items which are being returned (required)
   * @return the {@link ConsultantSessionListResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantSessionListResponseDTO> getAnonymousEnquiries(
      Integer offset, Integer count, @RequestHeader String rcToken) {

    ConsultantSessionListResponseDTO anonymousEnquirySessions =
        this.conversationListResolver.resolveConversations(
            offset, count, ANONYMOUS_ENQUIRY, rcToken);

    return ResponseEntity.ok(anonymousEnquirySessions);
  }

  /**
   * Entry point to retrieve all registered enquiries for current authenticated consultant.
   *
   * @param offset Number of items where to start in the query (0 = first item) (required)
   * @param count Number of items which are being returned (required)
   * @return the {@link ConsultantSessionListResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantSessionListResponseDTO> getRegisteredEnquiries(
      Integer offset, Integer count, @RequestHeader String rcToken) {

    ConsultantSessionListResponseDTO registeredEnquirySessions =
        this.conversationListResolver.resolveConversations(
            offset, count, REGISTERED_ENQUIRY, rcToken);

    return ResponseEntity.ok(registeredEnquirySessions);
  }

  /**
   * Entry point to retrieve all archived sessions for current authenticated consultant.
   *
   * @param offset Number of items where to start in the query (0 = first item) (required)
   * @param count Number of items which are being returned (required)
   * @return the {@link ConsultantSessionListResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantSessionListResponseDTO> getArchivedSessions(
      Integer offset, Integer count, @RequestHeader String rcToken) {

    ConsultantSessionListResponseDTO archivedSessions =
        this.conversationListResolver.resolveConversations(
            offset, count, ARCHIVED_SESSION, rcToken);

    return ResponseEntity.ok(archivedSessions);
  }

  /**
   * Entry point to retrieve all archived team sessions for current authenticated consultant.
   *
   * @param offset Number of items where to start in the query (0 = first item) (required)
   * @param count Number of items which are being returned (required)
   * @return the {@link ConsultantSessionListResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantSessionListResponseDTO> getArchivedTeamSessions(
      Integer offset, Integer count, @RequestHeader String rcToken) {

    ConsultantSessionListResponseDTO archivedTeamSessions =
        this.conversationListResolver.resolveConversations(
            offset, count, ARCHIVED_TEAM_SESSION, rcToken);

    return ResponseEntity.ok(archivedTeamSessions);
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

    var createAnonymousEnquiryResponseDTO =
        createAnonymousEnquiryFacade.createAnonymousEnquiry(createAnonymousEnquiryDTO);

    return new ResponseEntity<>(createAnonymousEnquiryResponseDTO, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<AnonymousEnquiry> getAnonymousEnquiryDetails(Long sessionId) {
    var sessionMap =
        messenger
            .findSession(sessionId)
            .orElseThrow(() -> new NotFoundException("Session (%s) not found", sessionId));

    if (!mapper.adviceSeekerIdOf(sessionMap).equals(authenticatedUser.getUserId())) {
      throw new ForbiddenException(
          "Access to session (%s) is limited to its advice seeker.", sessionId);
    }

    var consultingTypeId = mapper.consultingTypeIdOf(sessionMap);
    var consultants = messenger.findAvailableConsultants(consultingTypeId);
    var anonymousEnquiry = mapper.anonymousEnquiryOf(sessionMap, consultants);

    return ResponseEntity.ok(anonymousEnquiry);
  }

  /**
   * Finishes an anonymous conversation and sets the status to done.
   *
   * @param sessionId the identifier of the session
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> finishAnonymousConversation(Long sessionId) {
    this.finishAnonymousConversationFacade.finishConversation(sessionId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
