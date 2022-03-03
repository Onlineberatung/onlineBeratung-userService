package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.repository.session.RegistrationType.ANONYMOUS;
import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes;

import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.container.RocketChatCredentials;
import de.caritas.cob.userservice.api.exception.CreateEnquiryException;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddSystemUserException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatCreateGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.helper.RocketChatRoomNameGenerator;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.CreateEnquiryMessageResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.MonitoringService;
import de.caritas.cob.userservice.api.service.message.MessageServiceProvider;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.ServletRequestAttributes;

/*
 * Facade for capsuling the steps for saving the enquiry message.
 */
@Service
@RequiredArgsConstructor
public class CreateEnquiryMessageFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull EmailNotificationFacade emailNotificationFacade;
  private final @NonNull MessageServiceProvider messageServiceProvider;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull MonitoringService monitoringService;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull UserHelper userHelper;
  private final @NonNull UserService userService;
  private final RocketChatRoomNameGenerator rocketChatRoomNameGenerator = new RocketChatRoomNameGenerator();

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
  public CreateEnquiryMessageResponseDTO createEnquiryMessage(User user, Long sessionId,
      String message,
      RocketChatCredentials rocketChatCredentials) {

    try {

      checkIfKeycloakAndRocketChatUsernamesMatch(rocketChatCredentials.getRocketChatUserId(), user);

      var session = fetchSessionForEnquiryMessage(sessionId, user);
      checkIfNotAnonymousEnquiry(session);
      checkIfEnquiryMessageIsAlreadyWrittenForSession(session);

      var extendedConsultingTypeResponseDTO = consultingTypeManager
          .getConsultingTypeSettings(session.getConsultingTypeId());

      List<ConsultantAgency> agencyList =
          consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());

      String rcGroupId = createRocketChatRoomAndAddUsers(session, agencyList,
          rocketChatCredentials);
      String rcFeedbackGroupId = retrieveRcFeedbackGroupIdIfConsultingTypeHasFeedbackChat(session,
          rcGroupId, agencyList, extendedConsultingTypeResponseDTO);

      var createEnquiryExceptionInformation = CreateEnquiryExceptionInformation.builder()
          .session(session)
          .rcGroupId(rcGroupId)
          .rcFeedbackGroupId(rcFeedbackGroupId)
          .build();

      saveRocketChatIdForUser(user, rocketChatCredentials, createEnquiryExceptionInformation);

      messageServiceProvider.postEnquiryMessage(message, rocketChatCredentials, rcGroupId,
          createEnquiryExceptionInformation);
      messageServiceProvider.postWelcomeMessageIfConfigured(rcGroupId, user,
          extendedConsultingTypeResponseDTO, createEnquiryExceptionInformation);
      messageServiceProvider.postFurtherStepsOrSaveSessionDataMessageIfConfigured(rcGroupId,
          extendedConsultingTypeResponseDTO, createEnquiryExceptionInformation);

      updateSession(session, rcGroupId, rcFeedbackGroupId, createEnquiryExceptionInformation);

      emailNotificationFacade
          .sendNewEnquiryEmailNotification(session, getHttpServletRequest().getServerName(),
              TenantContext.getCurrentTenantData());

      return new CreateEnquiryMessageResponseDTO()
          .rcGroupId(rcGroupId)
          .sessionId(sessionId);

    } catch (CreateEnquiryException exception) {
      doRollback(exception.getExceptionInformation(), rocketChatCredentials);
      throw new InternalServerErrorException(exception.getMessage(), exception,
          LogService::logCreateEnquiryMessageException);
    }

  }


  private HttpServletRequest getHttpServletRequest() {
    return ((ServletRequestAttributes) currentRequestAttributes()).getRequest();
  }

  private void checkIfKeycloakAndRocketChatUsernamesMatch(String rcUserId, User user) {

    UserInfoResponseDTO rcUserInfoDTO = rocketChatService.getUserInfo(rcUserId);

    if (!userHelper.doUsernamesMatch(rcUserInfoDTO.getUser().getUsername(), user.getUsername())) {
      throw new BadRequestException(String.format(
          "Enquiry message check: User with username %s does not match user with Rocket.Chat ID %s",
          user.getUsername(), rcUserId), LogService::logCreateEnquiryMessageException);
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

  private void checkIfNotAnonymousEnquiry(Session session) {
    if (session.getRegistrationType().equals(ANONYMOUS)) {
      throw new BadRequestException(
          String.format("Session %s is anonymous and therefore can't have an enquiry message.",
              session.getId()), LogService::logCreateEnquiryMessageException);
    }
  }

  private void checkIfEnquiryMessageIsAlreadyWrittenForSession(Session session) {
    if (nonNull(session.getEnquiryMessageDate())) {
      throw new ConflictException(
          String.format("Enquiry message already written for session %s", session.getId()),
          LogService::logCreateEnquiryMessageException);
    }
  }

  /**
   * Creates a new room in Rocket.Chat for the given {@link Session} and adds the consultants from
   * the provided {@link ConsultantAgency} list to the created room.
   *
   * @param session               {@link Session}
   * @param agencyList            list of {{@link ConsultantAgency} to add to the room
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return Rocket.Chat group ID
   * @throws CreateEnquiryException when error occurs during creation
   */
  public String createRocketChatRoomAndAddUsers(Session session, List<ConsultantAgency> agencyList,
      RocketChatCredentials rocketChatCredentials) throws CreateEnquiryException {

    String rcGroupId = createRocketChatGroupForSession(session, rocketChatCredentials);

    try {
      addSystemUserToGroup(rcGroupId);
      if (!ANONYMOUS.equals(session.getRegistrationType())) {
        addConsultantsToGroup(rcGroupId, agencyList);
      }
      rocketChatService.removeSystemMessages(rcGroupId,
          nowInUtc().minusHours(Helper.ONE_DAY_IN_HOURS), nowInUtc());

    } catch (RocketChatAddSystemUserException | RocketChatAddUserToGroupException
        | RocketChatRemoveSystemMessagesException | RocketChatUserNotInitializedException exception) {
      var createEnquiryExceptionInformation =
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
          .createPrivateGroup(rocketChatRoomNameGenerator.generateGroupName(session),
              rocketChatCredentials);
      return retrieveRcGroupResponseDto(rcGroupDTO, session.getId(),
          rocketChatCredentials.getRocketChatUserId()).getGroup().getId();

    } catch (RocketChatCreateGroupException exception) {
      throw new InternalServerErrorException(
          String
              .format("Could not create Rocket.Chat room for session %s and Rocket.Chat user %s",
                  session.getId(), rocketChatCredentials.getRocketChatUserId()),
          LogService::logCreateEnquiryMessageException);
    }

  }

  private GroupResponseDTO retrieveRcGroupResponseDto(Optional<GroupResponseDTO> groupResponseDTO,
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
      String rcGroupId, List<ConsultantAgency> agencyList,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO)
      throws CreateEnquiryException {

    if (isFalse(extendedConsultingTypeResponseDTO.getInitializeFeedbackChat())) {
      return null;
    }

    return createRcFeedbackGroup(session, rcGroupId, agencyList);
  }

  private String createRcFeedbackGroup(Session session, String rcGroupId,
      List<ConsultantAgency> agencyList) throws CreateEnquiryException {

    String rcFeedbackGroupId = null;
    var createEnquiryExceptionInformation = CreateEnquiryExceptionInformation.builder()
        .session(session)
        .rcGroupId(rcGroupId)
        .build();

    try {

      rcFeedbackGroupId = createFeedbackGroupAsSystemUser(session,
          createEnquiryExceptionInformation);
      addSystemUserToGroup(rcFeedbackGroupId);
      addConsultantsToFeedbackChatGroup(agencyList, rcFeedbackGroupId);
      rocketChatService
          .removeSystemMessages(rcFeedbackGroupId, nowInUtc().minusHours(Helper.ONE_DAY_IN_HOURS),
              nowInUtc());

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
        .createPrivateGroupWithSystemUser(
            rocketChatRoomNameGenerator.generateFeedbackGroupName(session));

    if (rcFeedbackGroupDTO.isEmpty() || Objects
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
      rocketChatService.addUserToGroup(agency.getConsultant().getRocketChatId(), rcFeedbackGroupId);
    }
  }

  private void saveRocketChatIdForUser(User user, RocketChatCredentials rocketChatCredentials,
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws CreateEnquiryException {

    try {
      userService.updateRocketChatIdInDatabase(user, rocketChatCredentials.getRocketChatUserId());
    } catch (IllegalArgumentException exception) {
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
      session.setEnquiryMessageDate(nowInUtc());
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
    if (nonNull(createEnquiryExceptionInformation)) {
      rollbackCreateGroup(createEnquiryExceptionInformation.getRcGroupId(), rocketChatCredentials);
      rollbackCreateGroupAsSystemUser(createEnquiryExceptionInformation.getRcFeedbackGroupId());
      monitoringService
          .rollbackInitializeMonitoring(createEnquiryExceptionInformation.getSession());
    }
  }

  private void rollbackCreateGroup(String rcGroupId,
      RocketChatCredentials rocketChatCredentials) {
    if (nonNull(rcGroupId) && nonNull(rocketChatCredentials) && !rocketChatService
        .rollbackGroup(rcGroupId, rocketChatCredentials)) {
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
