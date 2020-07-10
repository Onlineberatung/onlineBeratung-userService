package de.caritas.cob.UserService.api.facade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.authorization.Authority;
import de.caritas.cob.UserService.api.container.CreateEnquiryExceptionParameter;
import de.caritas.cob.UserService.api.exception.CheckForCorrectRocketChatUserException;
import de.caritas.cob.UserService.api.exception.CreateMonitoringException;
import de.caritas.cob.UserService.api.exception.EnquiryMessageException;
import de.caritas.cob.UserService.api.exception.InitializeFeedbackChatException;
import de.caritas.cob.UserService.api.exception.MessageHasAlreadyBeenSavedException;
import de.caritas.cob.UserService.api.exception.NoUserSessionException;
import de.caritas.cob.UserService.api.exception.SaveUserException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatAddConsultantsAndTechUserException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatAddUserToGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatCreateGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatDeleteGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatGetUserInfoException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatLoginException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatPostMessageException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatPostWelcomeMessageException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.UserService.api.helper.Helper;
import de.caritas.cob.UserService.api.helper.RocketChatHelper;
import de.caritas.cob.UserService.api.helper.UserHelper;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.UserService.api.model.rocketChat.group.GroupResponseDTO;
import de.caritas.cob.UserService.api.model.rocketChat.user.UserInfoResponseDTO;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.session.SessionStatus;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.service.ConsultantAgencyService;
import de.caritas.cob.UserService.api.service.LogService;
import de.caritas.cob.UserService.api.service.MonitoringService;
import de.caritas.cob.UserService.api.service.RocketChatService;
import de.caritas.cob.UserService.api.service.SessionService;
import de.caritas.cob.UserService.api.service.helper.KeycloakAdminClientHelper;
import de.caritas.cob.UserService.api.service.helper.MessageServiceHelper;

/*
 * Facade for capsuling the steps for saving the enquiry message.
 */
@Service
public class CreateEnquiryMessageFacade {

  @Value("${rocket.systemuser.id}")
  private String ROCKET_CHAT_SYSTEM_USER_ID;

  private final SessionService sessionService;
  private final RocketChatService rocketChatService;
  private final LogService logService;
  private final EmailNotificationFacade emailNotificationFacade;
  private final MessageServiceHelper messageServiceHelper;
  private final ConsultantAgencyService consultantAgencyService;
  private final MonitoringService monitoringService;
  private final ConsultingTypeManager consultingTypeManager;
  private final KeycloakAdminClientHelper keycloakAdminClientHelper;
  private final UserHelper userHelper;
  private final RocketChatHelper rocketChatHelper;

  /**
   * Constructor
   * 
   * @param sessionService
   * @param rocketChatService
   * @param logService
   * @param emailNotificationFacade
   * @param messageServiceHelper
   */
  @Autowired
  public CreateEnquiryMessageFacade(SessionService sessionService,
      RocketChatService rocketChatService, LogService logService,
      EmailNotificationFacade emailNotificationFacade, MessageServiceHelper messageServiceHelper,
      ConsultantAgencyService consultantAgencyService, MonitoringService monitoringService,
      ConsultingTypeManager consultingTypeManager, KeycloakAdminClientHelper keycloakHelper,
      UserHelper userHelper, RocketChatHelper rocketChatHelper) {
    this.sessionService = sessionService;
    this.rocketChatService = rocketChatService;
    this.logService = logService;
    this.emailNotificationFacade = emailNotificationFacade;
    this.messageServiceHelper = messageServiceHelper;
    this.consultantAgencyService = consultantAgencyService;
    this.monitoringService = monitoringService;
    this.consultingTypeManager = consultingTypeManager;
    this.keycloakAdminClientHelper = keycloakHelper;
    this.userHelper = userHelper;
    this.rocketChatHelper = rocketChatHelper;
  }

