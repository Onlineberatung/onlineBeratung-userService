package de.caritas.cob.userservice.api.controller;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.exception.SaveChatException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import de.caritas.cob.userservice.api.authorization.Authority;
import de.caritas.cob.userservice.api.authorization.UserRole;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.facade.assignsession.AssignSessionFacade;
import de.caritas.cob.userservice.api.facade.CreateChatFacade;
import de.caritas.cob.userservice.api.facade.CreateEnquiryMessageFacade;
import de.caritas.cob.userservice.api.facade.CreateSessionFacade;
import de.caritas.cob.userservice.api.facade.CreateUserFacade;
import de.caritas.cob.userservice.api.facade.DeleteUserFacade;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.GetChatFacade;
import de.caritas.cob.userservice.api.facade.GetChatMembersFacade;
import de.caritas.cob.userservice.api.facade.GetSessionListFacade;
import de.caritas.cob.userservice.api.facade.GetUserDataFacade;
import de.caritas.cob.userservice.api.facade.JoinAndLeaveChatFacade;
import de.caritas.cob.userservice.api.facade.StartChatFacade;
import de.caritas.cob.userservice.api.facade.StopChatFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.AuthenticatedUserHelper;
import de.caritas.cob.userservice.api.model.AbsenceDTO;
import de.caritas.cob.userservice.api.model.ChatDTO;
import de.caritas.cob.userservice.api.model.ChatInfoResponseDTO;
import de.caritas.cob.userservice.api.model.ChatMembersResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.model.CreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.DeleteUserDTO;
import de.caritas.cob.userservice.api.model.EnquiryMessageDTO;
import de.caritas.cob.userservice.api.model.MasterKeyDTO;
import de.caritas.cob.userservice.api.model.MonitoringDTO;
import de.caritas.cob.userservice.api.model.NewMessageNotificationDTO;
import de.caritas.cob.userservice.api.model.NewRegistrationDto;
import de.caritas.cob.userservice.api.model.NewRegistrationResponseDto;
import de.caritas.cob.userservice.api.model.PasswordDTO;
import de.caritas.cob.userservice.api.model.UpdateChatResponseDTO;
import de.caritas.cob.userservice.api.model.UserDTO;
import de.caritas.cob.userservice.api.model.UserDataResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.keycloak.login.LoginResponseDTO;
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
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.DecryptionService;
import de.caritas.cob.userservice.api.service.KeycloakService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.UserService;
import de.caritas.cob.userservice.generated.api.controller.UsersApi;
import io.swagger.annotations.Api;

/**
 * Controller for user api requests
 */
@RestController
@Api(tags = "user-controller")
public class UserController implements UsersApi {

  @Value("${api.success.userRegistered}")
  private String USER_REGISTERED;

  @Value("${api.error.keycloakError}")
  private String KEYCLOAK_ERROR;

  @Value("${api.error.userRegistered}")
  private String USER_NOT_CREATED;

  @Value("${keycloakService.user.dummySuffix}")
  private String EMAIL_DUMMY_SUFFIX;

  private final int MIN_OFFSET = 0;
  private final int MIN_COUNT = 1;

  private final UserService userService;
  private final ConsultantService consultantService;
  private final SessionService sessionService;
  private final AuthenticatedUser authenticatedUser;
  private final CreateEnquiryMessageFacade createEnquiryMessageFacade;
  private final GetUserDataFacade getUserDataFacade;
  private final ConsultantImportService consultantImportService;
  private final EmailNotificationFacade emailNotificationFacade;
  private final MonitoringService monitoringService;
  private final AskerImportService askerImportService;
  private final GetSessionListFacade getSessionListFacade;
  private final ConsultantAgencyService consultantAgencyService;
  private final AssignSessionFacade assignSessionFacade;
  private final KeycloakService keycloakService;
  private final DecryptionService decryptionService;
  private final AuthenticatedUserHelper authenticatedUserHelper;
  private final ChatService chatService;
  private final StartChatFacade startChatFacade;
  private final GetChatFacade getChatFacade;
  private final JoinAndLeaveChatFacade joinAndLeaveChatFacade;
  private final CreateChatFacade createChatFacade;
  private final StopChatFacade stopChatFacade;
  private final GetChatMembersFacade getChatMembersFacade;
  private final CreateUserFacade createUserFacade;
  private final DeleteUserFacade deleteUserFacade;
  private final CreateSessionFacade createSessionFacade;

