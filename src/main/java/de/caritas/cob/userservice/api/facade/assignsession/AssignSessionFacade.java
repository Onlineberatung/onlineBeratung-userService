package de.caritas.cob.userservice.api.facade.assignsession;

import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatRemoveFromGroupOperationService;
import de.caritas.cob.userservice.api.facade.EmailNotificationFacade;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionStatus;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatRollbackService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
  private final @NonNull RocketChatFacade rocketChatFacade;
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
  public void assignSession(Session session, Consultant consultant) {

    var sessionToConsultantVerifier = new SessionToConsultantVerifier(session, consultant);
    sessionToConsultantVerifier.verifyPreconditionsForAssignment();

    var initialConsultant = session.getConsultant();
    SessionStatus initialStatus = session.getStatus();
    List<GroupMemberDTO> initialMembers =
        this.rocketChatFacade.retrieveRocketChatMembers(session.getGroupId());
    List<GroupMemberDTO> initialFeedbackGroupMembers =
        this.rocketChatFacade.retrieveRocketChatMembers(session.getFeedbackGroupId());

    try {
      updateSessionInDatabase(session, consultant, initialStatus);
      updateChangesInRocketChatRooms(session, consultant);
    } catch (Exception exception) {
      initiateRollback(session, initialConsultant, initialStatus, initialMembers,
          initialFeedbackGroupMembers);
      throw exception;
    }
    sendEmailForConsultantChange(session, consultant);
  }

  private void updateChangesInRocketChatRooms(Session session, Consultant consultant) {
    if (!session.isTeamSession()) {
      removeAllOtherMembersOfGroup(session.getGroupId(), session, consultant);
    }

    rocketChatFacade.addUserToRocketChatGroup(consultant.getRocketChatId(), session.getGroupId());
    rocketChatFacade.removeSystemMessagesFromRocketChatGroup(session.getGroupId());

    if (session.hasFeedbackChat()) {
      removeAllOtherMembersOfGroup(session.getFeedbackGroupId(), session, consultant);
      rocketChatFacade
          .addUserToRocketChatGroup(consultant.getRocketChatId(), session.getFeedbackGroupId());
      rocketChatFacade.removeSystemMessagesFromRocketChatGroup(session.getFeedbackGroupId());
    }
  }

  private void updateSessionInDatabase(Session session, Consultant consultant,
      SessionStatus initialStatus) {
    sessionService.updateConsultantAndStatusForSession(session, consultant,
        initialStatus == SessionStatus.NEW ? SessionStatus.IN_PROGRESS : initialStatus);
  }

  private void removeAllOtherMembersOfGroup(String rcGroupId, Session session,
      Consultant consultant) {
    List<GroupMemberDTO> memberList = this.rocketChatFacade.retrieveRocketChatMembers(rcGroupId);
    removeAllOtherMembers(session, consultant, memberList);
  }

  private void removeAllOtherMembers(Session session, Consultant consultant,
      List<GroupMemberDTO> memberList) {
    List<Consultant> consultantsToRemoveFromRocketChat = memberList.stream()
        .filter(groupMemberDTO -> isUnauthorizedMember(session, consultant, groupMemberDTO))
        .map(GroupMemberDTO::get_id)
        .map(this.consultantService::getConsultantByRcUserId)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    RocketChatRemoveFromGroupOperationService
        .getInstance(this.rocketChatFacade, this.keycloakAdminClientService)
        .onSessionConsultants(Map.of(session, consultantsToRemoveFromRocketChat))
        .removeFromGroupsOrRollbackOnFailure();
  }

  private boolean isUnauthorizedMember(Session session, Consultant consultant,
      GroupMemberDTO member) {
    return !member.get_id().equalsIgnoreCase(session.getUser().getRcUserId())
        && !member.get_id().equalsIgnoreCase(consultant.getRocketChatId())
        && !member.getUsername().equalsIgnoreCase(rocketChatTechUserUsername)
        && !member.get_id().equalsIgnoreCase(rocketChatSystemUserId);
  }

  private void initiateRollback(Session session, Consultant initialConsultant,
      SessionStatus initialStatus, List<GroupMemberDTO> initialMembers,
      List<GroupMemberDTO> initialFeedbackGroupMembers) {
    this.rocketChatRollbackService.rollbackRemoveUsersFromRocketChatGroup(session.getGroupId(),
        initialMembers);
    this.rocketChatRollbackService
        .rollbackRemoveUsersFromRocketChatGroup(session.getFeedbackGroupId(),
            initialFeedbackGroupMembers);
    this.sessionService
        .updateConsultantAndStatusForSession(session, initialConsultant, initialStatus);
  }

  private void sendEmailForConsultantChange(Session session, Consultant consultant) {
    if (!authenticatedUser.getUserId().equals(consultant.getId())) {
      emailNotificationFacade.sendAssignEnquiryEmailNotification(consultant,
          authenticatedUser.getUserId(), session.getUser().getUsername());
    }
  }

}