  /**
   * Handles possible exceptions and roll backs during the creation of the enquiry message for a
   * session.
   * 
   * @param user {@link User}
   * @param sessionId {@link Session#getId()}
   * @param message enquiry message
   * @param rcToken Rocket.Chat token
   * @param rcUserId Rocket.Chat user ID
   * @return true, if successful. false, if message already saved.
   */
  public HttpStatus createEnquiryMessage(User user, Long sessionId, String message, String rcToken,
      String rcUserId) {

    try {
      doCreateEnquiryMessageSteps(user, sessionId, message, rcToken, rcUserId);

    } catch (MessageHasAlreadyBeenSavedException messageHasAlreadyBeenSavedException) {
      logService.logCreateEnquiryMessageException(messageHasAlreadyBeenSavedException);

      return HttpStatus.CONFLICT;
    } catch (NoUserSessionException
        | CheckForCorrectRocketChatUserException checkForCorrectRocketChatUserException) {
      logService.logCreateEnquiryMessageException(checkForCorrectRocketChatUserException);

      return HttpStatus.BAD_REQUEST;
    } catch (RocketChatCreateGroupException | RocketChatGetUserInfoException exception) {
      logService.logCreateEnquiryMessageException(exception);

      return HttpStatus.INTERNAL_SERVER_ERROR;
    } catch (RocketChatAddConsultantsAndTechUserException | CreateMonitoringException
        | RocketChatPostMessageException | RocketChatPostWelcomeMessageException
        | EnquiryMessageException | InitializeFeedbackChatException exception) {
      logService.logCreateEnquiryMessageException(exception);
      doRollback(exception.getExceptionParameter(), rcUserId, rcToken);

      return HttpStatus.INTERNAL_SERVER_ERROR;
    } catch (RocketChatLoginException | ServiceException | SaveUserException exception) {
      // Presumably only the Rocket.Chat group was created yet
      logService.logCreateEnquiryMessageException(exception);
      Optional<Session> session = sessionService.getSession(sessionId);
      CreateEnquiryExceptionParameter exceptionParameter =
          CreateEnquiryExceptionParameter.builder().rcGroupId(session.get().getGroupId()).build();
      doRollback(exceptionParameter, rcUserId, rcToken);

      return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    return HttpStatus.CREATED;
  }

  /**
   * Creates the private Rocket.Chat group, initializes the session monitoring and saves the enquiry
   * message in Rocket.Chat.
   * 
   * @param user {@link User}
   * @param sessionId {@link Session#getId()}
   * @param message Message
   * @param rcToken Rocket.Chat token
   * @param rcUserId Rocket.Chat user ID
   * @throws RocketChatAddConsultantsAndTechUserException
   * @throws CreateMonitoringException
   * @throws RocketChatPostMessageException
   * @throws RocketChatPostWelcomeMessageException
   * @throws EnquiryMessageException
   * @throws InitializeFeedbackChatException
   */
  private void doCreateEnquiryMessageSteps(User user, Long sessionId, String message,
      String rcToken, String rcUserId)
      throws RocketChatAddConsultantsAndTechUserException, CreateMonitoringException,
      RocketChatPostMessageException, RocketChatPostWelcomeMessageException,
      EnquiryMessageException, InitializeFeedbackChatException, ServiceException {

    Session session = getSessionForEnquiryMessage(sessionId, user);

    // Check if Keycloak and Rocket.Chat users match (e.g. multiple opened tabs)
    checkForCorrectRocketChatUser(rcUserId, user);

    // Create Rocket.Chat group and update the user's Rocket.Chat ID
    GroupResponseDTO rcGroupDTO = createRocketChatGroupForSession(session, rcToken, rcUserId);
    userHelper.updateRocketChatIdInDatabase(user, rcGroupDTO.getGroup().getUser().getId());

    List<ConsultantAgency> agencyList =
        consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());

    // Add technical user and all consultants of related agency to Rocket.Chat group
    addConsultantsAndTechUserToGroup(rcGroupDTO.getGroup().getId(), rcUserId, rcToken, agencyList);

    // Create an initial monitoring data set for the session
    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultantTypeSettings(session.getConsultingType());
    monitoringService.createMonitoring(session, consultingTypeSettings);

    // Post enquiry message to Rocket.Chat group
    CreateEnquiryExceptionParameter createEnquiryExceptionParameter =
        CreateEnquiryExceptionParameter.builder().session(session)
            .rcGroupId(rcGroupDTO.getGroup().getId()).build();
    messageServiceHelper.postMessage(message, rcUserId, rcToken, rcGroupDTO.getGroup().getId(),
        createEnquiryExceptionParameter);

    // Update message date and R.C group ID for session
    sessionService.saveEnquiryMessageDateAndRocketChatGroupId(session,
        rcGroupDTO.getGroup().getId());

    // Send welcome message (if given/set)
    messageServiceHelper.postWelcomeMessage(rcGroupDTO.getGroup().getId(), user,
        consultingTypeSettings, createEnquiryExceptionParameter);

    // Create feedback chat group and add (peer) consultants and system user (if given/set)
    initializeFeedbackChat(session, rcGroupDTO.getGroup().getId(), rcUserId, rcToken, agencyList,
        consultingTypeSettings);

    // Send e-mail notifications
    emailNotificationFacade.sendNewEnquiryEmailNotification(session);
  }

