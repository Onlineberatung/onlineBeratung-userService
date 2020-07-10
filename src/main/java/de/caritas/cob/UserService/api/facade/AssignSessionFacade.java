package de.caritas.cob.UserService.api.facade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import de.caritas.cob.UserService.api.authorization.Authority;
import de.caritas.cob.UserService.api.exception.ServiceException;
import de.caritas.cob.UserService.api.exception.UpdateSessionException;
import de.caritas.cob.UserService.api.exception.keycloak.KeycloakException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatAddUserToGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatGetGroupMembersException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatLoginException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.UserService.api.helper.AuthenticatedUser;
import de.caritas.cob.UserService.api.helper.Helper;
import de.caritas.cob.UserService.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.session.SessionStatus;
import de.caritas.cob.UserService.api.service.ConsultantService;
import de.caritas.cob.UserService.api.service.LogService;
import de.caritas.cob.UserService.api.service.RocketChatService;
import de.caritas.cob.UserService.api.service.SessionService;
import de.caritas.cob.UserService.api.service.helper.KeycloakAdminClientHelper;
import de.caritas.cob.UserService.api.service.helper.RocketChatRollbackHelper;

/**
 * Facade to encapsulate the steps for accepting an enquiry and/or assigning a session to a
 * consultant.
 *
 */

@Service
public class AssignSessionFacade {

  @Value("${rocket.technical.username}")
  private String ROCKET_CHAT_TECH_USER_USERNAME;

  @Value("${rocket.systemuser.id}")
  private String ROCKET_CHAT_SYSTEM_USER_ID;

  private final SessionService sessionService;
  private final RocketChatService rocketChatService;
  private final KeycloakAdminClientHelper keycloakAdminClientHelper;
  private final ConsultantService consultantService;
  private final RocketChatRollbackHelper rocketChatRollbackHelper;
  private final LogService logService;
  private final AuthenticatedUser authenticatedUser;
  private final EmailNotificationFacade emailNotificationFacade;

