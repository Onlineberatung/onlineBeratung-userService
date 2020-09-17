package de.caritas.cob.userservice.api.facade;

import de.caritas.cob.userservice.api.authorization.Authority;
import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.CheckForCorrectRocketChatUserException;
import de.caritas.cob.userservice.api.exception.CreateMonitoringException;
import de.caritas.cob.userservice.api.exception.EnquiryMessageException;
import de.caritas.cob.userservice.api.exception.InitializeFeedbackChatException;
import de.caritas.cob.userservice.api.exception.MessageHasAlreadyBeenSavedException;
import de.caritas.cob.userservice.api.exception.NoUserSessionException;
import de.caritas.cob.userservice.api.exception.SaveUserException;
import de.caritas.cob.userservice.api.exception.UpdateFeedbackGroupIdException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddConsultantsException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddSystemUserException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetUserInfoException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatPostWelcomeMessageException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.RocketChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.model.rocketChat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import de.caritas.cob.userservice.api.service.helper.MessageServiceHelper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/*
 * Facade for capsuling the steps for saving the enquiry message.
 */
@Service
@RequiredArgsConstructor
public class CreateEnquiryMessageFacade {

  private final @NonNull
  SessionService sessionService;
  private final @NonNull
  RocketChatService rocketChatService;
  private final @NonNull
  EmailNotificationFacade emailNotificationFacade;
  private final @NonNull
  MessageServiceHelper messageServiceHelper;
  private final @NonNull
  ConsultantAgencyService consultantAgencyService;
  private final @NonNull
  MonitoringService monitoringService;
  private final @NonNull
  ConsultingTypeManager consultingTypeManager;
  private final @NonNull
  KeycloakAdminClientHelper keycloakAdminClientHelper;
  private final @NonNull
  UserHelper userHelper;
  private final @NonNull
  RocketChatHelper rocketChatHelper;
  @Value("${rocket.systemuser.id}")
  private String rocketChatSystemUserId;

  /**
   * Handles possible exceptions and roll backs during the creation of the enquiry message for a
   * session.
   *
   * @param user                  {@link User}
   * @param sessionId             {@link Session#getId()}
   * @param message               enquiry message
   * @param rocketChatCredentials {@link RocketChatCredentials}
   */
  public void createEnquiryMessage(User user, Long sessionId, String message,
      RocketChatCredentials rocketChatCredentials) {

    try {
      doCreateEnquiryMessageSteps(user, sessionId, message, rocketChatCredentials);

    } catch (MessageHasAlreadyBeenSavedException messageHasAlreadyBeenSavedException) {
      throw new ConflictException(messageHasAlreadyBeenSavedException.getMessage(),
          LogService::logCreateEnquiryMessageException);
    } catch (NoUserSessionException
        | CheckForCorrectRocketChatUserException checkForCorrectRocketChatUserException) {
      throw new BadRequestException(checkForCorrectRocketChatUserException.getMessage(),
          LogService::logCreateEnquiryMessageException);
    } catch (RocketChatCreateGroupException | RocketChatGetUserInfoException exception) {
      throw new InternalServerErrorException(exception.getMessage(),
          LogService::logCreateEnquiryMessageException);
    } catch (RocketChatAddConsultantsException | RocketChatAddSystemUserException | CreateMonitoringException
        | RocketChatPostMessageException | RocketChatPostWelcomeMessageException
        | EnquiryMessageException | InitializeFeedbackChatException exception) {
      doRollback(exception.getExceptionInformation(), rocketChatCredentials);
      throw new InternalServerErrorException(exception.getMessage(),
          LogService::logCreateEnquiryMessageException);
    } catch (InternalServerErrorException | SaveUserException exception) {
      // Presumably only the Rocket.Chat group was created yet
      Optional<Session> session = sessionService.getSession(sessionId);
      if (session.isPresent()) {
        CreateEnquiryExceptionInformation exceptionInformation =
            CreateEnquiryExceptionInformation.builder().rcGroupId(session.get().getGroupId())
                .build();
        doRollback(exceptionInformation, rocketChatCredentials);
      }

      throw new InternalServerErrorException(exception.getMessage(),
          LogService::logCreateEnquiryMessageException);
    }

  }