  /**
   * Returns the {@link Session} for the given session ID.
   * 
   * Throws {@link NoUserSessionException} when no session is found for the given ID. Throws
   * {@link MessageHasAlreadyBeenSavedException} when an enquiry message has already been written.
   * 
   * @param sessionId {@link Session#getId()}
   * @param user {@link User}
   * @return {@link Session}
   * @throws {@link NoUserSessionException}
   * @throws MessageHasAlreadyBeenSavedException
   */
  private Session getSessionForEnquiryMessage(Long sessionId, User user)
      throws NoUserSessionException, MessageHasAlreadyBeenSavedException {

    Optional<Session> session = null;

    session = sessionService.getSession(sessionId);

    if (!session.isPresent() || !session.get().getUser().getUserId().equals(user.getUserId())) {
      throw new NoUserSessionException(
          String.format("Session %s not found for user %s", sessionId, user.getUserId()));
    }

    if (session.get().getEnquiryMessageDate() != null) {
      throw new MessageHasAlreadyBeenSavedException(
          String.format("Enquiry message already written for session %s", sessionId));
    }

    return session.get();
  }

  /**
   * Checks if the given Keycloak and Rocket.Chat user are the same.
   * 
   * @param rcUserId Rocket.Chat user ID
   * @param user {@link User}
   * @throws CheckForCorrectRocketChatUserException
   */
  private void checkForCorrectRocketChatUser(String rcUserId, User user)
      throws CheckForCorrectRocketChatUserException {

    UserInfoResponseDTO rcUserInfoDTO = rocketChatService.getUserInfo(rcUserId);
    if (!userHelper.doUsernamesMatch(rcUserInfoDTO.getUser().getUsername(), user.getUsername())) {
      throw new CheckForCorrectRocketChatUserException(String.format(
          "Enquiry message check: User with username %s does not match user with Rocket.Chat ID %s",
          user.getUsername(), rcUserId));
    }
  }

  /**
   * Creates the private Rocket.Chat room for the given {@link Session}. Throws a
   * {@link RocketChatCreateGroupException} if no group is being returned by Rocket.Chat.
   * 
   * @param session {@link Session}
   * @param rcToken
   * @param rcUserId
   * @return {@link GroupResponseDTO}
   * @throws RocketChatCreateGroupException
   */
  private GroupResponseDTO createRocketChatGroupForSession(Session session, String rcToken,
      String rcUserId) throws RocketChatCreateGroupException {

    Optional<GroupResponseDTO> rcGroupDTO = rocketChatService
        .createPrivateGroup(rocketChatHelper.generateGroupName(session), rcToken, rcUserId);

    if (!rcGroupDTO.isPresent()) {
      throw new RocketChatCreateGroupException(
          String.format("Could not create Rocket.Chat room for session %s and Rocket.Chat user %s",
              session.getId(), rcUserId));
    }

    return rcGroupDTO.get();
  }

