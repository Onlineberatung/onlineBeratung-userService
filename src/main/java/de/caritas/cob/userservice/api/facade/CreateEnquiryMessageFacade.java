package de.caritas.cob.userservice.api.facade;

import de.caritas.cob.userservice.api.authorization.Authority;
import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.CreateEnquiryException;
import de.caritas.cob.userservice.api.exception.InitializeChatException;
import de.caritas.cob.userservice.api.exception.SaveUserException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddConsultantsException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddSystemUserException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.RocketChatHelper;
import de.caritas.cob.userservice.api.helper.ThrowingConsumerWrapper;
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

  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull EmailNotificationFacade emailNotificationFacade;
  private final @NonNull MessageServiceHelper messageServiceHelper;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull MonitoringService monitoringService;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull KeycloakAdminClientHelper keycloakAdminClientHelper;
  private final @NonNull UserHelper userHelper;
  private final @NonNull RocketChatHelper rocketChatHelper;
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

    } catch (CreateEnquiryException exception) {
      doRollback(exception.getExceptionInformation(), rocketChatCredentials);
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
      throws  CreateEnquiryException {

    String rcFeedbackGroupId = null;
    String rcGroupId;

    Session session = getSessionForEnquiryMessage(sessionId, user);
    checkIfSessionHasNoMessageDateYet(session);
    checkIfKeycloakAndRocketChatUsernamesMatch(rocketChatCredentials.getRocketChatUserId(), user);

    ConsultingTypeSettings consultingTypeSettings =
        consultingTypeManager.getConsultantTypeSettings(session.getConsultingType());
    List<ConsultantAgency> agencyList =
        consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());

    // InitializeChatException
    rcGroupId = initializeChat(session, agencyList, rocketChatCredentials);
    // InitializeChatException
    if (consultingTypeSettings.isFeedbackChat()) {
      rcFeedbackGroupId = initializeFeedbackChat(session, rcGroupId, agencyList);
    }

    CreateEnquiryExceptionInformation createEnquiryExceptionInformation =
        CreateEnquiryExceptionInformation.builder().session(session)
            .rcGroupId(rcGroupId).rcFeedbackGroupId(rcFeedbackGroupId).build();

    // CreateEnquiryException
    updateUser(user, rocketChatCredentials, createEnquiryExceptionInformation);

    // CreateMonitoringException
    monitoringService.createMonitoring(session, consultingTypeSettings);

    // RocketChatPostMessageException
    messageServiceHelper.postMessage(message, rocketChatCredentials, rcGroupId,
        createEnquiryExceptionInformation);
    // RocketChatPostWelcomeMessageException
    messageServiceHelper.postWelcomeMessage(rcGroupId, user,
        consultingTypeSettings, createEnquiryExceptionInformation);

    // CreateEnquiryException
    updateSession(session, rcGroupId, rcFeedbackGroupId, createEnquiryExceptionInformation);

    emailNotificationFacade.sendNewEnquiryEmailNotification(session);
  }

  private void checkIfSessionHasNoMessageDateYet(Session session) {
    if (!Objects.isNull(session.getEnquiryMessageDate())) {
      throw new ConflictException(
          String.format("Enquiry message already written for session %s", session.getId()),
          LogService::logCreateEnquiryMessageException);
    }
  }

  private void updateSession(Session session, String rcGroupId, String rcFeedbackGroupId,
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws CreateEnquiryException {

    try {
      session.setGroupId(rcGroupId);
      session.setFeedbackGroupId(rcFeedbackGroupId);
      session.setStatus(SessionStatus.NEW);
      sessionService.saveSession(session);
    } catch (InternalServerErrorException exception) {
      throw new CreateEnquiryException(String
          .format("Could not update session %s with groupId %s and feedbackGroupId %s",
              session.getId(), rcGroupId, rcFeedbackGroupId),
          exception, createEnquiryExceptionInformation);
    }

  }

  private void updateUser(User user, RocketChatCredentials rocketChatCredentials,
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws CreateEnquiryException {

    try {
      userHelper.updateRocketChatIdInDatabase(user, rocketChatCredentials.getRocketChatUserId());
    } catch (SaveUserException exception) {
      throw new CreateEnquiryException(String.format("Could not update user %s", user.getUserId()), exception,
          createEnquiryExceptionInformation);
    }
  }

  private Session getSessionForEnquiryMessage(Long sessionId, User user) {

    Optional<Session> session = sessionService.getSession(sessionId);

    if (!isSessionIsPresentAndBelongsToGivenUser(session, user)) {
      throw new BadRequestException(
          String.format("Session %s not found for user %s", sessionId, user.getUserId()),
          LogService::logCreateEnquiryMessageException);
    }

    return session.get();
  }

  private boolean isSessionIsPresentAndBelongsToGivenUser(Optional<Session> session, User user) {
    return session.isPresent() && session.get().getUser().getUserId().equals(user.getUserId());
  }

  private void checkIfKeycloakAndRocketChatUsernamesMatch(String rcUserId, User user) {

    UserInfoResponseDTO rcUserInfoDTO = rocketChatService.getUserInfo(rcUserId);

    if (!userHelper.doUsernamesMatch(rcUserInfoDTO.getUser().getUsername(), user.getUsername())) {
      throw new BadRequestException(String.format(
          "Enquiry message check: User with username %s does not match user with Rocket.Chat ID %s",
          user.getUsername(), rcUserId), LogService::logCreateEnquiryMessageException);
    }
  }

  private String initializeChat(Session session, List<ConsultantAgency> agencyList,
      RocketChatCredentials rocketChatCredentials) throws InitializeChatException {

    String rcGroupId = null;

    try {

      rcGroupId = createRocketChatGroupForSession(session, rocketChatCredentials);
      addSystemUserToGroup(rcGroupId);
      addConsultantsToGroup(rcGroupId, rocketChatCredentials,
          agencyList);
      rocketChatService.removeSystemMessages(rcGroupId,
          LocalDateTime.now().minusHours(Helper.ONE_DAY_IN_HOURS), LocalDateTime.now());

    } catch (RocketChatAddSystemUserException | RocketChatAddConsultantsException
        | RocketChatRemoveSystemMessagesException | RocketChatUserNotInitializedException exception) {
      CreateEnquiryExceptionInformation exceptionWithFeedbackId =
          CreateEnquiryExceptionInformation.builder().session(session).rcGroupId(rcGroupId).build();
      throw new InitializeChatException(
          session.getId(),
          exceptionWithFeedbackId);
    }

    return rcGroupId;
  }

  private String createRocketChatGroupForSession(Session session,
      RocketChatCredentials rocketChatCredentials) {

    Optional<GroupResponseDTO> rcGroupDTO;

    try {

      rcGroupDTO = rocketChatService
          .createPrivateGroup(rocketChatHelper.generateGroupName(session), rocketChatCredentials);

    } catch (RocketChatCreateGroupException exception) {
      throw new InternalServerErrorException(
          String
              .format("Could not create Rocket.Chat room for session %s and Rocket.Chat user %s",
                  session.getId(), rocketChatCredentials.getRocketChatUserId()),
          LogService::logCreateEnquiryMessageException);
    }

    return rcGroupDTO.get().getGroup().getId();

  }

  private String initializeFeedbackChat(Session session, String rcGroupId,
      List<ConsultantAgency> agencyList)
      throws InitializeChatException {

    String rcFeedbackGroupId = null;
    CreateEnquiryExceptionInformation exceptionWithoutFeedbackId =
        CreateEnquiryExceptionInformation.builder().session(session).rcGroupId(rcGroupId).build();

    try {

      rcFeedbackGroupId = createFeedbackGroupAsSystemUser(session,
          exceptionWithoutFeedbackId);

      rocketChatService.addUserToGroup(rocketChatSystemUserId, rcFeedbackGroupId);

      String finalRcFeedbackGroupId = rcFeedbackGroupId;
      agencyList.stream().filter(
          agency -> keycloakAdminClientHelper.userHasAuthority(agency.getConsultant().getId(),
              Authority.VIEW_ALL_FEEDBACK_SESSIONS)).forEach(ThrowingConsumerWrapper
          .throwingConsumerWrapper(
              agency -> rocketChatService.addUserToGroup(agency.getConsultant().getRocketChatId(),
                  finalRcFeedbackGroupId)));

      rocketChatService.removeSystemMessages(rcFeedbackGroupId,
          LocalDateTime.now().minusHours(Helper.ONE_DAY_IN_HOURS), LocalDateTime.now());

    } catch (RocketChatCreateGroupException | RocketChatAddUserToGroupException | RocketChatRemoveSystemMessagesException
        | RocketChatUserNotInitializedException exception) {
      CreateEnquiryExceptionInformation exceptionWithFeedbackId =
          CreateEnquiryExceptionInformation.builder().session(session).rcGroupId(rcGroupId)
              .rcFeedbackGroupId(rcFeedbackGroupId).build();
      throw new InitializeChatException(
          session.getId(),
          exceptionWithFeedbackId);
    }

    return rcFeedbackGroupId;
  }

  private String createFeedbackGroupAsSystemUser(Session session,
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws RocketChatCreateGroupException, InitializeChatException {

    Optional<GroupResponseDTO> rcFeedbackGroupDTO = rocketChatService
        .createPrivateGroupWithSystemUser(rocketChatHelper.generateFeedbackGroupName(session));

    if (!rcFeedbackGroupDTO.isPresent() || Objects
        .isNull(rcFeedbackGroupDTO.get().getGroup().getId())) {
      throw new InitializeChatException(
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
    monitoringService
        .rollbackInitializeMonitoring(createEnquiryExceptionInformation.getSession());
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
