package de.caritas.cob.userservice.api.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.controller.validation.MinValue;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.facade.CreateChatFacade;
import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.facade.CreateNewConsultingTypeFacade;
import de.caritas.cob.userservice.api.facade.CreateUserFacade;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.GetChatFacade;
import de.caritas.cob.userservice.api.facade.GetChatMembersFacade;
import de.caritas.cob.userservice.api.facade.JoinAndLeaveChatFacade;
import de.caritas.cob.userservice.api.facade.StartChatFacade;
import de.caritas.cob.userservice.api.facade.StopChatFacade;
import de.caritas.cob.userservice.api.facade.assignsession.AssignSessionFacade;
import de.caritas.cob.userservice.api.facade.sessionlist.SessionListFacade;
import de.caritas.cob.userservice.api.facade.userdata.ConsultantDataFacade;
import de.caritas.cob.userservice.api.facade.userdata.UserDataFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.AuthenticatedUserHelper;
import de.caritas.cob.userservice.api.model.AbsenceDTO;
import de.caritas.cob.userservice.api.model.ChatInfoResponseDTO;
import de.caritas.cob.userservice.api.model.ChatMembersResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.model.DeleteUserAccountDTO;
import de.caritas.cob.userservice.api.model.EnquiryMessageDTO;
import de.caritas.cob.userservice.api.model.MasterKeyDTO;
import de.caritas.cob.userservice.api.model.MobileTokenDTO;
import de.caritas.cob.userservice.api.model.NewMessageNotificationDTO;
import de.caritas.cob.userservice.api.model.NewRegistrationResponseDto;
import de.caritas.cob.userservice.api.model.PasswordDTO;
import de.caritas.cob.userservice.api.model.SessionDataDTO;
import de.caritas.cob.userservice.api.model.UpdateChatResponseDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.chat.ChatDTO;
import de.caritas.cob.userservice.api.model.monitoring.MonitoringDTO;
import de.caritas.cob.userservice.api.model.registration.NewRegistrationDto;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.user.UserDataResponseDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionFilter;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.AskerImportService;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantImportService;
import de.caritas.cob.userservice.api.service.DecryptionService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.SessionDataService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import de.caritas.cob.userservice.generated.api.controller.UsersApi;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.MapUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user api requests
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "user-controller")
public class  UserController implements UsersApi {

  private static final int MIN_OFFSET = 0;
  private static final int MIN_COUNT = 1;
  private static final String OFFSET_INVALID_MESSAGE = "offset must be a positive number";
  private static final String COUNT_INVALID_MESSAGE = "count must be a positive number";

  private final @NotNull ValidatedUserAccountProvider userAccountProvider;
  private final @NotNull SessionService sessionService;
  private final @NotNull AuthenticatedUser authenticatedUser;
  private final @NotNull CreateEnquiryMessageFacade createEnquiryMessageFacade;
  private final @NotNull UserDataFacade userDataFacade;
  private final @NotNull ConsultantImportService consultantImportService;
  private final @NotNull EmailNotificationFacade emailNotificationFacade;
  private final @NotNull MonitoringService monitoringService;
  private final @NotNull AskerImportService askerImportService;
  private final @NotNull SessionListFacade sessionListFacade;
  private final @NotNull ConsultantAgencyService consultantAgencyService;
  private final @NotNull AssignSessionFacade assignSessionFacade;
  private final @NotNull DecryptionService decryptionService;
  private final @NotNull AuthenticatedUserHelper authenticatedUserHelper;
  private final @NotNull ChatService chatService;
  private final @NotNull StartChatFacade startChatFacade;
  private final @NotNull GetChatFacade getChatFacade;
  private final @NotNull JoinAndLeaveChatFacade joinAndLeaveChatFacade;
  private final @NotNull CreateChatFacade createChatFacade;
  private final @NotNull StopChatFacade stopChatFacade;
  private final @NotNull GetChatMembersFacade getChatMembersFacade;
  private final @NotNull CreateUserFacade createUserFacade;
  private final @NotNull CreateNewConsultingTypeFacade createNewConsultingTypeFacade;
  private final @NotNull ConsultantDataFacade consultantDataFacade;
  private final @NotNull SessionDataService sessionDataService;