  @Autowired
  public UserController(UserService userService, ConsultantService consultantService,
      SessionService sessionService, AuthenticatedUser authenticatedUser,
      CreateEnquiryMessageFacade createEnquiryMessageFacade, GetUserDataFacade getUserDataFacade,
      ConsultantImportService consultantImportService,
      EmailNotificationFacade emailNotificationFacade, MonitoringService monitoringService,
      AskerImportService askerImportService, GetSessionListFacade getSessionListFacade,
      ConsultantAgencyService consultantAgencyService, AssignSessionFacade assignSessionFacade,
      KeycloakService keycloakService, DecryptionService encryptionService,
      AuthenticatedUserHelper authenticatedUserHelper, ChatService chatService,
      StartChatFacade startChatFacade, GetChatFacade getChatFacade,
      JoinAndLeaveChatFacade joinAndLeaveChatFacade, CreateChatFacade createChatFacade,
      StopChatFacade stopChatFacade, GetChatMembersFacade getChatMembersFacade,
      CreateUserFacade createUserFacade, DeleteUserFacade deleteUserFacade,
      CreateSessionFacade createSessionFacade) {

    this.userService = userService;
    this.consultantService = consultantService;
    this.sessionService = sessionService;
    this.authenticatedUser = authenticatedUser;
    this.createEnquiryMessageFacade = createEnquiryMessageFacade;
    this.getUserDataFacade = getUserDataFacade;
    this.consultantImportService = consultantImportService;
    this.emailNotificationFacade = emailNotificationFacade;
    this.monitoringService = monitoringService;
    this.askerImportService = askerImportService;
    this.getSessionListFacade = getSessionListFacade;
    this.consultantAgencyService = consultantAgencyService;
    this.assignSessionFacade = assignSessionFacade;
    this.keycloakService = keycloakService;
    this.decryptionService = encryptionService;
    this.authenticatedUserHelper = authenticatedUserHelper;
    this.chatService = chatService;
    this.startChatFacade = startChatFacade;
    this.getChatFacade = getChatFacade;
    this.joinAndLeaveChatFacade = joinAndLeaveChatFacade;
    this.createChatFacade = createChatFacade;
    this.stopChatFacade = stopChatFacade;
    this.getChatMembersFacade = getChatMembersFacade;
    this.createUserFacade = createUserFacade;
    this.deleteUserFacade = deleteUserFacade;
    this.createSessionFacade = createSessionFacade;
  }

  /**
   * Creates a Keycloak user and returns a 201 CREATED on success
   *
   * @param user
   * @return
   */
  @Override
  public ResponseEntity<CreateUserResponseDTO> registerUser(@Valid @RequestBody UserDTO user) {

    KeycloakCreateUserResponseDTO response = createUserFacade.createUserAndInitializeAccount(user);

    if (!response.getStatus().equals(HttpStatus.CONFLICT)) {
      return new ResponseEntity<>(response.getStatus());
    } else {
      return new ResponseEntity<>(response.getResponseDTO(),
          response.getStatus());
    }
  }

  /**
   * Creates a new session if there is not already an existing session for the provided consulting
   * type.
   *
   * @param newRegistrationDto {@link NewRegistrationDto}
   * @return {@link ResponseEntity} with {@link NewRegistrationResponseDto}
   */
  @Override
  public ResponseEntity<NewRegistrationResponseDto> registerNewConsultingType(
      @Valid @RequestBody NewRegistrationDto newRegistrationDto) {

    Optional<User> user = userService.getUser(authenticatedUser.getUserId());

    if (!user.isPresent()) {
      throw new InternalServerErrorException(
          String.format("User with id %s not found while registering new consulting type: %s",
              authenticatedUser.getUserId(), newRegistrationDto.toString()));
    }

    Long createdSessionId = createSessionFacade.createSession(newRegistrationDto, user.get());
    return new ResponseEntity<>(
        NewRegistrationResponseDto.builder()
            .sessionId(createdSessionId)
            .status(HttpStatus.CREATED)
            .build(), HttpStatus.CREATED);
  }

