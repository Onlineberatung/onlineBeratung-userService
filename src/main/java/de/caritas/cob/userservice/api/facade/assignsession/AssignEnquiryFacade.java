package de.caritas.cob.userservice.api.facade.assignsession;

import static de.caritas.cob.userservice.api.repository.session.SessionStatus.IN_PROGRESS;
import static de.caritas.cob.userservice.api.repository.session.SessionStatus.NEW;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.admin.service.rocketchat.RocketChatRemoveFromGroupOperationService;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.rocketchat.group.GroupMemberDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatRollbackService;
import de.caritas.cob.userservice.api.service.session.SessionService;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate the steps for accepting an enquiry and/or assigning a enquiry to a
 * consultant.
 */
@Service
@RequiredArgsConstructor
public class AssignEnquiryFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull RocketChatFacade rocketChatFacade;
  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull RocketChatRollbackService rocketChatRollbackService;
  private final @NonNull SessionToConsultantVerifier sessionToConsultantVerifier;
  private final @NonNull ConsultingTypeManager consultingTypeManager;
  private final @NonNull UnauthorizedMembersProvider unauthorizedMembersProvider;

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
    updateRocketChatRooms(session.getGroupId(), session, consultant);
    if (session.hasFeedbackChat()) {
      updateRocketChatRooms(session.getFeedbackGroupId(), session, consultant);
    }
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
      rollbackSessionUpdate(session);
      throw new InternalServerErrorException(
          String.format("Could not add consultant %s to group %s", consultant.getRocketChatId(),
              session.getGroupId()));
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

  private void updateRocketChatRooms(String rcGroupId, Session session, Consultant consultant) {
    List<GroupMemberDTO> memberList = null;
    try {
      memberList = this.rocketChatFacade.retrieveRocketChatMembers(rcGroupId);
      removeUnauthorizedMembers(rcGroupId, session, consultant, memberList);
      if (session.hasFeedbackChat()) {
        this.rocketChatFacade
            .addUserToRocketChatGroup(consultant.getRocketChatId(), rcGroupId);
      }
      this.rocketChatFacade.removeSystemMessagesFromRocketChatGroup(rcGroupId);

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

  private void removeUnauthorizedMembers(String rcGroupId, Session session, Consultant consultant,
      List<GroupMemberDTO> memberList) {
    List<Consultant> consultantsToRemoveFromRocketChat =
        unauthorizedMembersProvider.obtainConsultantsToRemove(rcGroupId, session, consultant, memberList);

    if (rcGroupId.equalsIgnoreCase(session.getGroupId())) {
      RocketChatRemoveFromGroupOperationService
          .getInstance(this.rocketChatFacade, this.keycloakAdminClientService, this.consultingTypeManager)
          .onSessionConsultants(Map.of(session, consultantsToRemoveFromRocketChat))
          .removeFromGroupOrRollbackOnFailure();
    }
    if (rcGroupId.equalsIgnoreCase(session.getFeedbackGroupId())) {
      RocketChatRemoveFromGroupOperationService
          .getInstance(this.rocketChatFacade, this.keycloakAdminClientService, this.consultingTypeManager)
          .onSessionConsultants(Map.of(session, consultantsToRemoveFromRocketChat))
          .removeFromFeedbackGroupOrRollbackOnFailure();
    }
  }

  private void rollbackSessionUpdate(Session session) {
    if (nonNull(session)) {
      sessionService.updateConsultantAndStatusForSession(session, null, NEW);
    }
  }

}