  /**
   * Creates an user account and returns a 201 CREATED on success.
   *
   * @param user the {@link UserDTO}
   * @return {@link ResponseEntity} with possible registration conflict information in header
   */
  @Override
  public ResponseEntity<Void> registerUser(@Valid @RequestBody UserDTO user) {
    user.setNewUserAccount(true);
    createUserFacade.createUserAccountWithInitializedConsultingType(user);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  /**
   * Creates a new session or chat-agency relation depending on the provided consulting type.
   *
   * @param rcToken            Rocket.Chat token (required)
   * @param rcUserId           Rocket.Chat user ID (required)
   * @param newRegistrationDto {@link NewRegistrationDto}
   * @return {@link ResponseEntity} containing {@link NewRegistrationResponseDto}
   */
  @Override
  public ResponseEntity<NewRegistrationResponseDto> registerNewConsultingType(
      @RequestHeader String rcToken, @RequestHeader String rcUserId,
      @Valid @RequestBody NewRegistrationDto newRegistrationDto) {

    User user = this.userAccountProvider.retrieveValidatedUser();
    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .rocketChatToken(rcToken)
        .rocketChatUserId(rcUserId)
        .build();

    return new ResponseEntity<>(new NewRegistrationResponseDto()
        .sessionId(createNewConsultingTypeFacade
            .initializeNewConsultingType(newRegistrationDto, user, rocketChatCredentials))
        .status(HttpStatus.CREATED), HttpStatus.CREATED);
  }

  /**
   * Assigns the given session to the calling consultant.
   *
   * @param sessionId Session ID (required)
   * @param rcUserId  Rocket.Chat user ID (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> acceptEnquiry(@PathVariable Long sessionId,
      @RequestHeader String rcUserId) {

    Optional<Session> session = sessionService.getSession(sessionId);

    if (!session.isPresent() || isNull(session.get().getGroupId())) {
      LogService.logInternalServerError(String.format(
          "Session id %s is invalid, session not found or has no Rocket.Chat groupId assigned.",
          sessionId));

      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    Consultant consultant = this.userAccountProvider.retrieveValidatedConsultant();
    assignSessionFacade.assignEnquiry(session.get(), consultant);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * @param sessionId      Session Id (required)
   * @param rcToken        Rocket.Chat token (required)
   * @param rcUserId       Rocket.Chat user ID (required)
   * @param enquiryMessage Enquiry message (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> createEnquiryMessage(@PathVariable Long sessionId,
      @RequestHeader String rcToken, @RequestHeader String rcUserId,
      @RequestBody EnquiryMessageDTO enquiryMessage) {

    User user = this.userAccountProvider.retrieveValidatedUser();
    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .rocketChatToken(rcToken)
        .rocketChatUserId(rcUserId)
        .build();

    createEnquiryMessageFacade.createEnquiryMessage(user, sessionId,
        enquiryMessage.getMessage(), rocketChatCredentials);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  /**
   * Returns a list of sessions for the currently authenticated/logged in user.
   *
   * @param rcToken Rocket.Chat token (required)
   * @return {@link ResponseEntity} of {@link UserSessionListResponseDTO}
   */
  @Override
  public ResponseEntity<UserSessionListResponseDTO> getSessionsForAuthenticatedUser(
      @RequestHeader String rcToken) {

    User user = this.userAccountProvider.retrieveValidatedUser();
    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .rocketChatUserId(user.getRcUserId())
        .rocketChatToken(rcToken)
        .build();

    UserSessionListResponseDTO userSessionsDTO = sessionListFacade
        .retrieveSortedSessionsForAuthenticatedUser(user.getUserId(), rocketChatCredentials);

    return isNotEmpty(userSessionsDTO.getSessions())
        ? new ResponseEntity<>(userSessionsDTO, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Updates the absence (and its message) for the calling consultant.
   *
   * @param absence {@link AbsenceDTO}
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> updateAbsence(@RequestBody AbsenceDTO absence) {
    Consultant consultant = userAccountProvider.retrieveValidatedConsultant();
    this.consultantDataFacade.updateConsultantAbsent(consultant, absence);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Gets the user data for the current logged in user depending on his user role.
   *
   * @return {@link ResponseEntity} containing {@link UserDataResponseDTO}
   */
  @Override
  public ResponseEntity<UserDataResponseDTO> getUserData() {

    UserDataResponseDTO responseDTO = this.userDataFacade.buildUserDataByRole();

    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
  }

  /**
   * Updates the data for the current logged in consultant.
   *
   * @param updateConsultantDTO  (required) the request {@link UpdateConsultantDTO}
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> updateConsultantData(UpdateConsultantDTO updateConsultantDTO) {
    this.consultantDataFacade.updateConsultantData(updateConsultantDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Returns a list of sessions for the currently authenticated consultant depending on the
   * submitted sessionStatus.
   *
   * @param rcToken Rocket.Chat token (required)
   * @param offset  Number of items where to start in the query (0 = first item) (required)
   * @param count   Number of items which are being returned (required)
   * @param filter  Information on how to filter the list (required)
   * @param status  Session status type (optional)
   * @return {@link ResponseEntity} containing {@link ConsultantSessionListResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantSessionListResponseDTO> getSessionsForAuthenticatedConsultant(
      @RequestHeader String rcToken,
      @MinValue(value = MIN_OFFSET, message = OFFSET_INVALID_MESSAGE) Integer offset,
      @MinValue(value = MIN_COUNT, message = COUNT_INVALID_MESSAGE) Integer count,
      @RequestParam String filter,
      @RequestParam Integer status) {

    Consultant consultant = this.userAccountProvider.retrieveValidatedConsultant();

    ConsultantSessionListResponseDTO consultantSessionListResponseDTO = null;
    Optional<SessionFilter> optionalSessionFilter = SessionFilter.getByValue(filter);
    if (optionalSessionFilter.isPresent()) {

      SessionListQueryParameter sessionListQueryParameter = SessionListQueryParameter.builder()
          .sessionStatus(status).count(count).offset(offset)
          .sessionFilter(optionalSessionFilter.get())
          .build();

      consultantSessionListResponseDTO = sessionListFacade
          .retrieveSessionsDtoForAuthenticatedConsultant(consultant,
              rcToken, sessionListQueryParameter);
    }

    return nonNull(consultantSessionListResponseDTO) && isNotEmpty(
        consultantSessionListResponseDTO.getSessions())
        ? new ResponseEntity<>(consultantSessionListResponseDTO, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Returns a list of team consulting sessions for the currently authenticated consultant.
   *
   * @param rcToken Rocket.Chat token (required)
   * @param offset  Number of items where to start in the query (0 = first item) (required)
   * @param count   Number of items which are being returned (required)
   * @param filter  Information on how to filter the list (required)
   * @return {@link ResponseEntity} containing {@link ConsultantSessionListResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantSessionListResponseDTO> getTeamSessionsForAuthenticatedConsultant(
      @RequestHeader String rcToken,
      @MinValue(value = MIN_OFFSET, message = OFFSET_INVALID_MESSAGE) Integer offset,
      @MinValue(value = MIN_COUNT, message = COUNT_INVALID_MESSAGE) Integer count,
      @RequestParam String filter) {

    Consultant consultant = this.userAccountProvider.retrieveValidatedTeamConsultant();

    ConsultantSessionListResponseDTO teamSessionListDTO = null;
    Optional<SessionFilter> optionalSessionFilter = SessionFilter.getByValue(filter);
    if (optionalSessionFilter.isPresent()) {

      SessionListQueryParameter sessionListQueryParameter = SessionListQueryParameter.builder()
          .count(count).offset(offset).sessionFilter(optionalSessionFilter.get())
          .build();

      teamSessionListDTO = sessionListFacade
          .retrieveTeamSessionsDtoForAuthenticatedConsultant(consultant,
              rcToken, sessionListQueryParameter);
    }

    return nonNull(teamSessionListDTO) && isNotEmpty(teamSessionListDTO.getSessions())
        ? new ResponseEntity<>(teamSessionListDTO, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Imports a file list of consultants. Technical user authorization required.
   *
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> importConsultants() {

    consultantImportService.startImport();

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Imports a file list of askers. Technical user authorization required.
   *
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> importAskers() {

    askerImportService.startImport();

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Imports a file list of askers without a session. Technical user authorization required.
   *
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> importAskersWithoutSession() {

    askerImportService.startImportForAskersWithoutSession();

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Sends email notifications to the user(s) if there has been a new answer. Uses the provided
   * Keycloak authorization token for user verification (user role). This means that the user that
   * wrote the answer should also call this method.
   *
   * @param newMessageNotificationDTO (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> sendNewMessageNotification(
      @RequestBody NewMessageNotificationDTO newMessageNotificationDTO) {

    emailNotificationFacade.sendNewMessageNotification(newMessageNotificationDTO.getRcGroupId(),
        authenticatedUser.getRoles(), authenticatedUser.getUserId());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Sends email notifications to the user(s) if there has been a new feedback answer. Uses the
   * provided Keycloak authorization token for user verification (user role). This means that the
   * user that wrote the answer should also call this method.
   *
   * @param newMessageNotificationDTO (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> sendNewFeedbackMessageNotification(
      @RequestBody NewMessageNotificationDTO newMessageNotificationDTO) {

    emailNotificationFacade.sendNewFeedbackMessageNotification(
        newMessageNotificationDTO.getRcGroupId(), authenticatedUser.getUserId());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Returns the monitoring for the given session.
   *
   * @param sessionId Session Id (required)
   * @return {@link ResponseEntity} containing {@link MonitoringDTO}
   */
  @Override
  public ResponseEntity<MonitoringDTO> getMonitoring(@PathVariable Long sessionId) {

    // Check if session exists
    Optional<Session> session = sessionService.getSession(sessionId);
    if (!session.isPresent()) {
      LogService.logBadRequest(String.format("Session with id %s not found", sessionId));
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Check if consultant has the right to access the session
    if (!authenticatedUserHelper.hasPermissionForSession(session.get())) {
      LogService.logBadRequest(
          String.format("Consultant with id %s has no permission to access session with id %s",
              authenticatedUser.getUserId(), sessionId));
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    MonitoringDTO responseDTO = monitoringService.getMonitoring(session.get());

    if (nonNull(responseDTO) && MapUtils.isNotEmpty(responseDTO.getProperties())) {
      return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

  }

  /**
   * Updates the monitoring values of a {@link Session}. Only a consultant which is directly
   * assigned to the session can update the values (MVP only).
   *
   * @param sessionId  Session Id (required)
   * @param monitoring {@link MonitoringDTO} (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> updateMonitoring(@PathVariable Long sessionId,
      @RequestBody MonitoringDTO monitoring) {

    Optional<Session> session = sessionService.getSession(sessionId);

    if (session.isPresent()) {

      // Check if calling consultant has the permission to update the monitoring values
      if (authenticatedUserHelper.hasPermissionForSession(session.get())) {
        monitoringService.updateMonitoring(session.get().getId(), monitoring);
        return new ResponseEntity<>(HttpStatus.OK);

      } else {
        LogService.logUnauthorized(String.format(
            "Consultant with id %s is not authorized to update monitoring of session %s",
            authenticatedUser.getUserId(), sessionId));
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
      }

    } else {
      LogService.logBadRequest(String.format("Session with id %s not found", sessionId));
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Returns all consultants of the provided agency id as a list of {@link ConsultantResponseDTO}.
   *
   * @param agencyId Agency Id (required)
   * @return {@link ResponseEntity} containing {@link List} of {@link ConsultantResponseDTO}
   */
  @Override
  public ResponseEntity<List<ConsultantResponseDTO>> getConsultants(
      @RequestParam Long agencyId) {

    List<ConsultantResponseDTO> consultants =
        consultantAgencyService.getConsultantsOfAgency(agencyId);

    return isNotEmpty(consultants)
        ? new ResponseEntity<>(consultants, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Assigns a session (the provided session id) to the provided consultant id.
   *
   * @param sessionId    Session Id (required)
   * @param consultantId Consultant Id (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> assignSession(@PathVariable Long sessionId,
      @PathVariable String consultantId) {

    this.userAccountProvider.retrieveValidatedConsultant();

    Optional<Session> session = sessionService.getSession(sessionId);

    if (!session.isPresent()) {
      LogService.logInternalServerError(String.format("Session with id %s not found.", sessionId));
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Check if the calling consultant has the correct right to assign this session to a new
    // consultant
    if (session.get().getStatus().equals(SessionStatus.IN_PROGRESS) && !authenticatedUser
        .getGrantedAuthorities().contains(Authority.ASSIGN_CONSULTANT_TO_SESSION)) {
      LogService.logForbidden(String.format(
          "The calling consultant with id %s does not have the authority to assign a session to a another consultant.",
          authenticatedUser.getUserId()));

      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Check if the calling consultant has the correct right to assign the enquiry to a consultant
    if (session.get().getStatus().equals(SessionStatus.NEW) && !authenticatedUser
        .getGrantedAuthorities().contains(Authority.ASSIGN_CONSULTANT_TO_ENQUIRY)) {
      LogService.logForbidden(String.format(
          "The calling consultant with id %s does not have the authority to assign the enquiry to a consultant.",
          authenticatedUser.getUserId()));

      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    Consultant consultant = this.userAccountProvider.retrieveValidatedConsultantById(consultantId);
    assignSessionFacade.assignSession(session.get(), consultant);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Changes the (Keycloak) password of the currently authenticated user.
   *
   * @param passwordDTO (required) {@link PasswordDTO}
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> updatePassword(@RequestBody PasswordDTO passwordDTO) {
    this.userAccountProvider.changePassword(passwordDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Updates the master key fragment for the en-/decryption of messages.
   *
   * @param masterKey {@link MasterKeyDTO} (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> updateKey(@RequestBody MasterKeyDTO masterKey) {
    if (!decryptionService.getMasterKey().equals(masterKey.getMasterKey())) {
      decryptionService.updateMasterKey(masterKey.getMasterKey());
      LogService.logInfo("MasterKey updated");
      return new ResponseEntity<>(HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.CONFLICT);
  }

  /**
   * Creates a new chat with the given details and returns the generated chat link.
   *
   * @param chatDTO {@link ChatDTO} (required)
   * @return {@link ResponseEntity} containing {@link CreateChatResponseDTO}
   */
  @Override
  public ResponseEntity<CreateChatResponseDTO> createChat(@RequestBody ChatDTO chatDTO) {

    Consultant callingConsultant = this.userAccountProvider.retrieveValidatedConsultant();
    // Create chat and return chat link
    CreateChatResponseDTO response = createChatFacade.createChat(chatDTO, callingConsultant);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  /**
   * Starts a chat.
   *
   * @param chatId Chat Id (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> startChat(@PathVariable Long chatId) {

    Chat chat = chatService.getChat(chatId)
        .orElseThrow(() -> new BadRequestException(
            String.format("Chat with id %s not found for starting chat.", chatId)));

    Consultant callingConsultant = this.userAccountProvider.retrieveValidatedConsultant();
    startChatFacade.startChat(chat, callingConsultant);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Gets the chat info of provided chat ID.
   *
   * @param chatId Chat Id (required)
   * @return {@link ResponseEntity} containing {@link ChatInfoResponseDTO}
   */
  @Override
  public ResponseEntity<ChatInfoResponseDTO> getChat(@PathVariable Long chatId) {

    ChatInfoResponseDTO response = getChatFacade.getChat(chatId);

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Join a chat.
   *
   * @param chatId Chat Id (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> joinChat(@PathVariable Long chatId) {

    joinAndLeaveChatFacade.joinChat(chatId, authenticatedUser);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Stops the given chat (chatId). Deletes all users and messages from the Rocket.Chat room
   * (repetitive chat) or deletes the whole room (singular chat).
   *
   * @param chatId Chat Id (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> stopChat(@PathVariable Long chatId) {

    Chat chat = chatService.getChat(chatId)
        .orElseThrow(() -> new BadRequestException(
            String.format("Chat with id %s not found while trying to stop the chat.", chatId)));

    Consultant callingConsultant = this.userAccountProvider.retrieveValidatedConsultant();
    stopChatFacade.stopChat(chat, callingConsultant);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Gets the members of a chat.
   *
   * @param chatId Chat Id (required)
   * @return {@link ResponseEntity} containing {@link ChatMembersResponseDTO}
   */
  @Override
  public ResponseEntity<ChatMembersResponseDTO> getChatMembers(@PathVariable Long chatId) {

    ChatMembersResponseDTO chatMembersResponseDTO = getChatMembersFacade.getChatMembers(chatId);

    return new ResponseEntity<>(chatMembersResponseDTO, HttpStatus.OK);
  }

  /**
   * Leave a chat.
   *
   * @param chatId Chat Id (required)
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> leaveChat(@PathVariable Long chatId) {

    joinAndLeaveChatFacade.leaveChat(chatId, authenticatedUser);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Updates the settings of the given {@link Chat}.
   *
   * @param chatId  Chat Id (required)
   * @param chatDTO {@link ChatDTO} (required)
   * @return {@link ResponseEntity} containing {@link UpdateChatResponseDTO}
   */
  @Override
  public ResponseEntity<UpdateChatResponseDTO> updateChat(@PathVariable Long chatId,
      @RequestBody ChatDTO chatDTO) {

    UpdateChatResponseDTO updateChatResponseDTO = chatService.updateChat(chatId, chatDTO,
        authenticatedUser);
    return new ResponseEntity<>(updateChatResponseDTO, HttpStatus.OK);
  }

  /**
   * Get a specific {@link ConsultantSessionDTO} for a consultant.
   *
   * @param sessionId Session id (required)
   * @return {@link ResponseEntity} containing {@link ConsultantSessionDTO}
   */
  @Override
  public ResponseEntity<ConsultantSessionDTO> fetchSessionForConsultant(
      @PathVariable Long sessionId) {

    Consultant consultant = this.userAccountProvider.retrieveValidatedConsultant();
    ConsultantSessionDTO consultantSessionDTO = sessionService
        .fetchSessionForConsultant(sessionId, consultant);
    return new ResponseEntity<>(consultantSessionDTO, HttpStatus.OK);
  }

  /**
   * Updates or sets the email address for the current authenticated user.
   *
   * @param emailAddress the email address to set
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> updateEmailAddress(@Valid String emailAddress) {
    this.userAccountProvider.changeUserAccountEmailAddress(emailAddress);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Flags an user account for deletion and deactivates the Keycloak account.
   *
   * @param deleteUserAccountDTO (required) {@link DeleteUserAccountDTO}
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> deactivateAndFlagUserAccountForDeletion(
      @Valid DeleteUserAccountDTO deleteUserAccountDTO) {
    this.userAccountProvider.deactivateAndFlagUserAccountForDeletion(deleteUserAccountDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Updates or sets the mobile client token for the current authenticated user.
   *
   * @param mobileTokenDTO (required) the mobile device identifier {@link MobileTokenDTO}
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> updateMobileToken(@Valid MobileTokenDTO mobileTokenDTO) {
    this.userAccountProvider.updateUserMobileToken(mobileTokenDTO.getToken());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Updates the session data for the given session.
   *
   * @param sessionId       (required) session ID
   * @param sessionDataDTO  (required) {@link SessionDataDTO}
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> updateSessionData(@PathVariable Long sessionId,
      @Valid SessionDataDTO sessionDataDTO) {
    this.sessionDataService.saveSessionData(sessionId, sessionDataDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