  /**
   * Accepting an enquiry
   */
  @Override
  public ResponseEntity<Void> acceptEnquiry(@PathVariable("sessionId") Long sessionId,
      @RequestHeader String rcUserId) {

    Optional<Session> session = sessionService.getSession(sessionId);

    if (!session.isPresent() || session.get().getGroupId() == null) {
      LogService.logInternalServerError(String.format(
          "Session id %s is invalid, session not found or has no Rocket.Chat groupId assigned.",
          sessionId));

      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    Optional<Consultant> consultant =
        consultantService.getConsultant(authenticatedUser.getUserId());

    if (!consultant.isPresent()) {
      LogService.logInternalServerError(
          String.format("Consultant with id %s not found for accepting the enquiry.",
              authenticatedUser.getUserId()));

      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    assignSessionFacade.assignEnquiry(session.get(), consultant.get());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Creating an enquiry message
   */

  @Override
  public ResponseEntity<Void> createEnquiryMessage(
      @Valid @NotNull @PathVariable("sessionId") Long sessionId, @RequestHeader String rcToken,
      @RequestHeader String rcUserId, @Valid @RequestBody EnquiryMessageDTO enquiryMessage) {

    Optional<User> user = userService.getUser(authenticatedUser.getUserId());

    if (!user.isPresent()) {
      LogService.logInternalServerError(
          String.format("User with id %s not found while creating enquiry message.",
              authenticatedUser.getUserId()));
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    RocketChatCredentials rocketChatCredentials =
        RocketChatCredentials.builder().RocketChatToken(rcToken).RocketChatUserId(rcUserId).build();
    createEnquiryMessageFacade.createEnquiryMessage(user.get(), sessionId,
        enquiryMessage.getMessage(), rocketChatCredentials);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  /**
   * Returns a list of sessions for the currently authenticated/logged in user
   *
   * @param rcToken Rocket.Chat token as request header value
   * @return {@link List} of {@link UserSessionResponseDTO}
   */
  @Override
  public ResponseEntity<UserSessionListResponseDTO> getSessionsForAuthenticatedUser(
      @RequestHeader String rcToken) {

    Optional<User> user = userService.getUser(authenticatedUser.getUserId());

    if (!user.isPresent()) {
      LogService.logInternalServerError(String.format(
          "User with id %s not found for getting user sessions.", authenticatedUser.getUserId()));
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    RocketChatCredentials rocketChatCredentials = RocketChatCredentials.builder()
        .RocketChatUserId(user.get().getRcUserId()).RocketChatToken(rcToken).build();
    UserSessionListResponseDTO sessions = getSessionListFacade
        .getSessionsForAuthenticatedUser(user.get().getUserId(), rocketChatCredentials);

    return isNotEmpty(sessions.getSessions())
        ? new ResponseEntity<>(sessions, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  public ResponseEntity<Void> updateAbsence(@Valid @RequestBody AbsenceDTO absence) {
    Optional<Consultant> consultant =
        consultantService.getConsultant(authenticatedUser.getUserId());

    if (!consultant.isPresent()) {
      LogService.logInternalServerError(
          String.format("Consultant with id %s not found while updating absent state.",
              authenticatedUser.getUserId()));
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    consultantService.updateConsultantAbsent(consultant.get(), absence);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Gets the user data for the current logged in user depending on his user role.
   *
   */
  @Override
  public ResponseEntity<UserDataResponseDTO> getUserData() {

    Set<String> roles = authenticatedUser.getRoles();

    UserDataResponseDTO responseDTO = null;

    if (roles.contains(UserRole.CONSULTANT.getValue())) {

      Optional<Consultant> consultant =
          consultantService.getConsultant(authenticatedUser.getUserId());

      if (!consultant.isPresent()) {
        LogService.logInternalServerError(
            String.format("Consultant with id %s not found while getting UserData.",
                authenticatedUser.getUserId()));
      } else {
        responseDTO = getUserDataFacade.getConsultantData(consultant.get());
      }

    } else if (roles.contains(UserRole.USER.getValue())) {

      Optional<User> user = userService.getUser(authenticatedUser.getUserId());

      if (!user.isPresent()) {
        LogService.logInternalServerError(String.format(
            "User with id %s not found while getting UserData.", authenticatedUser.getUserId()));
      } else {
        responseDTO = getUserDataFacade.getUserData(user.get());
      }

    } else {
      LogService.logInternalServerError(
          String.format("User with id %s has neither Consultant-Role, nor User-Role .",
              authenticatedUser.getUserId()));
    }

    responseDTO.setUserRoles(authenticatedUser.getRoles());
    responseDTO.setGrantedAuthorities(authenticatedUser.getGrantedAuthorities());

    return (responseDTO != null)
        ? new ResponseEntity<>(responseDTO, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Returns a list of sessions for the currently authenticated consultant depending on the
   * submitted sessionStatus
   */
  @Override
  public ResponseEntity<ConsultantSessionListResponseDTO> getSessionsForAuthenticatedConsultant(
      @RequestHeader String rcToken, @NotNull @Valid @RequestParam Integer offset,
      @NotNull @Valid @RequestParam Integer count, @Valid @NotEmpty @RequestParam String filter,
      @Valid @NotEmpty @RequestParam Integer status) {

    if (offset < MIN_OFFSET) {
      throw new BadRequestException("offset must be a positive number");
    }
    if (count < MIN_COUNT) {
      throw new BadRequestException("count must be a positive number");
    }

    Optional<Consultant> consultant =
        consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

    ConsultantSessionListResponseDTO sessionList = null;
    Optional<SessionFilter> optionalSessionFilter = SessionFilter.getByValue(filter);
    if (optionalSessionFilter.isPresent()) {
      sessionList = getSessionListFacade.getSessionsForAuthenticatedConsultant(consultant.get(),
          status, rcToken, offset, count, optionalSessionFilter.get());
    }

    return sessionList != null && isNotEmpty(sessionList.getSessions())
        ? new ResponseEntity<>(sessionList, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Returns a list of team consulting sessions for the currently authenticated consultant.
   */
  @Override
  public ResponseEntity<ConsultantSessionListResponseDTO> getTeamSessionsForAuthenticatedConsultant(
      @RequestHeader String rcToken, @NotNull @Valid @RequestParam Integer offset,
      @NotNull @Valid @RequestParam Integer count, @Valid @NotEmpty @RequestParam String filter) {

    if (offset < MIN_OFFSET) {
      throw new BadRequestException("offset must be a positive number");
    }
    if (count < MIN_COUNT) {
      throw new BadRequestException("count must be a positive number");
    }

    Optional<Consultant> consultant =
        consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

    if (!consultant.get().isTeamConsultant()) {
      LogService.logForbidden(String.format(
          "Consultant with id %s is no team consultant and therefor not allowed to get team sessions.",
          authenticatedUser.getUserId()));
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    ConsultantSessionListResponseDTO sessionList = null;
    Optional<SessionFilter> optionalSessionFilter = SessionFilter.getByValue(filter);
    if (optionalSessionFilter.isPresent()) {
      sessionList = getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(consultant.get(),
          rcToken, offset, count, optionalSessionFilter.get());
    }

    return sessionList != null && isNotEmpty(sessionList.getSessions())
        ? new ResponseEntity<>(sessionList, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Imports a file list of consultants. Technical user authorization required
   *
   */
  @Override
  public ResponseEntity<Void> importConsultants() {

    consultantImportService.startImport();

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Imports a file list of askers. Technical user authorization required.
   *
   */
  @Override
  public ResponseEntity<Void> importAskers() {

    askerImportService.startImport();

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Imports a file list of askers without a session. Technical user authorization required.
   *
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
   */
  @Override
  public ResponseEntity<Void> sendNewMessageNotification(
      @Valid @RequestBody NewMessageNotificationDTO newMessageNotificationDTO) {

    emailNotificationFacade.sendNewMessageNotification(newMessageNotificationDTO.getRcGroupId(),
        authenticatedUser.getRoles(), authenticatedUser.getUserId());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Sends email notifications to the user(s) if there has been a new feedback answer. Uses the
   * provided Keycloak authorization token for user verification (user role). This means that the
   * user that wrote the answer should also call this method.
   *
   */
  @Override
  public ResponseEntity<Void> sendNewFeedbackMessageNotification(
      @Valid @RequestBody NewMessageNotificationDTO newMessageNotificationDTO) {

    emailNotificationFacade.sendNewFeedbackMessageNotification(
        newMessageNotificationDTO.getRcGroupId(), authenticatedUser.getRoles(),
        authenticatedUser.getUserId());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Returns the monitoring for the given session
   *
   */
  @Override
  public ResponseEntity<MonitoringDTO> getMonitoring(
      @NotNull @Valid @PathVariable("sessionId") Long sessionId) {

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

    if (responseDTO != null && responseDTO.getProperties().size() > 0) {
      return new ResponseEntity<>(responseDTO, HttpStatus.OK);

    } else {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

  }

  /**
   * Updates the monitoring values of a {@link Session}. Only a consultant which is directly
   * assigned to the session can update the values (MVP only).
   */
  @Override
  public ResponseEntity<Void> updateMonitoring(@PathVariable("sessionId") Long sessionId,
      @Valid @RequestBody MonitoringDTO monitoring) {

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
   * Returns all consultants of the provided agency id as a list of {@link ConsultantResponseDTO}
   */
  @Override
  public ResponseEntity<List<ConsultantResponseDTO>> getConsultants(
      @Valid @NotNull @RequestParam Long agencyId) {

    List<ConsultantResponseDTO> consultants =
        consultantAgencyService.getConsultantsOfAgency(agencyId);

    return isNotEmpty(consultants)
        ? new ResponseEntity<>(consultants, HttpStatus.OK)
        : new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Assigns an session (the provided session id) to the provided consultant id.
   */
  @Override
  public ResponseEntity<Void> assignSession(
      @Valid @NotNull @PathVariable("sessionId") Long sessionId,
      @Valid @NotEmpty @PathVariable("consultantId") String consultantId) {

    // Check, if calling consultant exists
    consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

    Optional<Consultant> consultant = consultantService.getConsultant(consultantId);

    if (!consultant.isPresent()) {
      LogService.logInternalServerError(
          String.format("Consultant (to be assigned) with id %s not found.", consultantId));
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

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

    assignSessionFacade.assignSession(session.get(), consultant.get());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Changes the (Keycloak) password of a user.
   */
  @Override
  public ResponseEntity<Void> updatePassword(@Valid @RequestBody PasswordDTO passwordDTO) {

    // Check if old password is valid
    Optional<ResponseEntity<LoginResponseDTO>> loginResponse =
        keycloakService.loginUser(authenticatedUser.getUsername(), passwordDTO.getOldPassword());

    if (loginResponse.isPresent() && loginResponse.get().getStatusCode().equals(HttpStatus.OK)
        && keycloakService.logoutUser(loginResponse.get().getBody().getRefresh_token())) {

      // Change the user's (Keycloak) password
      if (keycloakService.changePassword(authenticatedUser.getUserId(),
          passwordDTO.getNewPassword())) {
        return new ResponseEntity<>(HttpStatus.OK);
      }
    }

    return new ResponseEntity<>(loginResponse.isPresent()
        && loginResponse.get().getStatusCode().equals(HttpStatus.BAD_REQUEST)
        ? HttpStatus.BAD_REQUEST
        : HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Updates the master key fragment for the en-/decryption of messages
   *
   * @param masterKey
   * @return
   */
  @Override
  public ResponseEntity<Void> updateKey(@Valid @RequestBody MasterKeyDTO masterKey) {
    if (!decryptionService.getMasterKey().equals(masterKey.getMasterKey())) {
      decryptionService.updateMasterKey(masterKey.getMasterKey());
      LogService.logInfo("MasterKey updated");
      return new ResponseEntity<>(HttpStatus.OK);
    }

    return new ResponseEntity<>(HttpStatus.CONFLICT);
  }

  /**
   * Creates a new chat with the given details and returns the generated chat link
   *
   * @param chatDTO {@link ChatDTO}
   * @return
   */
  @Override
  public ResponseEntity<CreateChatResponseDTO> createChat(@Valid @RequestBody ChatDTO chatDTO) {

    Optional<Consultant> callingConsultant =
        consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);
    if (!callingConsultant.isPresent()) {
      throw new InternalServerErrorException(String.format("No consultant for user %s exists",
          authenticatedUser.getUserId()));
    }

    // Create chat and return chat link
    CreateChatResponseDTO response = createChatFacade.createChat(chatDTO, callingConsultant.get());

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  /**
   * Start a chat
   */
  @Override
  public ResponseEntity<Void> startChat(@NotNull @Valid @PathVariable("chatId") Long chatId) {

    Optional<Consultant> callingConsultant =
        consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

    Optional<Chat> chat = chatService.getChat(chatId);
    if (!chat.isPresent()) {
      throw new BadRequestException(
          String.format("Chat with id %s not found for starting chat.", chatId));
    }

    startChatFacade.startChat(chat.get(), callingConsultant.get());

    return new ResponseEntity<>(HttpStatus.OK);

  }

  /**
   * Get chat info
   */
  @Override
  public ResponseEntity<ChatInfoResponseDTO> getChat(
      @NotNull @Valid @PathVariable("chatId") Long chatId) {

    ChatInfoResponseDTO response = getChatFacade.getChat(chatId, authenticatedUser);

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /**
   * Join a chat
   */
  @Override
  public ResponseEntity<Void> joinChat(@NotNull @Valid @PathVariable("chatId") Long chatId) {

    joinAndLeaveChatFacade.joinChat(chatId, authenticatedUser);

    return new ResponseEntity<>(HttpStatus.OK);

  }

  /**
   * Stops the given chat (chatId). Deletes all users and messages from the Rocket.Chat room
   * (repetitive chat) or deletes the whole room (singular chat).
   */
  @Override
  public ResponseEntity<Void> stopChat(@NotNull @Valid @PathVariable("chatId") Long chatId) {

    Optional<Consultant> callingConsultant =
        consultantService.getConsultantViaAuthenticatedUser(authenticatedUser);

    Optional<Chat> chat = chatService.getChat(chatId);
    if (!chat.isPresent()) {
      throw new BadRequestException(
          String.format("Chat with id %s not found while trying to stop the chat.", chatId));
    }

    stopChatFacade.stopChat(chat.get(), callingConsultant.get());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<ChatMembersResponseDTO> getChatMembers(
      @NotNull @Valid @PathVariable("chatId") Long chatId) {

    ChatMembersResponseDTO chatMembersResponseDTO =
        getChatMembersFacade.getChatMembers(chatId, authenticatedUser);

    return new ResponseEntity<>(chatMembersResponseDTO, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> leaveChat(@NotNull @Valid @PathVariable("chatId") Long chatId) {

    joinAndLeaveChatFacade.leaveChat(chatId, authenticatedUser);

    return new ResponseEntity<>(HttpStatus.OK);

  }

  /**
   * Updates the settings of the given {@link Chat}.
   *
   */
  @Override
  public ResponseEntity<UpdateChatResponseDTO> updateChat(
      @NotNull @Valid @PathVariable("chatId") Long chatId, @Valid @RequestBody ChatDTO chatDTO) {

    UpdateChatResponseDTO updateChatResponseDTO = chatService.updateChat(chatId, chatDTO,
        authenticatedUser);
    return new ResponseEntity<>(updateChatResponseDTO, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deleteUser(@Valid @RequestBody DeleteUserDTO deleteUserDTO) {

    deleteUserFacade.deleteUser(deleteUserDTO);

    return new ResponseEntity<>(HttpStatus.OK);
  }

}
