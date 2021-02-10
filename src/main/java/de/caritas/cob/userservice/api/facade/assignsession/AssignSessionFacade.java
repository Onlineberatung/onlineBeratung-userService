package de.caritas.cob.userservice.api.facade.assignsession;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.authorization.Authorities.Authority;
import de.caritas.cob.userservice.api.exception.UpdateSessionException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveSystemMessagesException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatRollbackService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps for accepting an enquiry and/or assigning a session to a
 * consultant.
 */
@Service
@RequiredArgsConstructor
public class AssignSessionFacade {

  @Value("${rocket.technical.username}")
  private String rocketChatTechUserUsername;

  @Value("${rocket.systemuser.id}")
  private String rocketChatSystemUserId;

  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull RocketChatRollbackService rocketChatRollbackService;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull EmailNotificationFacade emailNotificationFacade;

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Remove all other
   * consultants from the Rocket.Chat group which don't have the right to view this session anymore.
   * Furthermore add the given {@link Consultant} to the feedback group if needed.
   */
  public void assignEnquiry(Session session, Consultant consultant) {
    new SessionToConsultantVerifier(session, consultant).verifySessionIsNotInProgress();
    assignSession(session, consultant);
  }

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Remove all other
   * consultants from the Rocket.Chat group which don't have the right to view this session anymore.
   * Furthermore add the given {@link Consultant} to the feedback group if needed.
   */
  public void assignSession(Session session, Consultant consultant) {

    SessionToConsultantVerifier sessionToConsultantVerifier =
        new SessionToConsultantVerifier(session, consultant);
    sessionToConsultantVerifier.verifyPreconditionsForAssignment();

    Consultant initialConsultant = session.getConsultant();
    SessionStatus initialStatus = session.getStatus();

    try {
      sessionService.updateConsultantAndStatusForSession(session, consultant,
          initialStatus == SessionStatus.NEW ? SessionStatus.IN_PROGRESS : initialStatus);

      List<GroupMemberDTO> memberList = createUpdatedRocketChatMembers(session, consultant,
          initialConsultant, initialStatus);

      if (session.hasFeedbackChat()) {
        updateFeedbackChatRelevantAssignments(session, consultant, initialConsultant, initialStatus,
            memberList);
      }
    } catch (UpdateSessionException updateEx) {
      String message = String.format("Could not update session %s with consultantId %s",
          session.getId(), consultant.getId());
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    } catch (RocketChatGetGroupMembersException getGroupMembersEx) {
      rollbackSessionUpdate(session, initialConsultant, initialStatus);

      String message = String.format(
          "Could not get Rocket.Chat group members of group id %s. Initiate rollback.",
          session.getGroupId());
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
    sendEmailForConsultantChange(session, consultant);
  }

  private List<GroupMemberDTO> createUpdatedRocketChatMembers(Session session,
      Consultant consultant, Consultant initialConsultant, SessionStatus initialStatus)
      throws RocketChatGetGroupMembersException {
    addTechnicalUserToGroup(session.getGroupId(), session, initialConsultant, initialStatus);

    List<GroupMemberDTO> memberList = rocketChatService.getMembersOfGroup(session.getGroupId());
    if (nonNull(initialConsultant)) {
      addUserToRocketChatGroup(session.getGroupId(), consultant.getRocketChatId(), session,
          initialConsultant, initialStatus, memberList);
    }

    removeUnauthorizedUsersFromRocketChatGroup(memberList, session, consultant,
        initialConsultant, initialStatus);

    removeTechnicalUserFromGroup(session.getGroupId(), session, initialConsultant,
        initialStatus, memberList);

    removeSystemMessagesFromRocketChatGroup(session.getGroupId(), null, session,
        initialConsultant, initialStatus, memberList);
    return memberList;
  }

  private void updateFeedbackChatRelevantAssignments(Session session, Consultant consultant,
      Consultant initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    addUserToRocketChatGroup(session.getFeedbackGroupId(), consultant.getRocketChatId(),
        session, initialConsultant, initialStatus, memberList);

    removePreviousConsultantFromRocketChatFeedbackGroup(session, consultant, initialConsultant,
        initialStatus, memberList);

    removeSystemMessagesFromRocketChatGroup(session.getFeedbackGroupId(),
        consultant.getRocketChatId(), session, initialConsultant, initialStatus, memberList);
  }

  private void rollbackSessionUpdate(Session session, Consultant consultant,
      SessionStatus status) {

    if (nonNull(session)) {
      try {
        session.setConsultant(consultant);
        session.setStatus(status);
        sessionService.updateConsultantAndStatusForSession(session, session.getConsultant(),
            session.getStatus());

      } catch (UpdateSessionException updateSessionEx) {
        LogService.logInternalServerError(String.format(
            "Error during rollback while setting the session with id %s back to previous state.",
            session.getId()));
      }
    }
  }

  private void rollbackSessionAndRocketChatGroup(Session session,
      Consultant initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    rollbackSessionUpdate(session, initialConsultant, initialStatus);
    rocketChatRollbackService.rollbackRemoveUsersFromRocketChatGroup(session.getGroupId(),
        memberList);
    if (nonNull(initialConsultant)) {
      try {
        rocketChatService.removeUserFromGroup(initialConsultant.getRocketChatId(),
            session.getFeedbackGroupId());
      } catch (RocketChatRemoveUserFromGroupException e) {
        throw new InternalServerErrorException(e.getMessage(),
            LogService::logInternalServerError);
      }
    }
  }

  private void addUserToRocketChatGroup(String groupId, String rcUserId, Session session,
      Consultant initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {

    try {
      rocketChatService.addUserToGroup(rcUserId, groupId);
    } catch (RocketChatAddUserToGroupException addUserEx) {
      rollbackSessionUpdate(session, initialConsultant, initialStatus);

      if (session.hasFeedbackChat()) {
        rocketChatRollbackService.rollbackRemoveUsersFromRocketChatGroup(groupId, memberList);
      }

      String message = String.format(
          "Could not add user with id %s to Rocket.Chat group with id %s. Initiate rollback.",
          rcUserId, groupId);
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  private void addTechnicalUserToGroup(String groupId, Session session,
      Consultant initialConsultant, SessionStatus initialStatus) {

    try {
      rocketChatService.addTechnicalUserToGroup(groupId);
    } catch (RocketChatAddUserToGroupException | RocketChatUserNotInitializedException addUserEx) {
      rollbackSessionUpdate(session, initialConsultant, initialStatus);
      String message = String.format(
          "Could not add Rocket.Chat technical user to Rocket.Chat group with id %s. Initiate rollback.",
          session.getGroupId());
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  private void removeTechnicalUserFromGroup(String groupId, Session session,
      Consultant initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    try {
      rocketChatService.removeTechnicalUserFromGroup(groupId);
    } catch (RocketChatRemoveUserFromGroupException | RocketChatUserNotInitializedException e) {
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);

      String message = String.format(
          "Could not remove technical user from Rocket.Chat group id %s", session.getGroupId());
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  private void removeUnauthorizedUsersFromRocketChatGroup(List<GroupMemberDTO> memberList,
      Session session, Consultant consultant, Consultant initialConsultant,
      SessionStatus initialStatus) {

    AtomicReference<String> rcUserIdToRemove = new AtomicReference<>();
    try {
      for (GroupMemberDTO memberDTO : memberList) {
        removeUnauthorizedMember(memberDTO, session, consultant, rcUserIdToRemove);
      }
    } catch (KeycloakException keycloakEx) {
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
      String message = String
          .format("Could not get Keycloak roles for user with id %s. Initiate rollback.",
              rcUserIdToRemove);
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    } catch (InternalServerErrorException serviceEx) {
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
      String message = String.format(
          "Could not get consultant with id %s information from database for role check. Initiate "
              + "rollback.", consultant.getId());
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  private void removeUnauthorizedMember(GroupMemberDTO member, Session session,
      Consultant consultant, AtomicReference<String> rcUserIdToRemove) {
    if (isUnauthorizedMember(session, consultant, member)) {
      rcUserIdToRemove.set(member.get_id());

      if (!session.isTeamSession()) {
        try {
          rocketChatService.removeUserFromGroup(rcUserIdToRemove.get(), session.getGroupId());
        } catch (RocketChatRemoveUserFromGroupException removeUserEx) {
          String message = String.format(
              "Could not remove Rocket.Chat user %s from Rocket.Chat group with id %s. Initiate rollback.",
              rcUserIdToRemove, session.getGroupId());
          throw new InternalServerErrorException(message, LogService::logInternalServerError);
        }
      } else if (session.hasFeedbackChat()) {
        removeUserIfRightForPeerSessionViewIsNotGiven(session, rcUserIdToRemove, member);
      }
    }
  }

  private void removeUserIfRightForPeerSessionViewIsNotGiven(Session session,
      AtomicReference<String> rcUserIdToRemove, GroupMemberDTO member) {
    Optional<Consultant> memberConsultant =
        consultantService.getConsultantByRcUserId(member.get_id());
    if (memberConsultant.isPresent()
        && !keycloakAdminClientService.userHasAuthority(memberConsultant.get().getId(),
        Authority.VIEW_ALL_PEER_SESSIONS)) {
      try {
        rocketChatService.removeUserFromGroup(rcUserIdToRemove.get(), session.getGroupId());
      } catch (RocketChatRemoveUserFromGroupException e) {
        throw new InternalServerErrorException(e.getMessage(), LogService::logInternalServerError);
      }
    }
  }

  private boolean isUnauthorizedMember(Session session, Consultant consultant,
      GroupMemberDTO member) {
    return !member.get_id().equalsIgnoreCase(session.getUser().getRcUserId())
        && !member.get_id().equalsIgnoreCase(consultant.getRocketChatId())
        && !member.getUsername().equalsIgnoreCase(rocketChatTechUserUsername)
        && !member.get_id().equalsIgnoreCase(rocketChatSystemUserId);
  }

  private void removePreviousConsultantFromRocketChatFeedbackGroup(Session session,
      Consultant consultant, Consultant initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {

    try {
      if (nonNull(initialConsultant) && !keycloakAdminClientService
          .userHasAuthority(initialConsultant.getId(),
              Authority.VIEW_ALL_FEEDBACK_SESSIONS)) {
        updateRocketChatUsersForFeedbackGroup(session, consultant, initialConsultant,
            initialStatus, memberList);
      }
    } catch (KeycloakException keycloakEx) {
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus,
          memberList);
      String consultantId =
          nonNull(initialConsultant) ? initialConsultant.getId() : "unknown";
      String message = String
          .format("Could not get Keycloak roles for user with id %s. Initiate rollback.",
              consultantId);

      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  private void updateRocketChatUsersForFeedbackGroup(Session session, Consultant consultant,
      Consultant initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    try {
      rocketChatService.addTechnicalUserToGroup(session.getFeedbackGroupId());
      removeConsultantFromFeedbackGroup(session, consultant, initialConsultant,
          initialStatus, memberList);
    } catch (RocketChatAddUserToGroupException | RocketChatUserNotInitializedException e) {
      removeUserFromGroupAndRollbackSession(consultant.getRocketChatId(), session,
          initialConsultant, initialStatus, memberList, consultant);
      String message = String
          .format("Could not add technical user to Rocket.Chat feedback group with id %s",
              session.getFeedbackGroupId());

      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  private void removeConsultantFromFeedbackGroup(Session session, Consultant consultant,
      Consultant initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    if (nonNull(initialConsultant)) {
      try {
        rocketChatService
            .removeUserFromGroup(initialConsultant.getRocketChatId(),
                session.getFeedbackGroupId());
        rocketChatService.removeTechnicalUserFromGroup(session.getFeedbackGroupId());
      } catch (RocketChatRemoveUserFromGroupException | RocketChatUserNotInitializedException e) {
        removeUserFromGroupAndRollbackSession(consultant.getRocketChatId(), session,
            initialConsultant, initialStatus, memberList, consultant);
        String message = String.format(
            "Could not remove technical user from Rocket.Chat feedback group with id %s",
            session.getGroupId());
        throw new InternalServerErrorException(message, LogService::logInternalServerError);
      }
    }
  }

  private void removeSystemMessagesFromRocketChatGroup(String groupId,
      String rcUserIdToRemoveOnRollback, Session session, Consultant initialConsultant,
      SessionStatus initialStatus, List<GroupMemberDTO> memberList) {

    try {
      rocketChatService.removeSystemMessages(groupId,
          LocalDateTime.now().minusHours(Helper.ONE_DAY_IN_HOURS), LocalDateTime.now());
    } catch (RocketChatRemoveSystemMessagesException | RocketChatUserNotInitializedException e) {
      if (nonNull(rcUserIdToRemoveOnRollback)) {
        removeUserFromGroupAndRollbackSession(rcUserIdToRemoveOnRollback, session,
            initialConsultant, initialStatus, memberList, initialConsultant);
      }
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
      String message = String
          .format("Could not remove system messages from Rocket.Chat group id %s", groupId);
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  private void removeUserFromGroupAndRollbackSession(String rcUserIdToRemoveOnRollback,
      Session session, Consultant initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList, Consultant consultant) {
    try {
      rocketChatService.removeUserFromGroup(rcUserIdToRemoveOnRollback,
          session.getFeedbackGroupId());
    } catch (RocketChatRemoveUserFromGroupException rocketChatRemoveUserFromGroupException) {
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus,
          memberList);

      String message = String.format(
          "Could not remove Rocket.Chat user %s from Rocket.Chat group with id %s. Initiate rollback.",
          consultant.getRocketChatId(), session.getFeedbackGroupId());
      throw new InternalServerErrorException(message, LogService::logInternalServerError);
    }
  }

  private void sendEmailForConsultantChange(Session session, Consultant consultant) {
    if (!authenticatedUser.getUserId().equals(consultant.getId())) {
      emailNotificationFacade.sendAssignEnquiryEmailNotification(consultant,
          authenticatedUser.getUserId(), session.getUser().getUsername());
    }
  }

}
