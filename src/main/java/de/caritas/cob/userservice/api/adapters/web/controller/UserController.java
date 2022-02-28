package de.caritas.cob.userservice.api.adapters.web.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import de.caritas.cob.userservice.api.actions.registry.ActionsRegistry;
import de.caritas.cob.userservice.api.actions.user.DeactivateKeycloakUserActionCommand;
import de.caritas.cob.userservice.api.adapters.web.controller.validation.MinValue;
import de.caritas.cob.userservice.api.adapters.web.dto.AbsenceDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ChatDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ChatInfoResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ChatMembersResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateChatResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateEnquiryMessageResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.DeleteUserAccountDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EmailDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.EnquiryMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.LanguageResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.MasterKeyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.MobileTokenDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.MonitoringDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NewMessageNotificationDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.NewRegistrationDto;
import de.caritas.cob.userservice.api.adapters.web.dto.NewRegistrationResponseDto;
import de.caritas.cob.userservice.api.adapters.web.dto.OneTimePasswordDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.PasswordDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.PatchUserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionDataDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.TwoFactorAuthDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateChatResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDataResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.update.ConsultantUpdateService;
import de.caritas.cob.userservice.api.config.auth.Authority.AuthorityValue;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.container.SessionListQueryParameter;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.NotFoundException;
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
import de.caritas.cob.userservice.api.facade.assignsession.AssignEnquiryFacade;
import de.caritas.cob.userservice.api.facade.assignsession.AssignSessionFacade;
import de.caritas.cob.userservice.api.facade.sessionlist.SessionListFacade;
import de.caritas.cob.userservice.api.facade.userdata.AskerDataProvider;
import de.caritas.cob.userservice.api.facade.userdata.ConsultantDataFacade;
import de.caritas.cob.userservice.api.facade.userdata.ConsultantDataProvider;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.AuthenticatedUserHelper;
import de.caritas.cob.userservice.api.port.in.AccountManaging;
import de.caritas.cob.userservice.api.port.in.IdentityManaging;
import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import de.caritas.cob.userservice.api.model.Chat;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.service.session.SessionFilter;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.AskerImportService;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.ConsultantImportService;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.DecryptionService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.SessionDataService;
import de.caritas.cob.userservice.api.service.archive.SessionArchiveService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.ValidatedUserAccountProvider;
import de.caritas.cob.userservice.api.workflow.delete.action.asker.DeleteSingleRoomAndSessionAction;
import de.caritas.cob.userservice.api.workflow.delete.model.SessionDeletionWorkflowDTO;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UsersApi;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.InternalServerErrorException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "user-controller")
public class UserController implements UsersApi {

  static final int MIN_OFFSET = 0;
  static final int MIN_COUNT = 1;
  static final String OFFSET_INVALID_MESSAGE = "offset must be a positive number";
  static final String COUNT_INVALID_MESSAGE = "count must be a positive number";

  private final @NotNull ValidatedUserAccountProvider userAccountProvider;
  private final @NotNull SessionService sessionService;
  private final @NotNull AuthenticatedUser authenticatedUser;
  private final @NotNull CreateEnquiryMessageFacade createEnquiryMessageFacade;
  private final @NotNull ConsultantImportService consultantImportService;
  private final @NotNull EmailNotificationFacade emailNotificationFacade;
  private final @NotNull MonitoringService monitoringService;
  private final @NotNull AskerImportService askerImportService;
  private final @NotNull SessionListFacade sessionListFacade;
  private final @NotNull ConsultantAgencyService consultantAgencyService;
  private final @NotNull AssignSessionFacade assignSessionFacade;
  private final @NotNull AssignEnquiryFacade assignEnquiryFacade;
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
  private final @NotNull SessionArchiveService sessionArchiveService;
  private final @NonNull IdentityClientConfig identityClientConfig;
  private final @NonNull IdentityManaging identityManager;
  private final @NonNull AccountManaging accountManager;
  private final @NotNull ActionsRegistry actionsRegistry;
  private final @NonNull ConsultantDtoMapper consultantDtoMapper;
  private final @NonNull UserDtoMapper userDtoMapper;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull ConsultantUpdateService consultantUpdateService;
  private final @NonNull ConsultantDataProvider consultantDataProvider;
  private final @NonNull AskerDataProvider askerDataProvider;

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

    var user = this.userAccountProvider.retrieveValidatedUser();
    var rocketChatCredentials = RocketChatCredentials.builder()
        .rocketChatToken(rcToken)
        .rocketChatUserId(rcUserId)
        .build();

    var registrationResponse = createNewConsultingTypeFacade
        .initializeNewConsultingType(newRegistrationDto, user, rocketChatCredentials);

