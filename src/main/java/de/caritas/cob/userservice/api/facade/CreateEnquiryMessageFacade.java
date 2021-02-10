package de.caritas.cob.userservice.api.facade;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.CreateEnquiryException;
import de.caritas.cob.userservice.api.exception.SaveUserException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddSystemUserException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.Now;
import de.caritas.cob.userservice.api.helper.RocketChatHelper;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
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
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull UserHelper userHelper;
  private final @NonNull RocketChatHelper rocketChatHelper;
  private final Now now;
  @Value("${rocket.systemuser.id}")
  private String rocketChatSystemUserId;

  /**
   * Creates the private Rocket.Chat group, initializes the session monitoring and saves the enquiry
   * message in Rocket.Chat.
   *
   * @param user                  {@link User}
   * @param sessionId             {@link Session#getId()}
   * @param message               enquiry message
   * @param rocketChatCredentials {@link RocketChatCredentials}
   */
  public void createEnquiryMessage(User user, Long sessionId, String message,
      RocketChatCredentials rocketChatCredentials) {

    try {

      checkIfKeycloakAndRocketChatUsernamesMatch(rocketChatCredentials.getRocketChatUserId(), user);

      Session session = fetchSessionForEnquiryMessage(sessionId, user);
      checkIfEnquiryMessageIsAlreadyWrittenForSession(session);

      ConsultingTypeSettings consultingTypeSettings =
          consultingTypeManager.getConsultingTypeSettings(session.getConsultingType());
      List<ConsultantAgency> agencyList =
          consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());

      String rcGroupId = retrieveRcGroupId(session, agencyList, rocketChatCredentials);
      String rcFeedbackGroupId = retrieveRcFeedbackGroupIdIfConsultingTypeHasFeedbackChat(session,
          rcGroupId, agencyList, consultingTypeSettings);

      CreateEnquiryExceptionInformation createEnquiryExceptionInformation =
          CreateEnquiryExceptionInformation.builder().session(session)
              .rcGroupId(rcGroupId).rcFeedbackGroupId(rcFeedbackGroupId).build();

      saveRocketChatIdForUser(user, rocketChatCredentials, createEnquiryExceptionInformation);

      messageServiceHelper.postMessage(message, rocketChatCredentials, rcGroupId,
          createEnquiryExceptionInformation);
      messageServiceHelper.postWelcomeMessageIfConfigured(rcGroupId, user,
          consultingTypeSettings, createEnquiryExceptionInformation);

      updateSession(session, rcGroupId, rcFeedbackGroupId, createEnquiryExceptionInformation);

      emailNotificationFacade.sendNewEnquiryEmailNotification(session);

    } catch (CreateEnquiryException exception) {
      doRollback(exception.getExceptionInformation(), rocketChatCredentials);
      throw new InternalServerErrorException(exception.getMessage(), exception,
          LogService::logCreateEnquiryMessageException);
    }

  }

  private Session fetchSessionForEnquiryMessage(Long sessionId, User user) {

    Optional<Session> session = sessionService.getSession(sessionId);

    if (session.isPresent() && session.get().getUser().getUserId().equals(user.getUserId())) {
      return session.get();
    }
    throw new BadRequestException(
        String.format("Session %s not found for user %s", sessionId, user.getUserId()),
        LogService::logCreateEnquiryMessageException);
  }

  private void checkIfEnquiryMessageIsAlreadyWrittenForSession(Session session) {
    if (nonNull(session.getEnquiryMessageDate())) {
      throw new ConflictException(
          String.format("Enquiry message already written for session %s", session.getId()),
          LogService::logCreateEnquiryMessageException);
    }
  }

  private void checkIfKeycloakAndRocketChatUsernamesMatch(String rcUserId, User user) {

    UserInfoResponseDTO rcUserInfoDTO = rocketChatService.getUserInfo(rcUserId);

    if (!userHelper.doUsernamesMatch(rcUserInfoDTO.getUser().getUsername(), user.getUsername())) {
      throw new BadRequestException(String.format(
          "Enquiry message check: User with username %s does not match user with Rocket.Chat ID %s",
          user.getUsername(), rcUserId), LogService::logCreateEnquiryMessageException);
    }
  }

  private String retrieveRcGroupId(Session session, List<ConsultantAgency> agencyList,
      RocketChatCredentials rocketChatCredentials) throws CreateEnquiryException {

    String rcGroupId = createRocketChatGroupForSession(session, rocketChatCredentials);

    try {
      addSystemUserToGroup(rcGroupId);
      addConsultantsToGroup(rcGroupId, agencyList);
      rocketChatService.removeSystemMessages(rcGroupId,
          LocalDateTime.now().minusHours(Helper.ONE_DAY_IN_HOURS), LocalDateTime.now());

    } catch (RocketChatAddSystemUserException | RocketChatAddUserToGroupException
        | RocketChatRemoveSystemMessagesException | RocketChatUserNotInitializedException exception) {
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation =
          CreateEnquiryExceptionInformation.builder().session(session).rcGroupId(rcGroupId).build();
      throw new CreateEnquiryException(
          String.format("Could not initialize chat for session %s", session.getId()),
          exception,
          createEnquiryExceptionInformation);
    }

    return rcGroupId;
  }

  private String createRocketChatGroupForSession(Session session,
      RocketChatCredentials rocketChatCredentials) {

    try {

      Optional<GroupResponseDTO> rcGroupDTO = rocketChatService
          .createPrivateGroup(rocketChatHelper.generateGroupName(session), rocketChatCredentials);
      return retrieveRcGroupResponceDto(rcGroupDTO, session.getId(),
          rocketChatCredentials.getRocketChatUserId()).getGroup().getId();

    } catch (RocketChatCreateGroupException exception) {
      throw new InternalServerErrorException(
          String
              .format("Could not create Rocket.Chat room for session %s and Rocket.Chat user %s",
                  session.getId(), rocketChatCredentials.getRocketChatUserId()),
          LogService::logCreateEnquiryMessageException);
    }

  }

  private GroupResponseDTO retrieveRcGroupResponceDto(Optional<GroupResponseDTO> groupResponseDTO,
      long sessionId, String rocketChatUserId) {
    return groupResponseDTO.orElseThrow(() -> new InternalServerErrorException(
        String.format("Could not create Rocket.Chat room for session %s and Rocket.Chat user %s",
            sessionId, rocketChatUserId),
        LogService::logCreateEnquiryMessageException));
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

  private void addConsultantsToGroup(String rcGroupId, List<ConsultantAgency> agencyList)
      throws RocketChatAddUserToGroupException {

    for (ConsultantAgency agency : agencyList) {
      rocketChatService.addUserToGroup(agency.getConsultant().getRocketChatId(), rcGroupId);
    }
  }

  private String retrieveRcFeedbackGroupIdIfConsultingTypeHasFeedbackChat(Session session,
      String rcGroupId,
      List<ConsultantAgency> agencyList, ConsultingTypeSettings consultingTypeSettings)
      throws CreateEnquiryException {

    if (!consultingTypeSettings.isFeedbackChat()) {
      return null;
    }

    return createRcFeedbackGroup(session, rcGroupId, agencyList);
  }

  private String createRcFeedbackGroup(Session session, String rcGroupId,
      List<ConsultantAgency> agencyList) throws CreateEnquiryException {

    String rcFeedbackGroupId = null;
    CreateEnquiryExceptionInformation createEnquiryExceptionInformation =
        CreateEnquiryExceptionInformation.builder().session(session).rcGroupId(rcGroupId).build();

    try {

      rcFeedbackGroupId = createFeedbackGroupAsSystemUser(session,
          createEnquiryExceptionInformation);
      addSystemUserToGroup(rcFeedbackGroupId);
      addConsultantsToFeedbackChatGroup(agencyList, rcFeedbackGroupId);
      rocketChatService.removeSystemMessages(rcFeedbackGroupId,
          LocalDateTime.now().minusHours(Helper.ONE_DAY_IN_HOURS), LocalDateTime.now());

    } catch (RocketChatAddSystemUserException | RocketChatCreateGroupException | RocketChatAddUserToGroupException
        | RocketChatRemoveSystemMessagesException | RocketChatUserNotInitializedException exception) {
      createEnquiryExceptionInformation.setRcFeedbackGroupId(rcFeedbackGroupId);
      throw new CreateEnquiryException(
          String.format("Could not initialize feedback chat for session %s", session.getId()),
          exception,
          createEnquiryExceptionInformation);
    }

    return rcFeedbackGroupId;
  }

  private String createFeedbackGroupAsSystemUser(Session session,
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws RocketChatCreateGroupException, CreateEnquiryException {

    Optional<GroupResponseDTO> rcFeedbackGroupDTO = rocketChatService
        .createPrivateGroupWithSystemUser(rocketChatHelper.generateFeedbackGroupName(session));

    if (!rcFeedbackGroupDTO.isPresent() || Objects
        .isNull(rcFeedbackGroupDTO.get().getGroup().getId())) {
      throw new CreateEnquiryException(
          String.format("Could not create rc feedback group for session %s", session.getId()),
          createEnquiryExceptionInformation);
    }
    return rcFeedbackGroupDTO.get().getGroup().getId();
  }

  private void addConsultantsToFeedbackChatGroup(List<ConsultantAgency> agencyList,
      String rcFeedbackGroupId) throws RocketChatAddUserToGroupException {

    for (ConsultantAgency agency : agencyList) {
      if (keycloakAdminClientService.userHasAuthority(agency.getConsultant().getId(),
          Authority.VIEW_ALL_FEEDBACK_SESSIONS)) {
        rocketChatService.addUserToGroup(agency.getConsultant().getRocketChatId(),
            rcFeedbackGroupId);
      }
    }
  }

  private void saveRocketChatIdForUser(User user, RocketChatCredentials rocketChatCredentials,
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws CreateEnquiryException {

    try {
      userHelper.updateRocketChatIdInDatabase(user, rocketChatCredentials.getRocketChatUserId());
    } catch (SaveUserException exception) {
      throw new CreateEnquiryException(String.format("Could not update user %s", user.getUserId()),
          exception,
          createEnquiryExceptionInformation);
    }
  }

  private void updateSession(Session session, String rcGroupId, String rcFeedbackGroupId,
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws CreateEnquiryException {

    try {
      session.setGroupId(rcGroupId);
      session.setFeedbackGroupId(rcFeedbackGroupId);
      session.setStatus(SessionStatus.NEW);
      session.setEnquiryMessageDate(now.getDate());
      sessionService.saveSession(session);
    } catch (InternalServerErrorException exception) {
      throw new CreateEnquiryException(String
          .format("Could not update session %s with groupId %s and feedbackGroupId %s",
              session.getId(), rcGroupId, rcFeedbackGroupId),
          exception, createEnquiryExceptionInformation);
    }

  }

  private void doRollback(CreateEnquiryExceptionInformation createEnquiryExceptionInformation,
      RocketChatCredentials rocketChatCredentials) {

    if (Objects.isNull(createEnquiryExceptionInformation)) {
      return;
    }

    rollbackCreateGroup(createEnquiryExceptionInformation.getRcGroupId(), rocketChatCredentials);
    rollbackCreateGroupAsSystemUser(createEnquiryExceptionInformation.getRcFeedbackGroupId());
    monitoringService
        .rollbackInitializeMonitoring(createEnquiryExceptionInformation.getSession());

  }

  private void rollbackCreateGroup(String rcGroupId,
      RocketChatCredentials rocketChatCredentials) {
    if (Objects.isNull(rcGroupId) || Objects.isNull(rocketChatCredentials)) {
      return;
    }
    if (!rocketChatService.rollbackGroup(rcGroupId, rocketChatCredentials)) {
      LogService.logInternalServerError(String.format(
          "Error during rollback of group while saving enquiry message. Group with id %s could not be deleted.",
          rcGroupId));
    }
  }

  private void rollbackCreateGroupAsSystemUser(String rcGroupId) {
    if (Objects.isNull(rcGroupId)) {
      return;
    }
    if (!rocketChatService.deleteGroupAsSystemUser(rcGroupId)) {
      LogService.logInternalServerError(String.format(
          "Error during rollback of feedback group while saving enquiry message. Group with id %s could not be deleted.",
          rcGroupId));
    }
  }

}