  @Autowired
  public AssignSessionFacade(SessionService sessionService, RocketChatService rocketChatService,
      KeycloakAdminClientHelper keycloakHelper, ConsultantService consultantService,
      RocketChatRollbackHelper rocketChatRollbackHelper, LogService logService,
      AuthenticatedUser authenticatedUser, EmailNotificationFacade emailNotificationFacade) {
    this.sessionService = sessionService;
    this.rocketChatService = rocketChatService;
    this.keycloakAdminClientHelper = keycloakHelper;
    this.consultantService = consultantService;
    this.rocketChatRollbackHelper = rocketChatRollbackHelper;
    this.logService = logService;
    this.authenticatedUser = authenticatedUser;
    this.emailNotificationFacade = emailNotificationFacade;
  }

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Remove all other
   * consultants from the Rocket.Chat group which don't have the right to view this session anymore.
   * Furthermore add the given {@link Consultant} to the feedback group if needed.
   * 
   * @param session
   * @param consultant
   * @param isFirstAssignment
   * @return
   */
  public HttpStatus assignSession(Session session, Consultant consultant,
      boolean isFirstAssignment) {

    // Check if session is already assigned
    if (sessionAlreadyAssignedToConsultant(session, consultant, isFirstAssignment)) {
      return HttpStatus.CONFLICT;
    }

    // Check if user and consultant do have a Rocket.Chat user id
    if (rocketChatIdMissing(session, consultant)) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    // Check if consultant is assigned to the agency of the session that he will be assigned to
    if (!consultantAssignedToAgency(session, consultant)) {
      return HttpStatus.FORBIDDEN;
    }

    Optional<Consultant> initialConsultant = Optional.ofNullable(session.getConsultant());
    SessionStatus initialStatus = session.getStatus();
    List<GroupMemberDTO> memberList = null;

    try {

      // Update the consultant and the status to IN_PROGRESS if session is an enquiry
      sessionService.updateConsultantAndStatusForSession(session, consultant,
          initialStatus == SessionStatus.NEW ? SessionStatus.IN_PROGRESS : initialStatus);

      // Add technical user to Rocket.Chat group
      if (!addTechnicalUserToGroup(session.getGroupId(), session, initialConsultant, initialStatus,
          memberList)) {
        return HttpStatus.INTERNAL_SERVER_ERROR;
      }

      // Add consultant to the Rocket.Chat group (if no enquiry)
      if (initialConsultant.isPresent()) {
        if (!addUserToRocketChatGroup(session.getGroupId(), consultant.getRocketChatId(), session,
            initialConsultant, initialStatus, memberList)) {
          return HttpStatus.INTERNAL_SERVER_ERROR;
        }
      }

      memberList = rocketChatService.getMembersOfGroup(session.getGroupId());

      // Remove all users from the Rocket.Chat group which should not be able to read
      // the messages after the session has been assigned
      if (!this.removeUnauthorizedUsersFromRocketChatGroup(memberList, session, consultant,
          initialConsultant, initialStatus)) {
        return HttpStatus.INTERNAL_SERVER_ERROR;
      }

      // Remove technical user from Rocket.Chat group
      if (!removeTechnicalUserFromGroup(session.getGroupId(), session, initialConsultant,
          initialStatus, memberList)) {
        return HttpStatus.INTERNAL_SERVER_ERROR;
      }

      // Remove all system messages from group
      if (!removeSystemMessagesFromRocketChatGroup(session.getGroupId(), null, session,
          initialConsultant, initialStatus, memberList)) {
        return HttpStatus.INTERNAL_SERVER_ERROR;
      }

      // If feedback chat is enabled for this session
      if (session.hasFeedbackChat()) {

        // Add consultant to the Rocket.Chat feedback group
        if (!addUserToRocketChatGroup(session.getFeedbackGroupId(), consultant.getRocketChatId(),
            session, initialConsultant, initialStatus, memberList)) {
          return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // Remove previous consultant from feedback group if session is no enquiry and previous
        // consultant is no main consultant
        if (!removePreviousConsultantFromRocketChatFeedbackGroup(session, consultant,
            initialConsultant, initialStatus, memberList)) {
          return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // Remove all system messages from feedback group, else do a rollback
        if (!removeSystemMessagesFromRocketChatGroup(session.getFeedbackGroupId(),
            consultant.getRocketChatId(), session, initialConsultant, initialStatus, memberList)) {
          return HttpStatus.INTERNAL_SERVER_ERROR;
        }
      }


    } catch (RocketChatLoginException rcLoginEx) {
      logService.logInternalServerError("Could not login Rocket.Chat technical user");

      return HttpStatus.INTERNAL_SERVER_ERROR;

    } catch (UpdateSessionException updateEx) {
      logService
          .logInternalServerError(String.format("Could not update session %s with consultantId %s",
              session.getId(), consultant.getId()), updateEx);

      return HttpStatus.INTERNAL_SERVER_ERROR;

    } catch (RocketChatGetGroupMembersException getGroupMembersEx) {
      logService.logInternalServerError(String.format(
          "Could not get Rocket.Chat group members of group id %s. Initiate rollback.",
          session.getGroupId()), getGroupMembersEx);
      rollbackSessionUpdate(session, initialConsultant, initialStatus);

      return HttpStatus.INTERNAL_SERVER_ERROR;

    }

    // Send an email notification if the enquiry/session was assigned to the consultant by another
    // consultant
    if (!authenticatedUser.getUserId().equals(consultant.getId())) {
      emailNotificationFacade.sendAssignEnquiryEmailNotification(consultant,
          authenticatedUser.getUserId(), session.getUser().getUsername());
    }

    return HttpStatus.OK;
  }

  /**
   * Sets the session back to the status it was before the
   * {@link #assignSession(Session, Consultant)} method took place.
   * 
   * @param session
   */
  private void rollbackSessionUpdate(Session session, Optional<Consultant> consultant,
      SessionStatus status) {

    if (session != null) {
      try {
        session.setConsultant(consultant.isPresent() ? consultant.get() : null);
        session.setStatus(status);
        sessionService.updateConsultantAndStatusForSession(session, session.getConsultant(),
            session.getStatus());

      } catch (UpdateSessionException updateSessionEx) {
        logService.logInternalServerError(String.format(
            "Error during rollback while setting the session with id %s back to previous state.",
            session.getId()));
      }
    }
  }

  /**
   * Resets the session, adds the user back to the Rocket.Chat group and removes the consultant from
   * the feedback group chat if the session is no enquiry
   * 
   * @param session
   * @param initialStatus
   * @param memberList
   */
  private void rollbackSessionAndRocketChatGroup(Session session,
      Optional<Consultant> initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    rollbackSessionUpdate(session, initialConsultant, initialStatus);
    rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(session.getGroupId(),
        memberList);
    if (initialConsultant.isPresent()) {
      rocketChatService.removeUserFromGroup(initialConsultant.get().getRocketChatId(),
          session.getFeedbackGroupId());
    }
  }

  /**
   * Returns true if the given Rocket.Chat user was added successfully to the given Rocket.Chat
   * group.
   * 
   * @param groupId
   * @param rcUserId
   * @param session
   * @param initialConsultant
   * @param initialStatus
   * @param memberList
   * @return
   */
  private boolean addUserToRocketChatGroup(String groupId, String rcUserId, Session session,
      Optional<Consultant> initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    try {
      rocketChatService.addUserToGroup(rcUserId, groupId);

    } catch (RocketChatAddUserToGroupException addUserEx) {
      logService.logInternalServerError(String.format(
          "Could not add user with id %s to Rocket.Chat group with id %s. Initiate rollback.",
          rcUserId, groupId), addUserEx);
      rollbackSessionUpdate(session, initialConsultant, initialStatus);

      if (session.hasFeedbackChat()) {
        rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(groupId, memberList);
      }

      return false;
    }

    return true;
  }

  /**
   * Returns true if the Rocket.Chat technical user was added successfully to the given groupId.
   * 
   * @param groupId
   * @param session
   * @param initialConsultant
   * @param initialStatus
   * @return
   */
  private boolean addTechnicalUserToGroup(String groupId, Session session,
      Optional<Consultant> initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {

    try {
      if (!rocketChatService.addTechnicalUserToGroup(groupId)) {
        logService.logInternalServerError(String.format(
            "Could not add Rocket.Chat technical user to Rocket.Chat group with id %s. Initiate rollback.",
            session.getGroupId()));
        rollbackSessionUpdate(session, initialConsultant, initialStatus);

        return false;
      }

    } catch (RocketChatAddUserToGroupException addUserEx) {
      logService.logInternalServerError(String.format(
          "Could not add Rocket.Chat technical user to Rocket.Chat group with id %s. Initiate rollback.",
          session.getGroupId()), addUserEx);
      rollbackSessionUpdate(session, initialConsultant, initialStatus);

      return false;
    }

    return true;
  }

  /**
   * Returns true if the Rocket.Chat technical user was removed successfully from the given groupId.
   * 
   * @param groupId
   * @param session
   * @param initialConsultant
   * @param initialStatus
   * @param memberList
   * @return
   */
  private boolean removeTechnicalUserFromGroup(String groupId, Session session,
      Optional<Consultant> initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    if (!rocketChatService.removeTechnicalUserFromGroup(groupId)) {
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
      logService.logInternalServerError(String.format(
          "Could not remove techical user from Rocket.Chat group id %s", session.getGroupId()));

      return false;
    }

    return true;
  }

  /**
   * Returns true if the given {@link Consultant} is assigned to the agency of the given
   * {@link Session}
   * 
   * @param session
   * @param consultant
   * @return
   */
  private boolean consultantAssignedToAgency(Session session, Consultant consultant) {
    Set<ConsultantAgency> consultantAgencies = consultant.getConsultantAgencies();
    List<Long> consultantAgencyIds =
        consultantAgencies.stream().map(ConsultantAgency::getAgencyId).collect(Collectors.toList());
    if (!consultantAgencyIds.contains(session.getAgencyId())) {
      logService.logAssignSessionFacadeWarning(
          String.format("Agency %s of session %s is not assigned to consultant %s.",
              session.getAgencyId().toString(), session.getId().toString(), consultant.getId()));

      return false;
    }

    return true;
  }

  /**
   * Returns true if the user of the given {@link Session} or the given {@link Consultant} don't
   * have an assigned Rocket.Chat id in the user database.
   * 
   * @param session
   * @param consultant
   * @return
   */
  private boolean rocketChatIdMissing(Session session, Consultant consultant) {
    if (session != null && session.getUser() != null
        && (session.getUser().getRcUserId() == null || session.getUser().getRcUserId().isEmpty())) {
      logService.logAssignSessionFacadeError(String.format(
          "The provided user with id %s does not have a Rocket.Chat id assigned in the database.",
          session.getUser().getUserId()));

      return true;
    }
    if (consultant != null
        && (consultant.getRocketChatId() == null || consultant.getRocketChatId().isEmpty())) {
      logService.logAssignSessionFacadeError(String.format(
          "The provided consultant with id %s does not have a Rocket.Chat id assigned in the database.",
          consultant.getId()));

      return true;
    }

    return false;
  }

  /**
   * Returns true if the given {@link Session} is already assigned to the given {@link Consultant}
   * for sessions in progress or to any consultant if session is new (enquiry).
   * 
   * @param session
   * @param consultant
   * @param isFirstAssignment true if session is an enquiry (SessionStatus = NEW)
   * @return
   */
  private boolean sessionAlreadyAssignedToConsultant(Session session, Consultant consultant,
      boolean isFirstAssignment) {

    if (Objects.isNull(session.getConsultant())
        || session.getConsultant().getId().equals(StringUtils.EMPTY)) {
      return false;
    }

    if (session.getStatus().equals(SessionStatus.NEW)
        || (session.getStatus().equals(SessionStatus.IN_PROGRESS) && isFirstAssignment)) {
      logService.logAssignSessionFacadeWarning(String.format(
          "Session %s is already assigned to a consultant and cannot be accepted by consultant %s.",
          session.getId().toString(), consultant.getId()));

      return true;
    }

    if (session.getStatus().equals(SessionStatus.IN_PROGRESS)
        && session.getConsultant().getId().equals(consultant.getId())) {
      logService.logAssignSessionFacadeWarning(String.format(
          "Session %s is already assigned to this consultant. Assignment to consultant %s is not possible.",
          session.getId().toString(), consultant.getId()));

      return true;
    }

    return false;
  }

  /**
   * Removes user from Rocket.Chat group (if not asker, technical user, system user, or the to be
   * assigned consultant or consultant in team session) which should not be able to read the
   * messages after the session has been assigned. Returns true on success.
   * 
   * @param memberList
   * @param session
   * @param consultant
   * @param initialConsultant
   * @param initialStatus
   * @return
   */
  private boolean removeUnauthorizedUsersFromRocketChatGroup(List<GroupMemberDTO> memberList,
      Session session, Consultant consultant, Optional<Consultant> initialConsultant,
      SessionStatus initialStatus) {
    AtomicReference<String> rcUserIdToRemove = new AtomicReference<>();

    try {
      memberList.forEach(member -> {
        if (!member.get_id().equalsIgnoreCase(session.getUser().getRcUserId())
            && !member.get_id().equalsIgnoreCase(consultant.getRocketChatId())
            && !member.getUsername().equalsIgnoreCase(ROCKET_CHAT_TECH_USER_USERNAME)
            && !member.get_id().equalsIgnoreCase(ROCKET_CHAT_SYSTEM_USER_ID)) {

          rcUserIdToRemove.set(member.get_id());

          if (!session.isTeamSession()) {
            rocketChatService.removeUserFromGroup(rcUserIdToRemove.get(), session.getGroupId());

          } else if (session.getConsultingType() == ConsultingType.U25) {
            // Special case when consulting type is U25: Remove user from Rocket.Chat group only
            // if he doesn't have the right to view all peer sessions.
            Optional<Consultant> memberConsultant =
                consultantService.getConsultantByRcUserId(member.get_id());
            if (memberConsultant.isPresent()
                && !keycloakAdminClientHelper.userHasAuthority(memberConsultant.get().getId(),
                    Authority.VIEW_ALL_PEER_SESSIONS)) {
              rocketChatService.removeUserFromGroup(rcUserIdToRemove.get(), session.getGroupId());
            }
          }
        }
      });
    } catch (RocketChatRemoveUserFromGroupException removeUserEx) {
      logService.logInternalServerError(String.format(
          "Could not remove Rocket.Chat user %s from Rocket.Chat group with id %s. Initiate rollback.",
          rcUserIdToRemove, session.getGroupId()), removeUserEx);
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);

      return false;

    } catch (KeycloakException keycloakEx) {
      logService.logInternalServerError(
          String.format("Could not get Keycloak roles for user with id %s. Initiate rollback.",
              rcUserIdToRemove),
          keycloakEx);
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);

      return false;

    } catch (ServiceException serviceEx) {
      logService.logInternalServerError(String.format(
          "Could not get consultant information from database for role check. Initiate rollback.",
          consultant.getId()), serviceEx);
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);

      return false;
    }

    return true;
  }