    return new ResponseEntity<>(registrationResponse, registrationResponse.getStatus());
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
    var session = sessionService.getSession(sessionId);

    if (session.isEmpty() || isNull(session.get().getGroupId())) {
      log.error("Internal Server Error: Session id {} is invalid, session not found or has no "
          + "Rocket.Chat groupId assigned.", sessionId);

      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    var consultant = this.userAccountProvider.retrieveValidatedConsultant();
    this.assignEnquiryFacade.assignRegisteredEnquiry(session.get(), consultant);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * @param sessionId      Session Id (required)
   * @param rcToken        Rocket.Chat token (required)
   * @param rcUserId       Rocket.Chat user ID (required)
   * @param enquiryMessage Enquiry message (required)
   * @return {@link ResponseEntity} containing {@link CreateEnquiryMessageResponseDTO}
   */
  @Override
  public ResponseEntity<CreateEnquiryMessageResponseDTO> createEnquiryMessage(
      @PathVariable Long sessionId,
      @RequestHeader String rcToken, @RequestHeader String rcUserId,
      @RequestBody EnquiryMessageDTO enquiryMessage) {

    var user = this.userAccountProvider.retrieveValidatedUser();
    var rocketChatCredentials = RocketChatCredentials.builder()
        .rocketChatToken(rcToken)
        .rocketChatUserId(rcUserId)
        .build();
    var language = consultantDtoMapper.languageOf(enquiryMessage.getLanguage());

    var response = createEnquiryMessageFacade.createEnquiryMessage(
        user, sessionId, enquiryMessage.getMessage(), language, rocketChatCredentials
    );

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Void> deleteSessionAndInactiveUser(@PathVariable Long sessionId) {
    var session = sessionService.getSession(sessionId)
        .orElseThrow(() -> new NotFoundException(
            String.format("A session with an id %s does not exist.", sessionId)));

    var user = session.getUser();
    if (user.getSessions().size() == 1) {
      actionsRegistry.buildContainerForType(User.class)
          .addActionToExecute(DeactivateKeycloakUserActionCommand.class)
          .executeActions(user);
    }

    var deleteSession = new SessionDeletionWorkflowDTO(session, null);
    actionsRegistry.buildContainerForType(SessionDeletionWorkflowDTO.class)
        .addActionToExecute(DeleteSingleRoomAndSessionAction.class)
        .executeActions(deleteSession);

    return new ResponseEntity<>(HttpStatus.OK);
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

    var user = this.userAccountProvider.retrieveValidatedUser();
    var rocketChatCredentials = RocketChatCredentials.builder()
        .rocketChatUserId(user.getRcUserId())
        .rocketChatToken(rcToken)
        .build();

    var userSessionsDTO = sessionListFacade
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
    var consultant = userAccountProvider.retrieveValidatedConsultant();
    this.consultantDataFacade.updateConsultantAbsent(consultant, absence);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Gets the user data for the current logged-in user depending on his user role.
   *
   * @return {@link ResponseEntity} containing {@link UserDataResponseDTO}
   */
  @Override
  public ResponseEntity<UserDataResponseDTO> getUserData() {
    var userDataResponseDTO = authenticatedUser.isConsultant()
        ? consultantDataProvider.retrieveData(userAccountProvider.retrieveValidatedConsultant())
        : askerDataProvider.retrieveData(userAccountProvider.retrieveValidatedUser());

    var isOtpEnabled = authenticatedUser.isUser() && identityClientConfig.getOtpAllowedForUsers()
        || authenticatedUser.isConsultant() && identityClientConfig.getOtpAllowedForConsultants();

    TwoFactorAuthDTO twoFactorAuthDTO;
    var encourage2fa = userDataResponseDTO.getEncourage2fa();
    if (isOtpEnabled) {
      var username = authenticatedUser.getUsername();
      var otpInfoDTO = identityManager.getOtpCredential(username);
      twoFactorAuthDTO = userDtoMapper.twoFactorAuthDtoOf(otpInfoDTO, encourage2fa);
    } else {
      twoFactorAuthDTO = userDtoMapper.twoFactorAuthDtoOf(encourage2fa);
    }
    userDataResponseDTO.setTwoFactorAuth(twoFactorAuthDTO);

    return new ResponseEntity<>(userDataResponseDTO, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> patchUser(PatchUserDTO patchUserDTO) {
    var patchMap = userDtoMapper.mapOf(patchUserDTO, authenticatedUser);
    accountManager.patchUser(patchMap).orElseThrow();

    return ResponseEntity.noContent().build();
  }

  /**
   * Updates the data for the current logged in consultant.
   *
   * @param updateConsultantDTO (required) the request {@link UpdateConsultantDTO}
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> updateConsultantData(UpdateConsultantDTO updateConsultantDTO) {
    var consultantId = authenticatedUser.getUserId();
    var consultant = consultantService.getConsultant(consultantId)
        .orElseThrow(() ->
            new NotFoundException(String.format("Consultant with id %s not found", consultantId))
        );

    var updateAdminConsultantDTO = consultantDtoMapper.updateAdminConsultantOf(
        updateConsultantDTO, consultant
    );
    consultantUpdateService.updateConsultant(consultantId, updateAdminConsultantDTO);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<LanguageResponseDTO> getLanguages(Long agencyId) {
    var languageCodes = consultantAgencyService.getLanguageCodesOfAgency(agencyId);
    var languageResponseDTO = consultantDtoMapper.languageResponseDtoOf(languageCodes);

    return new ResponseEntity<>(languageResponseDTO, HttpStatus.OK);
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

    var consultant = this.userAccountProvider.retrieveValidatedConsultant();

    ConsultantSessionListResponseDTO consultantSessionListResponseDTO = null;
    var optionalSessionFilter = SessionFilter.getByValue(filter);
    if (optionalSessionFilter.isPresent()) {

      var sessionListQueryParameter = SessionListQueryParameter.builder()
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

    var consultant = this.userAccountProvider.retrieveValidatedTeamConsultant();

    ConsultantSessionListResponseDTO teamSessionListDTO = null;
    var optionalSessionFilter = SessionFilter.getByValue(filter);
    if (optionalSessionFilter.isPresent()) {

      var sessionListQueryParameter = SessionListQueryParameter.builder()
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
    var session = sessionService.getSession(sessionId);
    if (session.isEmpty()) {
      log.warn("Bad request: Session with id {} not found", sessionId);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Check if consultant has the right to access the session
    if (!authenticatedUserHelper.hasPermissionForSession(session.get())) {
      log.warn("Bad request: Consultant with id {} has no permission to access session with id {}",
          authenticatedUser.getUserId(), sessionId);

      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    var responseDTO = monitoringService.getMonitoring(session.get());

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

    var session = sessionService.getSession(sessionId);

    if (session.isPresent()) {

      // Check if calling consultant has the permission to update the monitoring values
      if (authenticatedUserHelper.hasPermissionForSession(session.get())) {
        monitoringService.updateMonitoring(session.get().getId(), monitoring);
        return new ResponseEntity<>(HttpStatus.OK);

      } else {
        log.warn(
            "Unauthorized: Consultant with id {} is not authorized to update monitoring of session {}",
            authenticatedUser.getUserId(), sessionId
        );
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
      }

    } else {
      log.warn("Bad request: Session with id {} not found", sessionId);

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

    var consultants = consultantAgencyService.getConsultantsOfAgency(agencyId);

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

    var session = sessionService.getSession(sessionId);
    if (session.isEmpty()) {
      log.error("Internal Server Error: Session with id {} not found.", sessionId);

      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Check if the calling consultant has the correct right to assign the enquiry to a consultant
    if (session.get().getStatus().equals(SessionStatus.NEW) && !authenticatedUser
        .getGrantedAuthorities().contains(AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY)) {
      LogService.logForbidden(String.format(
          "The calling consultant with id %s does not have the authority to assign the enquiry to a consultant.",
          authenticatedUser.getUserId()));

      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    var consultant = this.userAccountProvider.retrieveValidatedConsultantById(consultantId);
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

    var callingConsultant = this.userAccountProvider.retrieveValidatedConsultant();
    var response = createChatFacade.createChat(chatDTO, callingConsultant);

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

    var chat = chatService.getChat(chatId)
        .orElseThrow(() -> new BadRequestException(
            String.format("Chat with id %s not found for starting chat.", chatId)));

    var callingConsultant = this.userAccountProvider.retrieveValidatedConsultant();
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

    var response = getChatFacade.getChat(chatId);

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

    var chat = chatService.getChat(chatId)
        .orElseThrow(() -> new BadRequestException(
            String.format("Chat with id %s not found while trying to stop the chat.", chatId)));

    var callingConsultant = this.userAccountProvider.retrieveValidatedConsultant();
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

    var chatMembersResponseDTO = getChatMembersFacade.getChatMembers(chatId);

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

    var updateChatResponseDTO = chatService.updateChat(chatId, chatDTO,
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

    var consultant = this.userAccountProvider.retrieveValidatedConsultant();
    var consultantSessionDTO = sessionService
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
    userAccountProvider.changeUserAccountEmailAddress(
        Optional.of(emailAddress)
    );

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Sets the user's email address to its default.
   *
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> deleteEmailAddress() {
    userAccountProvider.changeUserAccountEmailAddress(
        Optional.empty()
    );

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
   * Adds a mobile client token for the current authenticated user.
   *
   * @param mobileTokenDTO (required) the mobile device identifier {@link MobileTokenDTO}
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> addMobileAppToken(@Valid MobileTokenDTO mobileTokenDTO) {
    this.userAccountProvider.addMobileAppToken(mobileTokenDTO.getToken());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Updates the session data for the given session.
   *
   * @param sessionId      (required) session ID
   * @param sessionDataDTO (required) {@link SessionDataDTO}
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> updateSessionData(@PathVariable Long sessionId,
      @Valid SessionDataDTO sessionDataDTO) {
    this.sessionDataService.saveSessionData(sessionId, sessionDataDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Put a session into the archive.
   *
   * @param sessionId (required) session ID
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> archiveSession(@PathVariable Long sessionId) {
    this.sessionArchiveService.archiveSession(sessionId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Dearchive a session.
   *
   * @param sessionId (required) session ID
   * @return {@link ResponseEntity}
   */
  @Override
  public ResponseEntity<Void> dearchiveSession(@PathVariable Long sessionId) {
    this.sessionArchiveService.dearchiveSession(sessionId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> startTwoFactorAuthByEmailSetup(EmailDTO emailDTO) {
    var username = authenticatedUser.getUsername();
    var email = emailDTO.getEmail();

    if (!identityManager.isEmailAvailableOrOwn(username, email)) {
      return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    identityManager.setUpOneTimePassword(username, email).ifPresent(message -> {
      throw new InternalServerErrorException(message);
    });

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> finishTwoFactorAuthByEmailSetup(String tan) {
    var username = authenticatedUser.getUsername();
    var validationResult = identityManager.validateOneTimePassword(username, tan);

    if (Boolean.parseBoolean(validationResult.get("created"))) {
      var patchMap = userDtoMapper.mapOf(validationResult.get("email"), authenticatedUser);
      accountManager.patchUser(patchMap).orElseThrow();
      return ResponseEntity.noContent().build();
    }
    if (Boolean.parseBoolean(validationResult.get("attemptsLeft"))) {
      return ResponseEntity.badRequest().build();
    }
    if (Boolean.parseBoolean(validationResult.get("createdBefore"))) {
      return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
    }

    return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
  }

  /**
   * Activates 2FA by mobile app for the calling user.
   *
   * @param oneTimePasswordDTO (required) {@link OneTimePasswordDTO}
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> activateTwoFactorAuthByApp(OneTimePasswordDTO oneTimePasswordDTO) {
    if (authenticatedUser.isUser() && !identityClientConfig.getOtpAllowedForUsers()) {
      throw new ConflictException("2FA is disabled for user role");
    }
    if (authenticatedUser.isConsultant() && !identityClientConfig.getOtpAllowedForConsultants()) {
      throw new ConflictException("2FA is disabled for consultant role");
    }

    var isValid = identityManager.setUpOneTimePassword(
        authenticatedUser.getUsername(),
        oneTimePasswordDTO.getOtp(),
        oneTimePasswordDTO.getSecret()
    );

    return isValid ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
  }

  @Override
  public ResponseEntity<Void> activateTwoFactorAuthByAppDeprecated(
      OneTimePasswordDTO oneTimePasswordDTO) {
    return activateTwoFactorAuthByApp(oneTimePasswordDTO);
  }

  /**
   * Deactivates 2FA by mobile app for the calling user.
   *
   * @return {@link ResponseEntity} containing {@link HttpStatus}
   */
  @Override
  public ResponseEntity<Void> deactivateTwoFactorAuthByApp() {
    identityManager.deleteOneTimePassword(authenticatedUser.getUsername());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deactivateTwoFactorAuthByAppDeprecated() {
    return deactivateTwoFactorAuthByApp();
  }

  /**
   * Returns all agencies of given consultant.
   *
   * @param consultantId Consultant Id (required)
   * @return {@link ResponseEntity} containing all agencies of consultant
   */
  @Override
  public ResponseEntity<ConsultantResponseDTO> getConsultantPublicData(String consultantId) {
    var consultant = consultantService.getConsultant(consultantId)
        .orElseThrow(() ->
            new NotFoundException(String.format("Consultant with id %s not found", consultantId))
        );
    var agencies = consultantAgencyService.getAgenciesOfConsultant(consultantId);
    var consultantDto = consultantDtoMapper.consultantResponseDtoOf(consultant, agencies, false);

    return new ResponseEntity<>(consultantDto, HttpStatus.OK);
  }
}