  /**
   * Creates the private Rocket.Chat group, initializes the session monitoring and saves the enquiry
   * message in Rocket.Chat.
   *
   * @param user                  {@link User}
   * @param sessionId             {@link Session#getId()}
   * @param message               Message
   * @param rocketChatCredentials {@link RocketChatCredentials}
   */
  private void doCreateEnquiryMessageSteps(User user, Long sessionId, String message,
      RocketChatCredentials rocketChatCredentials)
      throws RocketChatAddConsultantsException, CreateMonitoringException,
      RocketChatPostMessageException, RocketChatPostWelcomeMessageException,
      EnquiryMessageException, InitializeFeedbackChatException, RocketChatCreateGroupException,
      RocketChatGetUserInfoException, SaveUserException, MessageHasAlreadyBeenSavedException, NoUserSessionException, RocketChatAddSystemUserException {

    Session session = getSessionForEnquiryMessage(sessionId, user);

    if (!Objects.isNull(session.getEnquiryMessageDate())) {
      throw new MessageHasAlreadyBeenSavedException(
          String.format("Enquiry message already written for session %s", sessionId));
    }

    checkIfKeycloakAndRocketChatUsernamesMatch(rocketChatCredentials.getRocketChatUserId(), user);

    GroupResponseDTO rcGroupDTO = createRocketChatGroupForSession(session, rocketChatCredentials);
    userHelper.updateRocketChatIdInDatabase(user, rcGroupDTO.getGroup().getUser().getId());

    List<ConsultantAgency> agencyList =
        consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());

    addSystemUserToGroup(rcGroupDTO.getGroup().getId());
    addConsultantsToGroup(rcGroupDTO.getGroup().getId(), rocketChatCredentials,
        agencyList);

    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultantTypeSettings(session.getConsultingType());
    monitoringService.createMonitoring(session, consultingTypeSettings);

    CreateEnquiryExceptionInformation createEnquiryExceptionInformation =
        CreateEnquiryExceptionInformation.builder().session(session)
            .rcGroupId(rcGroupDTO.getGroup().getId()).build();
    messageServiceHelper.postMessage(message, rocketChatCredentials, rcGroupDTO.getGroup().getId(),
        createEnquiryExceptionInformation);

    sessionService.saveEnquiryMessageDateAndRocketChatGroupId(session,
        rcGroupDTO.getGroup().getId());

    messageServiceHelper.postWelcomeMessage(rcGroupDTO.getGroup().getId(), user,
        consultingTypeSettings, createEnquiryExceptionInformation);

    if (consultingTypeSettings.isFeedbackChat()) {
      initializeFeedbackChat(session, rcGroupDTO.getGroup().getId(), agencyList);
    }