  /**
   * Removes previous consultant from feedback group if session is no enquiry and previous
   * consultant is no main consultant. Returns true on success.
   * 
   * @param session
   * @param consultant
   * @param initialConsultant
   * @param initialStatus
   * @param memberList
   * @return
   */
  private boolean removePreviousConsultantFromRocketChatFeedbackGroup(Session session,
      Consultant consultant, Optional<Consultant> initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {

    try {
      if (initialConsultant.isPresent()
          && !keycloakAdminClientHelper.userHasAuthority(initialConsultant.get().getId(),
              Authority.VIEW_ALL_FEEDBACK_SESSIONS)) {

        if (rocketChatService.addTechnicalUserToGroup(session.getFeedbackGroupId())) {
          rocketChatService.removeUserFromGroup(initialConsultant.get().getRocketChatId(),
              session.getFeedbackGroupId());

          // Remove technical user from Rocket.Chat feedback group
          if (!rocketChatService.removeTechnicalUserFromGroup(session.getFeedbackGroupId())) {
            rocketChatService.removeUserFromGroup(consultant.getRocketChatId(),
                session.getFeedbackGroupId());
            rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus,
                memberList);
            logService.logInternalServerError(String.format(
                "Could not remove techical user from Rocket.Chat feedback group with id %s",
                session.getGroupId()));

            return false;
          }
        } else {
          // Rollback
          rocketChatService.removeUserFromGroup(consultant.getRocketChatId(),
              session.getFeedbackGroupId());
          rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
          logService.logInternalServerError(
              String.format("Could not add technical user to Rocket.Chat feedback group with id %s",
                  session.getFeedbackGroupId()));

          return false;
        }
      }

    } catch (KeycloakException keycloakEx) {
      logService.logInternalServerError(
          String.format("Could not get Keycloak roles for user with id %s. Initiate rollback.",
              initialConsultant.get().getId()),
          keycloakEx);
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);

      return false;

    } catch (RocketChatAddUserToGroupException addUserEx) {
      logService.logInternalServerError(String.format(
          "Could not add Rocket.Chat technical userto Rocket.Chat group with id %s. Initiate rollback.",
          session.getFeedbackGroupId()), addUserEx);
      rollbackSessionUpdate(session, initialConsultant, initialStatus);
      rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(session.getGroupId(),
          memberList);

      return false;

    } catch (RocketChatRemoveUserFromGroupException removeUserEx) {
      logService.logInternalServerError(String.format(
          "Could not remove Rocket.Chat user %s from Rocket.Chat group with id %s. Initiate rollback.",
          consultant.getRocketChatId(), session.getFeedbackGroupId()), removeUserEx);
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);

      return false;

    }

    return true;
  }

  /**
   * Returns true if all system messages where successfully removed from the given Rocket.Chat
   * group.
   * 
   * @param groupId
   * @param session
   * @param initialConsultant
   * @param initialStatus
   * @param memberList
   * @return
   */
  private boolean removeSystemMessagesFromRocketChatGroup(String groupId,
      String rcUserIdToRemoveOnRollback, Session session, Optional<Consultant> initialConsultant,
      SessionStatus initialStatus, List<GroupMemberDTO> memberList) {


    if (!rocketChatService.removeSystemMessages(groupId,
        LocalDateTime.now().minusHours(Helper.ONE_DAY_IN_HOURS), LocalDateTime.now())) {

      if (rcUserIdToRemoveOnRollback != null) {
        rocketChatService.removeUserFromGroup(rcUserIdToRemoveOnRollback,
            session.getFeedbackGroupId());
      }
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
      logService.logInternalServerError(
          String.format("Could not remove system messages from Rocket.Chat group id %s", groupId));

      return false;
    }

    return true;
  }

}