  /**
   * Initializes the Rocket.Chat feedback chat group for a session (Create feedback chat group, add
   * (peer) consultants and system user).
   * 
   * @param session {@link Session}
   * @param rcGroupId Rocket.Chat group ID
   * @param rcUserId Rocket.Chat user ID
   * @param rcToken Rocket.Chat token
   * @param agencyList {@link List} of {@link ConsultantAgency}
   * @param consultingTypeSettings {@link ConsultingTypeSettings}
   * @throws InitializeFeedbackChatException
   */
  private void initializeFeedbackChat(Session session, String rcGroupId, String rcUserId,
      String rcToken, List<ConsultantAgency> agencyList,
      ConsultingTypeSettings consultingTypeSettings) throws InitializeFeedbackChatException {

    if (!consultingTypeSettings.isFeedbackChat()) {
      return;
    }

    String rcFeedbackGroupId = null;
    Optional<GroupResponseDTO> rcFeedbackGroupDTO = Optional.empty();
    CreateEnquiryExceptionParameter exceptionWithoutFeedbackId =
        CreateEnquiryExceptionParameter.builder().session(session).rcGroupId(rcGroupId).build();

    try {
      rcFeedbackGroupDTO = rocketChatService
          .createPrivateGroupWithSystemUser(rocketChatHelper.generateFeedbackGroupName(session));

      if (rcFeedbackGroupDTO.isPresent() && rcFeedbackGroupDTO.get().getGroup().getId() != null) {
        throw new InitializeFeedbackChatException(
            String.format("Could not create feedback chat group for session %s", session.getId()),
            exceptionWithoutFeedbackId);
      }
      rcFeedbackGroupId = rcFeedbackGroupDTO.get().getGroup().getId();

      // Add RocketChat user for system message to group
      rocketChatService.addUserToGroup(ROCKET_CHAT_SYSTEM_USER_ID, rcFeedbackGroupId);

      // Add all consultants of the session's agency to the feedback group that have the right
      // to view all feedback sessions
      for (ConsultantAgency agency : agencyList) {
        if (keycloakAdminClientHelper.userHasAuthority(agency.getConsultant().getId(),
            Authority.VIEW_ALL_FEEDBACK_SESSIONS)) {
          rocketChatService.addUserToGroup(agency.getConsultant().getRocketChatId(),
              rcFeedbackGroupId);
        }
      }

      // Remove all system messages
      rocketChatService.removeSystemMessages(rcFeedbackGroupId,
          LocalDateTime.now().minusHours(Helper.ONE_DAY_IN_HOURS), LocalDateTime.now());

      // Update the session's feedback group id
      sessionService.updateFeedbackGroupId(Optional.of(session), rcFeedbackGroupId);

    } catch (RocketChatCreateGroupException rocketChatCreateGroupException) {
      throw new InitializeFeedbackChatException(
          String.format("Could not create feedback chat group for session %s", session.getId()),
          exceptionWithoutFeedbackId);

    } catch (RocketChatAddUserToGroupException | RocketChatRemoveSystemMessagesException
        | RocketChatLoginException | UpdateFeedbackGroupIdException exception) {
      CreateEnquiryExceptionParameter exceptionWithFeedbackId =
          CreateEnquiryExceptionParameter.builder().session(session).rcGroupId(rcGroupId)
              .rcFeedbackGroupId(rcFeedbackGroupId).build();
      throw new InitializeFeedbackChatException(
          String.format("Could not create feedback chat group for session %s", session.getId()),
          exceptionWithFeedbackId);
    }
  }

