package de.caritas.cob.UserService.api.facade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.authorization.Authority;
import de.caritas.cob.UserService.api.exception.EnquiryMessageException;
import de.caritas.cob.UserService.api.exception.InitializeMonitoringException;
import de.caritas.cob.UserService.api.exception.MessageHasAlreadyBeenSavedException;
import de.caritas.cob.UserService.api.exception.MessageServiceHelperException;
import de.caritas.cob.UserService.api.exception.NoUserSessionException;
import de.caritas.cob.UserService.api.exception.SaveUserException;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatAddUserToGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatCreateGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatDeleteGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatGetUserInfoException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatLoginException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.UserService.api.helper.Helper;
import de.caritas.cob.UserService.api.helper.MessageHelper;
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
import de.caritas.cob.UserService.api.service.UserService;
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
  private final UserService userService;
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
      UserService userService, ConsultingTypeManager consultingTypeManager,
      KeycloakAdminClientHelper keycloakHelper, UserHelper userHelper,
      RocketChatHelper rocketChatHelper) {
    this.sessionService = sessionService;
    this.rocketChatService = rocketChatService;
    this.logService = logService;
    this.emailNotificationFacade = emailNotificationFacade;
    this.messageServiceHelper = messageServiceHelper;
    this.consultantAgencyService = consultantAgencyService;
    this.monitoringService = monitoringService;
    this.userService = userService;
    this.consultingTypeManager = consultingTypeManager;
    this.keycloakAdminClientHelper = keycloakHelper;
    this.userHelper = userHelper;
    this.rocketChatHelper = rocketChatHelper;
  }

  /**
   * Creation of the private Rocket.Chat group and saving of the enquiry message in the database
   * (session) and the Rocket.Chat group.
   * 
   * @param user
   * @param message
   * @param rcToken
   * @param rcUserId
   * @return true, if successful. false, if message already saved.
   */
  public HttpStatus createEnquiryMessage(User user, String message, String rcToken,
      String rcUserId) {

    Optional<GroupResponseDTO> rcGroupDTO;
    Optional<String> rcGroupId = Optional.empty();
    Session session = null;

    try {

      List<Session> userSessions = sessionService.getSessionsForUser(user);

      if (userSessions == null || userSessions.isEmpty()) {
        throw new NoUserSessionException(
            String.format("No sessions for user %s found.", user.getUserId()));
      }
      // Only 1 user session is currently possible
      session = userSessions.get(0);
      ConsultingTypeSettings consultingTypeSettings =
          consultingTypeManager.getConsultantTypeSettings(session.getConsultingType());

      if (session.getEnquiryMessageDate() != null) {
        // Enquiry message must not be updated
        throw new MessageHasAlreadyBeenSavedException();
      }

      // Parallel enquiry message check: do Keycloak user and given Rocket.Chat user match
      UserInfoResponseDTO rcUserInfoDTO = rocketChatService.getUserInfo(rcUserId);
      if (!userHelper.doUsernamesMatch(rcUserInfoDTO.getUser().getUsername(), user.getUsername())) {
        return HttpStatus.BAD_REQUEST;
      }

      rcGroupDTO = rocketChatService.createPrivateGroup(rocketChatHelper.generateGroupName(session),
          rcToken, rcUserId);

      if (rcGroupDTO.isPresent()) {

        if (rcGroupDTO.get().getGroup().getId() != null) {
          rcGroupId = Optional.of(rcGroupDTO.get().getGroup().getId());
        }

        // Update/set the user's Rocket.Chat id in the database
        user.setRcUserId(rcGroupDTO.get().getGroup().getUser().getId());
        userService.saveUser(user);

        List<ConsultantAgency> agencyList =
            consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());

        // Add technical user and all consultants of related agency to Rocket.Chat group
        if (!addCosultantsAndTechUserToGroup(rcGroupId, rcUserId, rcToken, agencyList)) {
          return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // Create an initial monitoring data set for the session
        if (consultingTypeSettings.isMonitoring()) {
          monitoringService.createMonitoring(session);
        }

        // Post enquiry message to Rocket.Chat group
        if (!messageServiceHelper.postMessage(message, rcUserId, rcToken, rcGroupId.get())) {
          rollbackCreateGroup(rcGroupId, rcToken, rcUserId);
          rollbackInitializeMonitoring(session);
          return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        sessionService.saveEnquiryMessageDateAndRocketChatGroupId(session, rcGroupId.get());

        // Send welcome message (if given/set)
        if (consultingTypeSettings.isSendWelcomeMessage()) {
          String welcomeMessage =
              MessageHelper.replaceUsernameInMessage(consultingTypeSettings.getWelcomeMessage(),
                  userHelper.decodeUsername(user.getUsername()));
          messageServiceHelper.postMessageAsSystemUser(welcomeMessage, rcGroupId.get());
        }

        // Create feedback chat group and add (peer) consultants and system user (if given/set)
        if (consultingTypeSettings.isFeedbackChat()) {
          if (!initializeFeedbackChat(session, rcGroupId, rcUserId, rcToken, agencyList)) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
          }
        }

      } else {
        return HttpStatus.INTERNAL_SERVER_ERROR;
      }

    } catch (RocketChatGetUserInfoException rocketChatGetUserInfoException) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    } catch (NoUserSessionException noUserSessionException) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    } catch (MessageHasAlreadyBeenSavedException messageHasAlreadyBeenSavedException) {
      return HttpStatus.CONFLICT;
    } catch (MessageServiceHelperException messageServiceHelperException) {
      logService.logMessageServiceHelperException("Error while calling the MessageService API: ",
          messageServiceHelperException);
      rollbackCreateGroup(rcGroupId, rcToken, rcUserId);
      return HttpStatus.INTERNAL_SERVER_ERROR;
    } catch (RocketChatCreateGroupException rocketChatCreateGroupException) {
      logService.logInternalServerError(
          String.format("Error while creating private group in Rocket.Chat. User-ID: %s",
              user.getUserId()),
          rocketChatCreateGroupException);
      return HttpStatus.INTERNAL_SERVER_ERROR;
    } catch (InitializeMonitoringException initializeMonitoringException) {
      logService.logInternalServerError(String.format(
          "Error while getting monitoring initialization data. Rollback needed. User-ID: %s",
          user.getUserId()), initializeMonitoringException);
      rollbackCreateGroup(rcGroupId, rcToken, rcUserId);
    } catch (EnquiryMessageException enquiryMessageException) {
      logService.logInternalServerError(String.format(
          "Error while saving enquiry message in database. Rollback needed. User-ID: %s",
          user.getUserId()), enquiryMessageException);
      rollbackCreateGroup(rcGroupId, rcToken, rcUserId);
      rollbackInitializeMonitoring(session);
      return HttpStatus.INTERNAL_SERVER_ERROR;
    } catch (RocketChatLoginException rocketChatLoginException) {
      logService.logInternalServerError("Could not log in technical user for Rocket.Chat API");
      rollbackCreateGroup(rcGroupId, rcToken, rcUserId);
      return HttpStatus.INTERNAL_SERVER_ERROR;
    } catch (ServiceException serviceException) {
      logService.logInternalServerError("Could not get consultants of agency");
      rollbackCreateGroup(rcGroupId, rcToken, rcUserId);
      return HttpStatus.INTERNAL_SERVER_ERROR;
    } catch (SaveUserException saveUserEx) {
      logService.logInternalServerError("Could not save Rocket.Chat user id to user in database");
      rollbackCreateGroup(rcGroupId, rcToken, rcUserId);
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    if (session != null) {
      emailNotificationFacade.sendNewEnquiryEmailNotification(session);
    }

    return HttpStatus.CREATED;

  }

  /**
   * Initializes the Rocket.Chat feedback chat group for a session (Create feedback chat group, add
   * (peer) consultants and system user).
   * 
   * @param session
   * @param rcGroupId
   * @param rcUserId
   * @param rcToken
   * @param agencyList
   * @return
   */
  private boolean initializeFeedbackChat(Session session, Optional<String> rcGroupId,
      String rcUserId, String rcToken, List<ConsultantAgency> agencyList) {

    Optional<String> rcFeedbackGroupId = Optional.empty();
    Optional<GroupResponseDTO> rcFeedbackGroupDTO = Optional.empty();

    try {
      rcFeedbackGroupDTO = rocketChatService
          .createPrivateGroupWithSystemUser(rocketChatHelper.generateFeedbackGroupName(session));

      if (rcFeedbackGroupDTO.isPresent() && rcFeedbackGroupDTO.get().getGroup().getId() != null) {
        rcFeedbackGroupId = Optional.of(rcFeedbackGroupDTO.get().getGroup().getId());

        // Add RocketChat user for system message to group
        rocketChatService.addUserToGroup(ROCKET_CHAT_SYSTEM_USER_ID, rcFeedbackGroupId.get());

        // Add all consultants of the session's agency to the feedback group that have the right
        // to view all feedback sessions
        for (ConsultantAgency agency : agencyList) {
          if (keycloakAdminClientHelper.userHasAuthority(agency.getConsultant().getId(),
              Authority.VIEW_ALL_FEEDBACK_SESSIONS)) {
            rocketChatService.addUserToGroup(agency.getConsultant().getRocketChatId(),
                rcFeedbackGroupId.get());
          }
        }

        // Remove all system messages
        if (!rocketChatService.removeSystemMessages(rcFeedbackGroupId.get(),
            LocalDateTime.now().minusHours(Helper.ONE_DAY_IN_HOURS), LocalDateTime.now())) {
          doRollback(session, rcGroupId, rcFeedbackGroupId, rcUserId, rcToken);
          return false;
        }

        // Update the session's feedback group id
        sessionService.updateFeedbackGroupId(Optional.of(session), rcFeedbackGroupId.get());

      } else {
        doRollback(session, rcGroupId, rcFeedbackGroupId, rcUserId, rcToken);
        return false;
      }

    } catch (RocketChatLoginException rocketChatLoginException) {
      rollbackCreateGroup(rcGroupId, rcToken, rcUserId);
      return false;

    } catch (RocketChatCreateGroupException rocketChatCreateGroupException) {
      logService.logInternalServerError(
          String.format("Error while creating feedback group in Rocket.Chat for user %s", rcUserId),
          rocketChatCreateGroupException);
      return false;

    } catch (RocketChatAddUserToGroupException rocketChatAddUserToGroupException) {
      doRollback(session, rcGroupId, rcFeedbackGroupId, rcUserId, rcToken);
      return false;

    } catch (RocketChatRemoveSystemMessagesException rocketChatRemoveSystemMessagesException) {
      doRollback(session, rcGroupId, rcFeedbackGroupId, rcUserId, rcToken);
      return false;

    } catch (UpdateFeedbackGroupIdException updateFeedbackEx) {
      doRollback(session, rcGroupId, rcFeedbackGroupId, rcUserId, rcToken);
      return false;
    }

    return true;
  }

  /**
   * Adds the consultant of the provided list of {@link ConsultantAgency} to the provided
   * Rocket.Chat group id
   * 
   * @param rcGroupId
   * @param agencyList
   * @return
   */
  private boolean addCosultantsAndTechUserToGroup(Optional<String> rcGroupId, String rcUserId,
      String rcToken, List<ConsultantAgency> agencyList) {

    try {
      // Add RocketChat user for system message to group
      rocketChatService.addUserToGroup(ROCKET_CHAT_SYSTEM_USER_ID, rcGroupId.get());

      if (agencyList != null) {
        for (ConsultantAgency agency : agencyList) {
          rocketChatService.addUserToGroup(agency.getConsultant().getRocketChatId(),
              rcGroupId.get());
        }
      }

    } catch (RocketChatAddUserToGroupException rocketChatAddUserToGroupException) {
      rollbackCreateGroup(rcGroupId, rcToken, rcUserId);
      return false;
    }

    return true;
  }

  /**
   * Performs a rollback for the parameter values (creation of Rocket.Chat groups and
   * changes/initialization of session), if given.
   * 
   * @param session
   * @param rcGroupId
   * @param rcFeedbackGroupId
   * @param rcUserId
   * @param rcToken
   */
  private void doRollback(Session session, Optional<String> rcGroupId,
      Optional<String> rcFeedbackGroupId, String rcUserId, String rcToken) {

    if (rcGroupId.isPresent()) {
      rollbackCreateGroup(rcGroupId, rcToken, rcUserId);
    }
    if (session != null) {
      rollbackInitializeMonitoring(session);
    }
    if (rcFeedbackGroupId.isPresent()) {
      rollbackCreateGroup(rcFeedbackGroupId, rcToken, rcUserId);
      rollbackSession(session);
    }
  }

  /**
   * Rollback the creation of the Rocket.Chat group
   * 
   * @param groupId
   * @param rcToken
   * @param rcUserId
   */
  private void rollbackCreateGroup(Optional<String> groupId, String rcToken, String rcUserId) {
    if (groupId != null && groupId.isPresent()) {
      try {
        if (!rocketChatService.deleteGroup(groupId.get(), rcToken, rcUserId)) {
          logService.logInternalServerError(String.format(
              "Error during rollback while saving enquiry message. Group with id %s could not be deleted.",
              groupId));
        }

      } catch (RocketChatDeleteGroupException rocketChatDeleteGroupException) {
        logService.logInternalServerError(String.format(
            "Error during rollback while saving enquiry message. Group with id %s could not be deleted.",
            groupId), rocketChatDeleteGroupException);
      }
    }
  }

  /**
   * Roll back the initialization of the monitoring data for a {@link Session}
   * 
   * @param session
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
   * Roll back the changes to the session
   * 
   * @param session
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