    emailNotificationFacade.sendNewEnquiryEmailNotification(session);
  }

  private Session getSessionForEnquiryMessage(Long sessionId, User user)
      throws NoUserSessionException {

    Optional<Session> session = sessionService.getSession(sessionId);

    if (!session.isPresent() || !session.get().getUser().getUserId().equals(user.getUserId())) {
      throw new NoUserSessionException(
          String.format("Session %s not found for user %s", sessionId, user.getUserId()));
    }

    return session.get();
  }

  private void checkIfKeycloakAndRocketChatUsernamesMatch(String rcUserId, User user)
      throws RocketChatGetUserInfoException {

    UserInfoResponseDTO rcUserInfoDTO = rocketChatService.getUserInfo(rcUserId);
    if (!userHelper.doUsernamesMatch(rcUserInfoDTO.getUser().getUsername(), user.getUsername())) {
      throw new CheckForCorrectRocketChatUserException(String.format(
          "Enquiry message check: User with username %s does not match user with Rocket.Chat ID %s",
          user.getUsername(), rcUserId));
    }
  }

  private GroupResponseDTO createRocketChatGroupForSession(Session session,
      RocketChatCredentials rocketChatCredentials) throws RocketChatCreateGroupException {

    Optional<GroupResponseDTO> rcGroupDTO = rocketChatService
        .createPrivateGroup(rocketChatHelper.generateGroupName(session), rocketChatCredentials);

    if (!rcGroupDTO.isPresent()) {
      throw new RocketChatCreateGroupException(
          String.format("Could not create Rocket.Chat room for session %s and Rocket.Chat user %s",
              session.getId(), rocketChatCredentials.getRocketChatUserId()));
    }

    return rcGroupDTO.get();
  }

  private void initializeFeedbackChat(Session session, String rcGroupId,
      List<ConsultantAgency> agencyList)
      throws InitializeFeedbackChatException {

    String rcFeedbackGroupId = null;
    CreateEnquiryExceptionInformation exceptionWithoutFeedbackId =
        CreateEnquiryExceptionInformation.builder().session(session).rcGroupId(rcGroupId).build();

    try {

      rcFeedbackGroupId = createFeedbackGroupAsSystemUserAndGetGroupId(session, exceptionWithoutFeedbackId);

      // Add RocketChat user for system message to group
      rocketChatService.addUserToGroup(rocketChatSystemUserId, rcFeedbackGroupId);

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

    } catch (RocketChatCreateGroupException | RocketChatAddUserToGroupException | RocketChatRemoveSystemMessagesException
        | UpdateFeedbackGroupIdException | RocketChatUserNotInitializedException exception) {
      CreateEnquiryExceptionInformation exceptionWithFeedbackId =
          CreateEnquiryExceptionInformation.builder().session(session).rcGroupId(rcGroupId)
              .rcFeedbackGroupId(rcFeedbackGroupId).build();
      throw new InitializeFeedbackChatException(
          session.getId(),
          exceptionWithFeedbackId);
    }
  }

  private String createFeedbackGroupAsSystemUserAndGetGroupId(Session session,
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws RocketChatCreateGroupException, InitializeFeedbackChatException {

    Optional<GroupResponseDTO> rcFeedbackGroupDTO = rocketChatService
        .createPrivateGroupWithSystemUser(rocketChatHelper.generateFeedbackGroupName(session));

    if (!rcFeedbackGroupDTO.isPresent() || Objects
        .isNull(rcFeedbackGroupDTO.get().getGroup().getId())) {
      throw new InitializeFeedbackChatException(
          session.getId(),
          createEnquiryExceptionInformation);
    }
    return rcFeedbackGroupDTO.get().getGroup().getId();
  }

  private void addSystemUserToGroup(String rcGroupId)
      throws RocketChatAddSystemUserException {

    try {
      rocketChatService.addUserToGroup(rocketChatSystemUserId, rcGroupId);
    } catch (RocketChatAddUserToGroupException rocketChatAddUserToGroupException) {
      throw new RocketChatAddSystemUserException(
          String.format(
              "Add system user error: Could not add user with ID %s to Rocket.Chat group %s",
              rocketChatSystemUserId, rcGroupId),
          rocketChatAddUserToGroupException,
          CreateEnquiryExceptionInformation.builder().rcGroupId(rcGroupId).build());
    }
  }

  private void addConsultantsToGroup(String rcGroupId,
      RocketChatCredentials rocketChatCredentials, List<ConsultantAgency> agencyList)
      throws RocketChatAddConsultantsException {

    try {
      for (ConsultantAgency agency : agencyList) {
        rocketChatService.addUserToGroup(agency.getConsultant().getRocketChatId(), rcGroupId);
      }
    } catch (RocketChatAddUserToGroupException rocketChatAddUserToGroupException) {
      throw new RocketChatAddConsultantsException(
          String.format(
              "Add consultants error: Could not add user with ID %s to group %s",
              rocketChatCredentials.getRocketChatUserId(), rcGroupId),
          rocketChatAddUserToGroupException,
          CreateEnquiryExceptionInformation.builder().rcGroupId(rcGroupId).build());
    }
  }

  private void doRollback(CreateEnquiryExceptionInformation createEnquiryExceptionInformation,
      RocketChatCredentials rocketChatCredentials) {

    if (Objects.isNull(createEnquiryExceptionInformation)) {
      return;
    }

    rollbackCreateGroup(createEnquiryExceptionInformation.getRcGroupId(), rocketChatCredentials);
    rollbackCreateGroup(createEnquiryExceptionInformation.getRcFeedbackGroupId(),
        rocketChatCredentials);
    monitoringService.rollbackInitializeMonitoring(createEnquiryExceptionInformation.getSession());
    rollbackSession(createEnquiryExceptionInformation.getSession());

  }

  private void rollbackCreateGroup(String rcGroupId,
      RocketChatCredentials rocketChatCredentials) {
    if (Objects.isNull(rcGroupId) || Objects.isNull(rocketChatCredentials)) {
      return;
    }
    if (!rocketChatService.rollbackGroup(rcGroupId, rocketChatCredentials)) {
      LogService.logInternalServerError(String.format(
          "Error during rollback while saving enquiry message. Group with id %s could not be deleted.",
          rcGroupId));
    }
  }

  private void rollbackSession(Session session) {
    if (!Objects.isNull(session)) {
      try {
        session.setEnquiryMessageDate(null);
        session.setStatus(SessionStatus.INITIAL);
        session.setGroupId(null);
        session.setFeedbackGroupId(null);
        sessionService.saveSession(session);
      } catch (InternalServerErrorException ex) {
        LogService.logInternalServerError(String.format(
            "Error during rollback while saving session with id %s. Session data could not be set to state before createEnquiryMessageFacade.",
            session.getId()), ex);
      }
    }
  }
}