  /**
   * Adds the consultant of the provided list of {@link ConsultantAgency} to the provided
   * Rocket.Chat group ID.
   * 
   * @param rcGroupId Rocket.Chat group ID
   * @param rcUserId Rocket.Chat user ID
   * @param rcToken Rocket.Chat token
   * @param agencyList {@link List} of {@link ConsultantAgency}
   * @throws RocketChatAddConsultantsAndTechUserException
   */
  private void addConsultantsAndTechUserToGroup(String rcGroupId, String rcUserId, String rcToken,
      List<ConsultantAgency> agencyList) throws RocketChatAddConsultantsAndTechUserException {

    try {
      // Add RocketChat user for system message to group
      rocketChatService.addUserToGroup(ROCKET_CHAT_SYSTEM_USER_ID, rcGroupId);

      if (agencyList != null) {
        for (ConsultantAgency agency : agencyList) {
          rocketChatService.addUserToGroup(agency.getConsultant().getRocketChatId(), rcGroupId);
        }
      }

    } catch (RocketChatAddUserToGroupException rocketChatAddUserToGroupException) {
      throw new RocketChatAddConsultantsAndTechUserException(
          String.format("Add consultants and tech user error: Could not add user to group %s",
              rcGroupId),
          rocketChatAddUserToGroupException,
          CreateEnquiryExceptionParameter.builder().rcGroupId(rcGroupId).build());
    }
  }

  /**
   * Performs a rollback depending on the given parameter values (creation of Rocket.Chat groups and
   * changes/initialization of session).
   * 
   * @param createEnquiryExceptionParameter {@link CreateEnquiryExceptionParameter}
   * @param rcUserId Rocket.Chat user ID
   * @param rcToken Rocket.Chat token
   */
  private void doRollback(CreateEnquiryExceptionParameter createEnquiryExceptionParameter,
      String rcUserId, String rcToken) {

    if (createEnquiryExceptionParameter == null) {
      return;
    }

    if (createEnquiryExceptionParameter.getRcGroupId() != null) {
      rollbackCreateGroup(createEnquiryExceptionParameter.getRcGroupId(), rcToken, rcUserId);
    }
    if (createEnquiryExceptionParameter.getSession() != null) {
      rollbackInitializeMonitoring(createEnquiryExceptionParameter.getSession());
      if (createEnquiryExceptionParameter.getRcFeedbackGroupId() != null) {
        rollbackCreateGroup(createEnquiryExceptionParameter.getRcFeedbackGroupId(), rcToken,
            rcUserId);
        rollbackSession(createEnquiryExceptionParameter.getSession());
      }
    }
  }

  /**
   * Roll back the creation of the Rocket.Chat group
   * 
   * @param rcGroupId Rocket.Chat group ID
   * @param rcToken Rocket.Chat token
   * @param rcUserId Rocket.Chat user ID
   */
  private void rollbackCreateGroup(String rcGroupId, String rcToken, String rcUserId) {
    if (rcGroupId != null) {
      try {
        if (!rocketChatService.deleteGroup(rcGroupId, rcToken, rcUserId)) {
          logService.logInternalServerError(String.format(
              "Error during rollback while saving enquiry message. Group with id %s could not be deleted.",
              rcGroupId));
        }

      } catch (RocketChatDeleteGroupException rocketChatDeleteGroupException) {
        logService.logInternalServerError(String.format(
            "Error during rollback while saving enquiry message. Group with id %s could not be deleted.",
            rcGroupId), rocketChatDeleteGroupException);
      }
    }
  }

  /**
   * Roll back the initialization of the monitoring data for a {@link Session}
   * 
   * @param session {@link Session}
   */
  private void rollbackInitializeMonitoring(Session session) {
    if (session != null) {
      try {
        monitoringService.deleteInitialMonitoring(session);

      } catch (ServiceException ex) {
        logService.logInternalServerError(String.format(
            "Error during rollback while saving enquiry message. Monitoring data for session with id %s could not be deleted.",
            session.getId()), ex);
      }
    }

  }

  /**
   * Roll back the session changes
   * 
   * @param session {@link Session}
   */
  private void rollbackSession(Session session) {
    if (session != null) {

      try {
        session.setEnquiryMessageDate(null);
        session.setStatus(SessionStatus.INITIAL);
        session.setGroupId(null);
        session.setFeedbackGroupId(null);
        sessionService.saveSession(session);
      } catch (ServiceException ex) {
        logService.logInternalServerError(String.format(
            "Error during rollback while saving session. Session data could not be set to state before createEnquiryMessageFacade.",
            session.getId()), ex);
      }
    }
  }
}
