package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.repository.session.SessionStatus.IN_PROGRESS;
import static de.caritas.cob.userservice.api.repository.session.SessionStatus.NEW;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatRemoveFromGroupOperationService;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.LogService;
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
 * Facade to encapsulate the steps for accepting an enquiry and/or assigning a enquiry to a
 * consultant.
 */
@Service
@RequiredArgsConstructor
public class AssignEnquiryFacade {

  @Value("${rocket.technical.username}")
  private String rocketChatTechUserUsername;

  @Value("${rocket.systemuser.id}")
  private String rocketChatSystemUserId;

  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull RocketChatRollbackService rocketChatRollbackService;
  private final @NonNull SessionToConsultantVerifier sessionToConsultantVerifier;

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Remove all other
   * consultants from the Rocket.Chat group which don't have the right to view this session anymore.
   * Furthermore add the given {@link Consultant} to the feedback group if needed.
   *
   * @param session    the session to assign the consultant
   * @param consultant the consultant to assign
   */
  public void assignRegisteredEnquiry(Session session, Consultant consultant) {
    assignEnquiry(session, consultant);
    updateRocketChatRooms(session, consultant);
  }

  /**
   * Assigns the given {@link Session} session to the given {@link Consultant}. Add the given {@link
   * Consultant} to the Rocket.Chat group.
   *
   * @param session    the session to assign the consultant
   * @param consultant the consultant to assign
   */
  public void assignAnonymousEnquiry(Session session, Consultant consultant) {
    assignEnquiry(session, consultant);
    try {
      this.rocketChatFacade
          .addUserToRocketChatGroup(consultant.getRocketChatId(), session.getGroupId());
    } catch (Exception e) {
      LogService.logInternalServerError(e);
      rollbackSessionUpdate(session);
    }
  }

  private void assignEnquiry(Session session, Consultant consultant) {
    var consultantSessionDTO = ConsultantSessionDTO.builder()
        .consultant(consultant)
        .session(session)
        .build();
    sessionToConsultantVerifier.verifySessionIsNotInProgress(consultantSessionDTO);
    sessionToConsultantVerifier.verifyPreconditionsForAssignment(consultantSessionDTO);

    sessionService.updateConsultantAndStatusForSession(session, consultant, IN_PROGRESS);
  }

  private void updateRocketChatRooms(Session session, Consultant consultant) {
    List<GroupMemberDTO> memberList = null;
    try {
      memberList = this.rocketChatFacade.retrieveRocketChatMembers(session.getGroupId());
      if (!session.isTeamSession()) {
        removeAllOtherMembers(session, consultant, memberList);
      }
      this.rocketChatFacade.removeSystemMessagesFromRocketChatGroup(session.getGroupId());
      if (session.hasFeedbackChat()) {
        updateFeedbackChatRelevantAssignments(session, consultant);
      }
    } catch (Exception e) {
      initiateRollback(session, memberList);
      throw e;
    }
  }

  private void initiateRollback(Session session, List<GroupMemberDTO> groupMembers) {
    this.rocketChatRollbackService.rollbackRemoveUsersFromRocketChatGroup(session.getGroupId(),
        groupMembers);
    this.rocketChatRollbackService
        .rollbackRemoveUsersFromRocketChatGroup(session.getFeedbackGroupId(), groupMembers);
    rollbackSessionUpdate(session);
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

  private void updateFeedbackChatRelevantAssignments(Session session, Consultant consultant) {
    this.rocketChatFacade.addUserToRocketChatGroup(consultant.getRocketChatId(),
        session.getFeedbackGroupId());

    this.rocketChatFacade.removeSystemMessagesFromRocketChatGroup(session.getFeedbackGroupId());
  }

  private void rollbackSessionUpdate(Session session) {
    if (nonNull(session)) {
      sessionService.updateConsultantAndStatusForSession(session, null, NEW);
    }
  }

}
