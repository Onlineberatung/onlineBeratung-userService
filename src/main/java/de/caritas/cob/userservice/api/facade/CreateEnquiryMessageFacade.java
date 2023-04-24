package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static de.caritas.cob.userservice.api.model.Session.RegistrationType.ANONYMOUS;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentials;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.group.GroupResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserInfoResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateEnquiryMessageResponseDTO;
import de.caritas.cob.userservice.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.userservice.api.exception.CreateEnquiryException;
import de.caritas.cob.userservice.api.exception.httpresponses.ConflictException;
import de.caritas.cob.userservice.api.exception.httpresponses.CreateEnquiryMessageException;
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
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.EnquiryData;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.service.ConsultantAgencyService;
import de.caritas.cob.userservice.api.service.message.MessageServiceProvider;
import de.caritas.cob.userservice.api.service.message.RocketChatData;
import de.caritas.cob.userservice.api.service.session.SessionService;
import de.caritas.cob.userservice.api.service.user.UserService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import de.caritas.cob.userservice.messageservice.generated.web.model.MessageResponseDTO;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/*
 * Facade for capsuling the steps for saving the enquiry message.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateEnquiryMessageFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull EmailNotificationFacade emailNotificationFacade;
  private final @NonNull MessageServiceProvider messageServiceProvider;
  private final @NonNull ConsultantAgencyService consultantAgencyService;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull UserHelper userHelper;
  private final @NonNull UserService userService;
  private final RocketChatRoomNameGenerator rocketChatRoomNameGenerator =
      new RocketChatRoomNameGenerator();

  @Value("${rocket.systemuser.id}")
  private String rocketChatSystemUserId;

  /**
   * Creates the private Rocket.Chat group, initializes the session and saves the enquiry message in
   * Rocket.Chat.
   *
   * @param enquiryData data necessary for creating the enquiry message
   */
  public CreateEnquiryMessageResponseDTO createEnquiryMessage(EnquiryData enquiryData) {
    try {
      checkIfKeycloakAndRocketChatUsernamesMatch(
          enquiryData.getRocketChatCredentials().getRocketChatUserId(), enquiryData.getUser());

      var session =
          fetchSessionForEnquiryMessage(enquiryData.getSessionId(), enquiryData.getUser());
      checkIfNotAnonymousEnquiry(session);
      checkIfEnquiryMessageIsAlreadyWrittenForSession(session);

      var extendedConsultingTypeResponseDTO =
          consultingTypeManager.getConsultingTypeSettings(session.getConsultingTypeId());
      List<ConsultantAgency> agencyList =
          consultantAgencyService.findConsultantsByAgencyId(session.getAgencyId());
      String rcGroupId =
          createRocketChatRoomAndAddUsers(
              session, agencyList, enquiryData.getRocketChatCredentials());
      String rcFeedbackGroupId =
          retrieveRcFeedbackGroupIdIfConsultingTypeHasFeedbackChat(
              session, rcGroupId, agencyList, extendedConsultingTypeResponseDTO);

      var createEnquiryExceptionInformation =
          CreateEnquiryExceptionInformation.builder()
              .session(session)
              .rcGroupId(rcGroupId)
              .rcFeedbackGroupId(rcFeedbackGroupId)
              .build();

      saveRocketChatIdForUser(
          enquiryData.getUser(),
          enquiryData.getRocketChatCredentials(),
          createEnquiryExceptionInformation);
      MessageResponseDTO messageResponse;

      if (isAppointmentEnquiryMessage(enquiryData)) {
        messageResponse =
            messageServiceProvider.assignUserToRocketChatGroup(
                rcGroupId, createEnquiryExceptionInformation);
      } else {
        var rocketChatData =
            new RocketChatData(
                enquiryData.getMessage(),
                enquiryData.getRocketChatCredentials(),
                rcGroupId,
                enquiryData.getType());
        messageResponse =
            messageServiceProvider.postEnquiryMessage(
                rocketChatData, createEnquiryExceptionInformation);
      }

      messageServiceProvider.postWelcomeMessageIfConfigured(
          rcGroupId,
          enquiryData.getUser(),
          extendedConsultingTypeResponseDTO,
          createEnquiryExceptionInformation);
      messageServiceProvider.postFurtherStepsIfConfigured(
          rcGroupId, extendedConsultingTypeResponseDTO, createEnquiryExceptionInformation);

      updateSession(
          session,
          enquiryData.getLanguage(),
          rcGroupId,
          rcFeedbackGroupId,
          createEnquiryExceptionInformation);

      if (session.getIsConsultantDirectlySet()) {
        emailNotificationFacade.sendNewDirectEnquiryEmailNotification(
            session.getConsultant().getId(),
            session.getAgencyId(),
            session.getPostcode(),
            TenantContext.getCurrentTenantData());
      } else {
        emailNotificationFacade.sendNewEnquiryEmailNotification(
            session, TenantContext.getCurrentTenantData());
      }

      return new CreateEnquiryMessageResponseDTO()
          .rcGroupId(rcGroupId)
          .sessionId(enquiryData.getSessionId())
          .t(messageResponse.getT());

    } catch (CreateEnquiryException exception) {
      doRollback(exception.getExceptionInformation(), enquiryData.getRocketChatCredentials());
      log.error("CreateEnquiryMessageFacade error: ", exception);
      throw new InternalServerErrorException(exception.getMessage(), exception);
    }
  }

  private boolean isAppointmentEnquiryMessage(EnquiryData enquiryData) {
    return enquiryData.getConsultantEmail() != null;
  }

  private void checkIfKeycloakAndRocketChatUsernamesMatch(String rcUserId, User user) {

    UserInfoResponseDTO rcUserInfoDTO = rocketChatService.getUserInfo(rcUserId);

    if (!userHelper.doUsernamesMatch(rcUserInfoDTO.getUser().getUsername(), user.getUsername())) {
      throw new CreateEnquiryMessageException(
          String.format(
              "Enquiry message check: User with username %s does not match user with Rocket.Chat ID %s",
              user.getUsername(), rcUserId));
    }
  }

  private Session fetchSessionForEnquiryMessage(Long sessionId, User user) {

    Optional<Session> session = sessionService.getSession(sessionId);

    if (session.isPresent() && session.get().getUser().getUserId().equals(user.getUserId())) {
      return session.get();
    }
    throw new CreateEnquiryMessageException(
        String.format("Session %s not found for user %s", sessionId, user.getUserId()));
  }

  private void checkIfNotAnonymousEnquiry(Session session) {
    if (session.getRegistrationType().equals(ANONYMOUS)) {
      throw new CreateEnquiryMessageException(
          String.format(
              "Session %s is anonymous and therefore can't have an enquiry message.",
              session.getId()));
    }
  }

  private void checkIfEnquiryMessageIsAlreadyWrittenForSession(Session session) {
    if (nonNull(session.getEnquiryMessageDate())) {
      throw new ConflictException(
          String.format("Enquiry message already written for session %s", session.getId()));
    }
  }

  /**
   * Creates a new room in Rocket.Chat for the given {@link Session} and adds the consultants from
   * the provided {@link ConsultantAgency} list to the created room.
   *
   * @param session {@link Session}
   * @param agencyList list of {{@link ConsultantAgency} to add to the room
   * @param rocketChatCredentials {@link RocketChatCredentials}
   * @return Rocket.Chat group ID
   * @throws CreateEnquiryException when error occurs during creation
   */
  public String createRocketChatRoomAndAddUsers(
      Session session,
      List<ConsultantAgency> agencyList,
      RocketChatCredentials rocketChatCredentials)
      throws CreateEnquiryException {

    String rcGroupId = createRocketChatGroupForSession(session, rocketChatCredentials);

    try {
      addSystemUserToGroup(rcGroupId);
      if (!ANONYMOUS.equals(session.getRegistrationType())) {
        addConsultantsToGroup(rcGroupId, agencyList);
      }
      rocketChatService.removeSystemMessages(
          rcGroupId, nowInUtc().minusHours(Helper.ONE_DAY_IN_HOURS), nowInUtc());

    } catch (RocketChatAddSystemUserException
        | RocketChatAddUserToGroupException
        | RocketChatRemoveSystemMessagesException
        | RocketChatUserNotInitializedException exception) {
      var createEnquiryExceptionInformation =
          CreateEnquiryExceptionInformation.builder().session(session).rcGroupId(rcGroupId).build();
      throw new CreateEnquiryException(
          String.format("Could not initialize chat for session %s", session.getId()),
          exception,
          createEnquiryExceptionInformation);
    }

    return rcGroupId;
  }

  private String createRocketChatGroupForSession(
      Session session, RocketChatCredentials rocketChatCredentials) {

    try {

      Optional<GroupResponseDTO> rcGroupDTO =
          rocketChatService.createPrivateGroup(
              rocketChatRoomNameGenerator.generateGroupName(session), rocketChatCredentials);
      return retrieveRcGroupResponseDto(
              rcGroupDTO, session.getId(), rocketChatCredentials.getRocketChatUserId())
          .getGroup()
          .getId();

    } catch (RocketChatCreateGroupException exception) {
      throw new InternalServerErrorException(
          String.format(
              "Could not create Rocket.Chat room for session %s and Rocket.Chat user %s",
              session.getId(), rocketChatCredentials.getRocketChatUserId()));
    }
  }

  private GroupResponseDTO retrieveRcGroupResponseDto(
      Optional<GroupResponseDTO> groupResponseDTO, long sessionId, String rocketChatUserId) {
    return groupResponseDTO.orElseThrow(
        () ->
            new InternalServerErrorException(
                String.format(
                    "Could not create Rocket.Chat room for session %s and Rocket.Chat user %s",
                    sessionId, rocketChatUserId)));
  }

  private void addSystemUserToGroup(String rcGroupId) throws RocketChatAddSystemUserException {

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

  private String retrieveRcFeedbackGroupIdIfConsultingTypeHasFeedbackChat(
      Session session,
      String rcGroupId,
      List<ConsultantAgency> agencyList,
      ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO)
      throws CreateEnquiryException {

    if (isFalse(extendedConsultingTypeResponseDTO.getInitializeFeedbackChat())) {
      return null;
    }

    return createRcFeedbackGroup(session, rcGroupId, agencyList);
  }

  public String createRcFeedbackGroup(
      Session session, String rcGroupId, List<ConsultantAgency> agencyList)
      throws CreateEnquiryException {

    String rcFeedbackGroupId = null;
    var createEnquiryExceptionInformation =
        CreateEnquiryExceptionInformation.builder().session(session).rcGroupId(rcGroupId).build();

    try {

      rcFeedbackGroupId =
          createFeedbackGroupAsSystemUser(session, createEnquiryExceptionInformation);
      addSystemUserToGroup(rcFeedbackGroupId);
      addConsultantsToFeedbackChatGroup(agencyList, rcFeedbackGroupId);
      rocketChatService.removeSystemMessages(
          rcFeedbackGroupId, nowInUtc().minusHours(Helper.ONE_DAY_IN_HOURS), nowInUtc());

    } catch (RocketChatAddSystemUserException
        | RocketChatCreateGroupException
        | RocketChatAddUserToGroupException
        | RocketChatRemoveSystemMessagesException
        | RocketChatUserNotInitializedException exception) {
      createEnquiryExceptionInformation.setRcFeedbackGroupId(rcFeedbackGroupId);
      throw new CreateEnquiryException(
          String.format("Could not initialize feedback chat for session %s", session.getId()),
          exception,
          createEnquiryExceptionInformation);
    }

    return rcFeedbackGroupId;
  }

  private String createFeedbackGroupAsSystemUser(
      Session session, CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws RocketChatCreateGroupException, CreateEnquiryException {

    Optional<GroupResponseDTO> rcFeedbackGroupDTO =
        rocketChatService.createPrivateGroupWithSystemUser(
            rocketChatRoomNameGenerator.generateFeedbackGroupName(session));

    if (rcFeedbackGroupDTO.isEmpty()
        || Objects.isNull(rcFeedbackGroupDTO.get().getGroup().getId())) {
      throw new CreateEnquiryException(
          String.format("Could not create rc feedback group for session %s", session.getId()),
          createEnquiryExceptionInformation);
    }
    return rcFeedbackGroupDTO.get().getGroup().getId();
  }

  private void addConsultantsToFeedbackChatGroup(
      List<ConsultantAgency> agencyList, String rcFeedbackGroupId)
      throws RocketChatAddUserToGroupException {

    for (ConsultantAgency agency : agencyList) {
      rocketChatService.addUserToGroup(agency.getConsultant().getRocketChatId(), rcFeedbackGroupId);
    }
  }

  private void saveRocketChatIdForUser(
      User user,
      RocketChatCredentials rocketChatCredentials,
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws CreateEnquiryException {

    try {
      userService.updateRocketChatIdInDatabase(user, rocketChatCredentials.getRocketChatUserId());
    } catch (IllegalArgumentException exception) {
      throw new CreateEnquiryException(
          String.format("Could not update user %s", user.getUserId()),
          exception,
          createEnquiryExceptionInformation);
    }
  }

  private void updateSession(
      Session session,
      String language,
      String rcGroupId,
      String rcFeedbackGroupId,
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation)
      throws CreateEnquiryException {

    try {
      session.setGroupId(rcGroupId);
      session.setFeedbackGroupId(rcFeedbackGroupId);
      session.setStatus(SessionStatus.NEW);
      session.setEnquiryMessageDate(nowInUtc());
      if (nonNull(language)) {
        session.setLanguageCode(LanguageCode.getByCode(language));
      }
      setSessionStatusInProgressIfConsultantIsAlreadyAssigned(session);
      sessionService.saveSession(session);
    } catch (InternalServerErrorException exception) {
      throw new CreateEnquiryException(
          String.format(
              "Could not update session %s with groupId %s and feedbackGroupId %s",
              session.getId(), rcGroupId, rcFeedbackGroupId),
          exception,
          createEnquiryExceptionInformation);
    }
  }

  private void setSessionStatusInProgressIfConsultantIsAlreadyAssigned(Session session) {
    if (nonNull(session.getConsultant())) {
      session.setStatus(SessionStatus.IN_PROGRESS);
    }
  }

  private void doRollback(
      CreateEnquiryExceptionInformation createEnquiryExceptionInformation,
      RocketChatCredentials rocketChatCredentials) {
    if (nonNull(createEnquiryExceptionInformation)) {
      rollbackCreateGroup(createEnquiryExceptionInformation.getRcGroupId(), rocketChatCredentials);
      rollbackCreateGroupAsSystemUser(createEnquiryExceptionInformation.getRcFeedbackGroupId());
    }
  }

  private void rollbackCreateGroup(String rcGroupId, RocketChatCredentials rocketChatCredentials) {
    if (nonNull(rcGroupId)
        && nonNull(rocketChatCredentials)
        && !rocketChatService.rollbackGroup(rcGroupId, rocketChatCredentials)) {
      log.error(
          "Internal Server Error: Error during rollback of group while saving enquiry "
              + "message. Group with id {} could not be deleted.",
          rcGroupId);
    }
  }

  private void rollbackCreateGroupAsSystemUser(String rcGroupId) {
    if (Objects.isNull(rcGroupId)) {
      return;
    }
    if (!rocketChatService.deleteGroupAsSystemUser(rcGroupId)) {
      log.error(
          "Internal Server Error: Error during rollback of feedback group while saving "
              + "enquiry message. Group with id {} could not be deleted.",
          rcGroupId);
    }
  }
}
