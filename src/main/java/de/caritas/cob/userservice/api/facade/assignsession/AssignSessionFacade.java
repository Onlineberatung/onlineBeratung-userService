package de.caritas.cob.userservice.api.facade.assignsession;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.authorization.Authority;
import de.caritas.cob.userservice.api.exception.ServiceException;
import de.caritas.cob.userservice.api.exception.UpdateSessionException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatAddUserToGroupException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatGetGroupMembersException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatLoginException;
import de.caritas.cob.userservice.api.exception.rocketChat.RocketChatRemoveUserFromGroupException;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import de.caritas.cob.userservice.api.service.helper.RocketChatRollbackHelper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
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
  private final @NonNull KeycloakAdminClientHelper keycloakAdminClientHelper;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull RocketChatRollbackHelper rocketChatRollbackHelper;
  private final @NonNull LogService logService;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull EmailNotificationFacade emailNotificationFacade;

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Remove all other
   * consultants from the Rocket.Chat group which don't have the right to view this session anymore.
   * Furthermore add the given {@link Consultant} to the feedback group if needed.
   */
  public void assignEnquiry(Session session, Consultant consultant) {
    new SessionToConsultantVerifier(session, consultant, this.logService)
        .verifySessionIsNotInProgress();
    assignSession(session, consultant);
  }

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Remove all other
   * consultants from the Rocket.Chat group which don't have the right to view this session anymore.
   * Furthermore add the given {@link Consultant} to the feedback group if needed.
   */
  public void assignSession(Session session, Consultant consultant) {

    SessionToConsultantVerifier sessionToConsultantVerifier =
        new SessionToConsultantVerifier(session, consultant, this.logService);
    sessionToConsultantVerifier.verifyPreconditionsForAssignment();

    Optional<Consultant> initialConsultant = Optional.ofNullable(session.getConsultant());
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
    } catch (RocketChatLoginException rcLoginEx) {
      throw new InternalServerErrorException("Could not login Rocket.Chat technical user",
          logService::logInternalServerError);
    } catch (UpdateSessionException updateEx) {
      String message = String.format("Could not update session %s with consultantId %s",
          session.getId(), consultant.getId());
      throw new InternalServerErrorException(message, logService::logInternalServerError);
    } catch (RocketChatGetGroupMembersException getGroupMembersEx) {
      rollbackSessionUpdate(session, initialConsultant, initialStatus);

      String message = String.format(
          "Could not get Rocket.Chat group members of group id %s. Initiate rollback.",
          session.getGroupId());
      throw new InternalServerErrorException(message, logService::logInternalServerError);
    }
    sendEmailForConsultantChange(session, consultant);
  }

  private List<GroupMemberDTO> createUpdatedRocketChatMembers(Session session,
      Consultant consultant,
      Optional<Consultant> initialConsultant, SessionStatus initialStatus) {
    addTechnicalUserToGroup(session.getGroupId(), session, initialConsultant, initialStatus);

    List<GroupMemberDTO> memberList = rocketChatService.getMembersOfGroup(session.getGroupId());
    if (initialConsultant.isPresent()) {
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
      Optional<Consultant> initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    addUserToRocketChatGroup(session.getFeedbackGroupId(), consultant.getRocketChatId(),
        session, initialConsultant, initialStatus, memberList);

    removePreviousConsultantFromRocketChatFeedbackGroup(session, consultant, initialConsultant,
        initialStatus, memberList);

    removeSystemMessagesFromRocketChatGroup(session.getFeedbackGroupId(),
        consultant.getRocketChatId(), session, initialConsultant, initialStatus, memberList);
  }

  private void rollbackSessionUpdate(Session session, Optional<Consultant> consultant,
      SessionStatus status) {

    if (nonNull(session)) {
      try {
        session.setConsultant(consultant.orElse(null));
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

  private void rollbackSessionAndRocketChatGroup(Session session,
      Optional<Consultant> initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    rollbackSessionUpdate(session, initialConsultant, initialStatus);
    rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(session.getGroupId(),
        memberList);
    initialConsultant
        .ifPresent(consultant -> rocketChatService.removeUserFromGroup(consultant.getRocketChatId(),
            session.getFeedbackGroupId()));
  }

  private void addUserToRocketChatGroup(String groupId, String rcUserId, Session session,
      Optional<Consultant> initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {

    try {
      rocketChatService.addUserToGroup(rcUserId, groupId);
    } catch (RocketChatAddUserToGroupException addUserEx) {
      rollbackSessionUpdate(session, initialConsultant, initialStatus);

      if (session.hasFeedbackChat()) {
        rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(groupId, memberList);
      }

      String message = String.format(
          "Could not add user with id %s to Rocket.Chat group with id %s. Initiate rollback.",
          rcUserId, groupId);
      throw new InternalServerErrorException(message, logService::logInternalServerError);
    }
  }

  private void addTechnicalUserToGroup(String groupId, Session session,
      Optional<Consultant> initialConsultant, SessionStatus initialStatus) {

    String possibleErrorMessage = String.format(
        "Could not add Rocket.Chat technical user to Rocket.Chat group with id %s. Initiate rollback.",
        session.getGroupId());

    try {
      if (!rocketChatService.addTechnicalUserToGroup(groupId)) {
        rollbackSessionUpdate(session, initialConsultant, initialStatus);
        throw new InternalServerErrorException(possibleErrorMessage,
            logService::logInternalServerError);
      }

    } catch (RocketChatAddUserToGroupException addUserEx) {
      rollbackSessionUpdate(session, initialConsultant, initialStatus);
      throw new InternalServerErrorException(possibleErrorMessage,
          logService::logInternalServerError);
    }
  }

  private void removeTechnicalUserFromGroup(String groupId, Session session,
      Optional<Consultant> initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    if (!rocketChatService.removeTechnicalUserFromGroup(groupId)) {
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);

      String message = String.format(
          "Could not remove technical user from Rocket.Chat group id %s", session.getGroupId());
      throw new InternalServerErrorException(message, logService::logInternalServerError);
    }
  }

  private void removeUnauthorizedUsersFromRocketChatGroup(List<GroupMemberDTO> memberList,
      Session session, Consultant consultant, Optional<Consultant> initialConsultant,
      SessionStatus initialStatus) {

    AtomicReference<String> rcUserIdToRemove = new AtomicReference<>();
    try {
      memberList.forEach(removeUnauthorizedMember(session, consultant, rcUserIdToRemove));
    } catch (RocketChatRemoveUserFromGroupException removeUserEx) {
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
      String message = String.format(
          "Could not remove Rocket.Chat user %s from Rocket.Chat group with id %s. Initiate rollback.",
          rcUserIdToRemove, session.getGroupId());
      throw new InternalServerErrorException(message, logService::logInternalServerError);
    } catch (KeycloakException keycloakEx) {
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
      String message = String
          .format("Could not get Keycloak roles for user with id %s. Initiate rollback.",
              rcUserIdToRemove);
      throw new InternalServerErrorException(message, logService::logInternalServerError);
    } catch (ServiceException serviceEx) {
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
      String message = String.format(
          "Could not get consultant with id %s information from database for role check. Initiate "
              + "rollback.", consultant.getId());
      throw new InternalServerErrorException(message, logService::logInternalServerError);
    }
  }

  private Consumer<GroupMemberDTO> removeUnauthorizedMember(Session session, Consultant consultant,
      AtomicReference<String> rcUserIdToRemove) {
    return member -> {
      if (isUnauthorizedMember(session, consultant, member)) {
        rcUserIdToRemove.set(member.get_id());

        if (!session.isTeamSession()) {
          rocketChatService.removeUserFromGroup(rcUserIdToRemove.get(), session.getGroupId());
        } else if (session.hasFeedbackChat()) {
          removeUserIfRightForPeerSessionViewIsNotGiven(session, rcUserIdToRemove, member);
        }
      }
    };
  }

  private void removeUserIfRightForPeerSessionViewIsNotGiven(Session session,
      AtomicReference<String> rcUserIdToRemove, GroupMemberDTO member) {
    Optional<Consultant> memberConsultant =
        consultantService.getConsultantByRcUserId(member.get_id());
    if (memberConsultant.isPresent()
        && !keycloakAdminClientHelper.userHasAuthority(memberConsultant.get().getId(),
        Authority.VIEW_ALL_PEER_SESSIONS)) {
      rocketChatService.removeUserFromGroup(rcUserIdToRemove.get(), session.getGroupId());
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
      Consultant consultant, Optional<Consultant> initialConsultantOptional,
      SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {

    try {
      initialConsultantOptional.ifPresent(initialConsultant -> {
        if (!keycloakAdminClientHelper.userHasAuthority(initialConsultant.getId(),
            Authority.VIEW_ALL_FEEDBACK_SESSIONS)) {
          updateRocketChatUsersForFeedbackGroup(session, consultant, initialConsultantOptional,
              initialStatus, memberList);
        }
      });
    } catch (KeycloakException keycloakEx) {
      rollbackSessionAndRocketChatGroup(session, initialConsultantOptional, initialStatus,
          memberList);
      String consultantId =
          initialConsultantOptional.isPresent() ? initialConsultantOptional.get().getId() :
              "unknown";
      String message = String
          .format("Could not get Keycloak roles for user with id %s. Initiate rollback.",
              consultantId);

      throw new InternalServerErrorException(message, logService::logInternalServerError);

    } catch (RocketChatAddUserToGroupException addUserEx) {
      rollbackSessionUpdate(session, initialConsultantOptional, initialStatus);
      rocketChatRollbackHelper.rollbackRemoveUsersFromRocketChatGroup(session.getGroupId(),
          memberList);

      String message = String.format(
          "Could not add Rocket.Chat technical user to Rocket.Chat group with id %s. Initiate "
              + "rollback.", session.getFeedbackGroupId());
      throw new InternalServerErrorException(message, logService::logInternalServerError);

    } catch (RocketChatRemoveUserFromGroupException removeUserEx) {
      rollbackSessionAndRocketChatGroup(session, initialConsultantOptional, initialStatus,
          memberList);

      String message = String.format(
          "Could not remove Rocket.Chat user %s from Rocket.Chat group with id %s. Initiate rollback.",
          consultant.getRocketChatId(), session.getFeedbackGroupId());
      throw new InternalServerErrorException(message, logService::logInternalServerError);
    }
  }

  private void updateRocketChatUsersForFeedbackGroup(Session session, Consultant consultant,
      Optional<Consultant> initialConsultantOptional, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    if (rocketChatService.addTechnicalUserToGroup(session.getFeedbackGroupId())) {
      removeConsultantFromFeedbackGroup(session, consultant, initialConsultantOptional,
          initialStatus, memberList);
    } else {
      removeUserFromGroupAndRollbackSession(session, consultant, initialConsultantOptional,
          initialStatus,
          memberList);
      String message = String
          .format("Could not add technical user to Rocket.Chat feedback group with id %s",
              session.getFeedbackGroupId());

      throw new InternalServerErrorException(message, logService::logInternalServerError);
    }
  }

  private void removeConsultantFromFeedbackGroup(Session session, Consultant consultant,
      Optional<Consultant> initialConsultantOptional, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    initialConsultantOptional.ifPresent(initialConsultant -> rocketChatService
        .removeUserFromGroup(initialConsultant.getRocketChatId(),
            session.getFeedbackGroupId()));

    if (!rocketChatService.removeTechnicalUserFromGroup(session.getFeedbackGroupId())) {
      removeUserFromGroupAndRollbackSession(session, consultant, initialConsultantOptional,
          initialStatus, memberList);
      String message = String.format(
          "Could not remove technical user from Rocket.Chat feedback group with id %s",
          session.getGroupId());
      throw new InternalServerErrorException(message, logService::logInternalServerError);
    }
  }

  private void removeUserFromGroupAndRollbackSession(Session session, Consultant consultant,
      Optional<Consultant> initialConsultant, SessionStatus initialStatus,
      List<GroupMemberDTO> memberList) {
    rocketChatService.removeUserFromGroup(consultant.getRocketChatId(),
        session.getFeedbackGroupId());
    rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
  }

  private void removeSystemMessagesFromRocketChatGroup(String groupId,
      String rcUserIdToRemoveOnRollback, Session session, Optional<Consultant> initialConsultant,
      SessionStatus initialStatus, List<GroupMemberDTO> memberList) {

    if (!rocketChatService.removeSystemMessages(groupId,
        LocalDateTime.now().minusHours(Helper.ONE_DAY_IN_HOURS), LocalDateTime.now())) {

      if (nonNull(rcUserIdToRemoveOnRollback)) {
        rocketChatService.removeUserFromGroup(rcUserIdToRemoveOnRollback,
            session.getFeedbackGroupId());
      }
      rollbackSessionAndRocketChatGroup(session, initialConsultant, initialStatus, memberList);
      String message = String
          .format("Could not remove system messages from Rocket.Chat group id %s", groupId);
      throw new InternalServerErrorException(message, logService::logInternalServerError);
    }
  }

  private void sendEmailForConsultantChange(Session session, Consultant consultant) {
    if (!authenticatedUser.getUserId().equals(consultant.getId())) {
      emailNotificationFacade.sendAssignEnquiryEmailNotification(consultant,
          authenticatedUser.getUserId(), session.getUser().getUsername());
    }
  }

}
